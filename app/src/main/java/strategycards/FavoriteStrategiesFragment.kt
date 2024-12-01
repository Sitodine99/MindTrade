package strategycards

import adapters.FavoriteStrategyAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
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
            openStrategyDetailFragment(strategy) // Llamar al método para abrir el detalle
        }

        recyclerView.adapter = adapter

        // Cargar estrategias favoritas
        loadFavoriteStrategies()

        return view
    }

    private fun openStrategyDetailFragment(strategy: Strategy) {
        // Ocultar el contenedor de fragmentos inicialmente
        val activity = requireActivity()
        val fragmentContainer = activity.findViewById<FragmentContainerView>(R.id.fragmentContainer)
        fragmentContainer?.visibility = View.INVISIBLE // Mantener invisible inicialmente

        // Crear el fragmento de detalle
        val fragment = StrategyDetailFragment().apply {
            arguments = Bundle().apply {
                putString("strategyId", strategy.id)
                putString("strategyTitle", strategy.title)
                putString("strategyDescription", strategy.description)
                putString("strategyAuthor", strategy.author)
                putString("strategyAvatarName", strategy.avatarName)
                putString("strategyAvatarUrl", strategy.avatarUrl)
                putStringArray("strategyIndicators", strategy.indicators.toTypedArray())
                putStringArray("strategyTimeframes", strategy.timeframes.toTypedArray())
                putStringArray("tradingStyles", strategy.tradingStyles.toTypedArray())
                putDouble("strategyRating", strategy.rating)
                putString("algorithmCode", strategy.algorithmCode) // Pasar el campo algorithmCode
                putStringArray("strategySymbols", strategy.symbols.toTypedArray())
            }
        }

        // Añadir el fragmento a la pila pero sin mostrarlo inmediatamente
        activity.supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in, // Animación de entrada
                R.anim.fade_out, // Animación de salida
                R.anim.fade_in, // Animación al retroceder (popEnter)
                R.anim.fade_out  // Animación al salir (popExit)
            )
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null) // Agregar el fragmento a la pila de retroceso
            .commit()

        // Retrasar la visibilidad del contenedor hasta que el fragmento esté cargado
        fragmentContainer?.postDelayed({
            fragmentContainer.visibility = View.VISIBLE // Mostrar el fragmento cargado
        }, 300) // Ajustar el retraso (300ms) según sea necesario
    }

    private fun loadFavoriteStrategies() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Ocultar el RecyclerView inicialmente
        recyclerView.visibility = View.INVISIBLE

        db.collection("strategies")
            .whereArrayContains("favoritedBy", userId)
            .get()
            .addOnSuccessListener { result ->
                strategies.clear()
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
                        symbols = (document.get("symbols") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        algorithmCode = document.getString("algorithmCode") ?: ""
                    )
                })
                adapter.notifyDataSetChanged()

                // Retrasar la visibilidad del RecyclerView para una transición suave
                recyclerView.postDelayed({
                    recyclerView.visibility = View.VISIBLE
                }, 300) // Retraso de 300ms (ajustable)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al cargar estrategias favoritas", Toast.LENGTH_SHORT).show()
            }
    }
}
