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
        ratingBar = view.findViewById(R.id.strategyRatingBar)
        Log.d("RatingBar", "RatingBar inicializado: ${ratingBar != null}")

        // Obtener datos desde los argumentos
        val tradingStyles = arguments?.getStringArray("tradingStyles")?.joinToString(", ")
        val indicators = arguments?.getStringArray("indicators")?.joinToString(", ")
        val timeframes = arguments?.getStringArray("timeframes")?.joinToString(", ")
        val rating = arguments?.getFloat("rating", 0f) ?: 0f
        strategyId = arguments?.getString("strategyId")
        Log.d("GeneralFragment", "Strategy ID recibido: $strategyId")

        // Configurar las vistas
        tradingStyleTextView.text = tradingStyles ?: "Sin estilos"
        indicatorsTextView.text = indicators ?: "Sin indicadores"
        timeframesTextView.text = timeframes ?: "Sin temporalidades"
        ratingBar.rating = rating

        // Configurar el estado inicial del RatingBar
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

        // Configurar listener para guardar valoración
        if (userId != null) {
            ratingBar.setOnRatingBarChangeListener { _, newRating, _ ->
                Log.d("RatingBar", "Usuario seleccionó un rating: $newRating")
                Toast.makeText(context, "Rating seleccionado: $newRating", Toast.LENGTH_SHORT).show()
                saveRating(newRating)
            }
        } else {
            ratingBar.setIsIndicator(true) // Desactivar para usuarios no autenticados
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

                // Obtener la valoración previa del usuario, si existe
                val previousRating = userRatings[userId]

                // Calcular nuevo promedio y total de votos
                val (newRating, newTotalVotes) = if (previousRating != null) {
                    // Actualizar promedio eliminando la valoración previa
                    val adjustedTotal = currentRating * totalVotes - previousRating + userRating
                    adjustedTotal / totalVotes to totalVotes
                } else {
                    // Nuevo voto del usuario
                    val adjustedTotal = currentRating * totalVotes + userRating
                    adjustedTotal / (totalVotes + 1) to (totalVotes + 1)
                }

                // Actualizar datos en Firestore
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
