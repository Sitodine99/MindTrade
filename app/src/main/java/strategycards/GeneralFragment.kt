package strategycards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mindtrade.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GeneralFragment : Fragment() {
    private lateinit var ratingBar: RatingBar
    private lateinit var successRateTextView: TextView
    private lateinit var totalMovementsTextView: TextView
    private lateinit var buyMovementsTextView: TextView
    private lateinit var sellMovementsTextView: TextView
    private lateinit var mostUsedSymbolTextView: TextView

    private val db = FirebaseFirestore.getInstance()
    private var strategyId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_general, container, false)

        // Vincular las vistas
        ratingBar = view.findViewById(R.id.strategyRatingBar)
        successRateTextView = view.findViewById(R.id.strategySuccessRateTextView)
        totalMovementsTextView = view.findViewById(R.id.strategyTotalMovementsTextView)
        buyMovementsTextView = view.findViewById(R.id.strategyBuyMovementsTextView)
        sellMovementsTextView = view.findViewById(R.id.strategySellMovementsTextView)
        mostUsedSymbolTextView = view.findViewById(R.id.strategyMostUsedSymbolTextView)

        // Obtener datos desde los argumentos
        strategyId = arguments?.getString("strategyId")

        // Cargar estadísticas
        strategyId?.let {
            loadStrategyStatistics(it)
            loadRating(it)
            loadStrategyDetails(it)
        }

        return view
    }

    /**
     * Carga estadísticas de la estrategia desde Firebase.
     */
    private fun loadStrategyStatistics(strategyId: String) {
        db.collection("strategies").document(strategyId)
            .collection("movements")
            .get()
            .addOnSuccessListener { documents ->
                val totalMovements = documents.size()
                var successfulMovements = 0
                var buyMovements = 0
                var sellMovements = 0
                val symbolCount = mutableMapOf<String, Int>()

                for (doc in documents) {
                    val profit = doc.getDouble("profit") ?: 0.0
                    val type = doc.getString("type") ?: "Unknown"
                    val symbol = doc.getString("symbol") ?: "Desconocido"

                    if (profit > 0) successfulMovements++
                    if (type.equals("Buy", ignoreCase = true)) buyMovements++
                    if (type.equals("Sell", ignoreCase = true)) sellMovements++

                    symbolCount[symbol] = symbolCount.getOrDefault(symbol, 0) + 1
                }

                val successRate = if (totalMovements > 0) {
                    (successfulMovements.toDouble() / totalMovements.toDouble()) * 100
                } else {
                    0.0
                }

                val mostUsedSymbol = symbolCount.maxByOrNull { it.value }?.key ?: "Desconocido"

                // Actualizar las vistas
                successRateTextView.text =
                    "Porcentaje de éxito: ${String.format("%.2f", successRate)}%"
                totalMovementsTextView.text = "Operaciones registradas: $totalMovements"

                // Aplicar color y actualizar texto dinámicamente
                buyMovementsTextView.text = "Operaciones Buy: $buyMovements"
                buyMovementsTextView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.blue_normal
                    )
                )

                sellMovementsTextView.text = "Operaciones Sell: $sellMovements"
                sellMovementsTextView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.my_red
                    )
                )

                mostUsedSymbolTextView.text = "Símbolo más utilizado: $mostUsedSymbol"
            }
            .addOnFailureListener {
                successRateTextView.text = "Porcentaje de éxito: Error al cargar"
                totalMovementsTextView.text = "Operaciones registradas: Error"
                buyMovementsTextView.text = "Operaciones Buy: Error"
                sellMovementsTextView.text = "Operaciones Sell: Error"
                mostUsedSymbolTextView.text = "Símbolo más utilizado: Error"

                Toast.makeText(context, "Error al obtener las estadísticas", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    /**
     * Carga la valoración del usuario y la estrategia desde Firebase.
     */
    private fun loadRating(strategyId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("strategies").document(strategyId).get().addOnSuccessListener { document ->
            val userRatings = document.get("userRatings") as? Map<String, Double> ?: emptyMap()

            if (userId != null && userRatings.containsKey(userId)) {
                val userRating = userRatings[userId]?.toFloat() ?: 0f
                ratingBar.rating = userRating
            }

            ratingBar.setIsIndicator(false) // Permitir que el usuario edite su valoración

            // Configurar interacción del usuario con el RatingBar
            var isUserTouching = false

            ratingBar.setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                    isUserTouching = true
                }
                false
            }

            ratingBar.setOnRatingBarChangeListener { _, newRating, _ ->
                if (isUserTouching) {
                    isUserTouching = false
                    saveRating(newRating)
                }
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Error al cargar la valoración", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Guarda la nueva valoración del usuario en Firebase.
     */
    private fun saveRating(userRating: Float) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        strategyId?.let { id ->
            val docRef = db.collection("strategies").document(id)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentRating = snapshot.getDouble("rating") ?: 0.0
                val totalVotes = snapshot.getLong("totalVotes")?.toInt() ?: 0
                val userRatings = snapshot.get("userRatings") as? Map<String, Double> ?: emptyMap()

                val previousRating = userRatings[userId]

                val (newRating, newTotalVotes) = if (previousRating != null) {
                    val adjustedTotal = currentRating * totalVotes - previousRating + userRating
                    adjustedTotal / totalVotes to totalVotes
                } else {
                    val adjustedTotal = currentRating * totalVotes + userRating
                    adjustedTotal / (totalVotes + 1) to (totalVotes + 1)
                }

                transaction.update(
                    docRef,
                    mapOf(
                        "rating" to newRating,
                        "totalVotes" to newTotalVotes,
                        "userRatings" to userRatings + (userId to userRating.toDouble())
                    )
                )
            }.addOnSuccessListener {
                Toast.makeText(context, "¡Valoración actualizada!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Error al actualizar valoración: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadStrategyDetails(strategyId: String) {
        db.collection("strategies").document(strategyId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Recuperar correctamente los datos
                    val tradingStyles =
                        (document.get("tradingStyles") as? List<*>)?.joinToString(", ") ?: "N/A"
                    val indicators =
                        (document.get("indicators") as? List<*>)?.joinToString(", ") ?: "N/A"
                    val timeframes =
                        (document.get("timeframes") as? List<*>)?.joinToString(", ") ?: "N/A"
                    val symbols = (document.get("symbols") as? List<*>)?.joinToString(", ") ?: "N/A"

                    // Asegurar que los IDs coincidan con los del XML
                    val tradingStyleTextView: TextView =
                        requireView().findViewById(R.id.tradingStyleTextView)
                    val indicatorsTextView: TextView =
                        requireView().findViewById(R.id.strategyIndicatorsTextView)
                    val timeframesTextView: TextView =
                        requireView().findViewById(R.id.strategyTimeframesTextView)
                    val symbolsTextView: TextView =
                        requireView().findViewById(R.id.strategySymbolsTextViewTest)

                    // Asignar los valores a las vistas SIN repetir el título
                    tradingStyleTextView.text = tradingStyles
                    indicatorsTextView.text = indicators
                    timeframesTextView.text = timeframes
                    symbolsTextView.text = symbols
                } else {
                    Toast.makeText(context, "Error: La estrategia no existe.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Error al cargar detalles de la estrategia",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}

