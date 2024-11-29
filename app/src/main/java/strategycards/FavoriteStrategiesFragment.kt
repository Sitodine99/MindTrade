package strategycards

import adapters.FavoriteStrategyAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Strategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteStrategiesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavoriteStrategyAdapter
    private val db = FirebaseFirestore.getInstance()
    private val strategies = mutableListOf<Strategy>() // Lista local para las estrategias favoritas

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite_strategies, container, false)

        // Configurar el RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewFavoriteStrategies)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inicializar el adaptador
        adapter = FavoriteStrategyAdapter(strategies) { strategy ->

            // Navegar al detalle de la estrategia (opcional)
            val fragment = StrategyDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("strategyId", strategy.id)
                    putString("strategyTitle", strategy.title)
                    putString("strategyDescription", strategy.description)
                    putString("strategyAuthor", strategy.author)
                    putString("strategyAvatarName", strategy.avatarName)
                    putStringArray("strategyIndicators", strategy.indicators.toTypedArray())
                    putStringArray("strategyTimeframes", strategy.timeframes.toTypedArray())
                    putStringArray("tradingStyles", strategy.tradingStyles.toTypedArray())
                    putDouble("strategyRating", strategy.rating)
                    putString("algorithmCode", strategy.algorithmCode) // Pasar el campo algorithmCode
                    putStringArray("strategySymbols", strategy.symbols.toTypedArray())
                }
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        recyclerView.adapter = adapter

        // Cargar estrategias favoritas
        loadFavoriteStrategies()

        return view
    }

    private fun loadFavoriteStrategies() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("strategies")
            .whereArrayContains("favoritedBy", userId)
            .get()
            .addOnSuccessListener { result ->
                strategies.clear()
                for (document in result) {
                    Log.d("FavoriteStrategies", "Documento: ${document.data}")
                }
                strategies.addAll(result.map { document ->
                    Strategy(
                        id = document.id,
                        title = document.getString("title") ?: "Sin título",
                        description = document.getString("description") ?: "Sin descripción",
                        author = document.getString("authorAlias") ?: "Anónimo",
                        avatarName = document.getString("avatarName"),
                        avatarUrl = document.getString("avatarUrl"),
                        rating = document.getDouble("rating") ?: 0.0,
                        createdBy = document.getString("createdBy") ?: "",
                        indicators = (document.get("indicators") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        timeframes = (document.get("timeframes") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        tradingStyles = (document.get("tradingStyles") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        symbols = (document.get("symbols") as? List<*>)?.filterIsInstance<kotlin.String>() ?: emptyList(),
                        algorithmCode = document.getString("algorithmCode") ?: ""
                    )
                })
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al cargar estrategias favoritas", Toast.LENGTH_SHORT).show()
                Log.e("FavoriteStrategies", "Error: ${e.message}")
            }
    }
}
