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
        strategyId?.let { id ->
            db.collection("strategies").document(id).get().addOnSuccessListener { document ->
                val votedBy = document.get("votedBy") as? List<*>
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                if (votedBy != null && userId in votedBy) {
                    ratingBar.setIsIndicator(true) // Desactivar interacción si ya ha votado
                } else {
                    ratingBar.setIsIndicator(false) // Permitir interacción si no ha votado
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Error al cargar los datos", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Desactivar RatingBar para usuarios no autenticados
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            ratingBar.setIsIndicator(false) // Permitir interacción
            ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                Log.d("RatingBar", "Usuario seleccionó un rating: $rating")
                Toast.makeText(context, "Rating seleccionado: $rating", Toast.LENGTH_SHORT).show()
                saveRating(rating)
            }

        } else {
            ratingBar.setIsIndicator(true) // Solo indicador
        }

        return view
    }

    private fun saveRating(userRating: Float) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("GeneralFragment", "strategyId: $strategyId")
        strategyId?.let { id ->
            val docRef = db.collection("strategies").document(id)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentRating = snapshot.getDouble("rating") ?: 0.0
                val totalVotes = snapshot.getLong("totalVotes")?.toInt() ?: 0
                val votedBy = snapshot.get("votedBy") as? List<*> ?: emptyList<Any>()

                // Verificar si el usuario ya ha votado
                if (votedBy.contains(userId)) {
                    throw Exception("Ya has votado esta estrategia.")
                }

                // Calcular nuevo promedio
                val newTotalVotes = totalVotes + 1
                val newRating = (currentRating * totalVotes + userRating) / newTotalVotes

                // Actualizar datos
                transaction.update(
                    docRef,
                    mapOf(
                        "rating" to newRating,
                        "totalVotes" to newTotalVotes,
                        "votedBy" to votedBy + userId // Agregar usuario a la lista de votantes
                    )
                )
            }.addOnSuccessListener {
                Toast.makeText(context, "¡Valoración guardada!", Toast.LENGTH_SHORT).show()
                // Desactivar el RatingBar para evitar votos adicionales
                ratingBar.setIsIndicator(true)
            }.addOnFailureListener { e ->
                if (e.message == "Ya has votado esta estrategia.") {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        "Error al guardar valoración: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}