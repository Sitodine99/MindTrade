package strategycards

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mindtrade.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GeneralFragment : Fragment() {
    private lateinit var ratingBar: RatingBar
    private val db = FirebaseFirestore.getInstance()
    private var strategyId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_general, container, false)

        // Vincular las vistas
        val tradingStyleTextView: TextView = view.findViewById(R.id.tradingStyleTextView)
        val indicatorsTextView: TextView = view.findViewById(R.id.strategyIndicatorsTextView)
        val timeframesTextView: TextView = view.findViewById(R.id.strategyTimeframesTextView)
        val symbolsTextView: TextView = view.findViewById(R.id.strategySymbolsTextViewTest)
        ratingBar = view.findViewById(R.id.strategyRatingBar)

        // Obtener datos desde los argumentos
        val tradingStyles = arguments?.getStringArray("tradingStyles")?.joinToString(", ")
        val indicators = arguments?.getStringArray("indicators")?.joinToString(", ")
        val timeframes = arguments?.getStringArray("timeframes")?.joinToString(", ")
        val symbols = arguments?.getStringArray("symbols")?.joinToString(", ") ?: "Sin símbolos"
        val rating = arguments?.getFloat("rating", 0f) ?: 0f
        strategyId = arguments?.getString("strategyId")

        // Configurar las vistas
        tradingStyleTextView.text = tradingStyles ?: "Sin estilos"
        indicatorsTextView.text = indicators ?: "Sin indicadores"
        timeframesTextView.text = timeframes ?: "Sin temporalidades"
        symbolsTextView.text = symbols
        ratingBar.rating = rating

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        strategyId?.let { id ->
            db.collection("strategies").document(id).get().addOnSuccessListener { document ->
                val userRatings = document.get("userRatings") as? Map<String, Double> ?: emptyMap()

                if (userId != null && userRatings.containsKey(userId)) {
                    // Mostrar la valoración actual del usuario
                    val userRating = userRatings[userId]?.toFloat() ?: 0f
                    ratingBar.rating = userRating
                }

                // Permitir interacción para usuarios autenticados
                ratingBar.setIsIndicator(false)
            }.addOnFailureListener {
                Toast.makeText(context, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar interacción del usuario con el RatingBar
        if (userId != null) {
            var isUserTouching = false

            // Detectar cuando el usuario interactúa directamente con el RatingBar
            ratingBar.setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                    isUserTouching = true
                }
                false // Permite que otros listeners también funcionen
            }

            // Solo guardar y mostrar el toast si el usuario cambia el valor manualmente
            ratingBar.setOnRatingBarChangeListener { _, newRating, _ ->
                if (isUserTouching) {
                    isUserTouching = false // Reiniciar la bandera
                    saveRating(newRating)
                }
            }
        } else {
            ratingBar.setIsIndicator(true)
        }

        return view
    }

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
}

