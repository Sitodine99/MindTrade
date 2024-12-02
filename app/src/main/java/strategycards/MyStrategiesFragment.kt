package strategycards

import adapters.SimpleStrategyAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Strategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyStrategiesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimpleStrategyAdapter
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null
    private var userAlias: String = "Anónimo"
    private val strategies = mutableListOf<Strategy>()
    private var listener: OnStrategyDeletedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_my_strategies, container, false)

        // Configurar el RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewMyStrategies)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inicializar el adaptador
        adapter = SimpleStrategyAdapter(
            strategies,
            onItemClick = { strategy -> openStrategyDetailFragment(strategy) },
            onDeleteClick = { strategy -> showDeleteConfirmationDialog(strategy) },
            onEditClick = { strategy -> showEditConfirmationDialog(strategy) }
        )
        recyclerView.adapter = adapter

        // Obtener el ID del usuario autenticado
        userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return view
        }

        loadUserAliasAndStrategies()
        return view
    }

    private fun loadUserAliasAndStrategies() {
        db.collection("users").document(userId!!)
            .get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    userAlias = userDocument.getString("alias") ?: "Anónimo"
                }
                loadUserStrategies()
            }
            .addOnFailureListener {
                loadUserStrategies()
            }
    }

    private fun loadUserStrategies() {
        recyclerView.visibility = View.INVISIBLE // Ocultar RecyclerView inicialmente

        db.collection("strategies")
            .whereEqualTo("createdBy", userId)
            .get()
            .addOnSuccessListener { result ->
                strategies.clear()
                strategies.addAll(result.map { document ->
                    Strategy(
                        id = document.id,
                        title = document.getString("title") ?: "Sin título",
                        description = document.getString("description") ?: "Sin descripción",
                        author = userAlias,
                        avatarName = document.getString("avatarName"),
                        avatarUrl = document.getString("avatarUrl"),

                        rating = document.getDouble("rating") ?: 0.0,
                        createdBy = document.getString("createdBy") ?: "",
                        indicators = (document.get("indicators") as? List<*>)?.filterIsInstance<String>()
                            ?: emptyList(),
                        timeframes = (document.get("timeframes") as? List<*>)?.filterIsInstance<String>()
                            ?: emptyList(),
                        tradingStyles = (document.get("tradingStyles") as? List<*>)?.filterIsInstance<String>()
                            ?: emptyList(),
                        symbols = (document.get("symbols") as? List<*>)?.filterIsInstance<String>()
                            ?: emptyList(),
                        algorithmCode = document.getString("algorithmCode") ?: ""
                    )
                })
                adapter.notifyDataSetChanged()

                // Retrasar la visibilidad del RecyclerView para una transición suave
                recyclerView.postDelayed({
                    recyclerView.visibility = View.VISIBLE
                }, 300)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar estrategias", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openStrategyDetailFragment(strategy: Strategy) {
        val fragmentContainer = requireActivity().findViewById<FragmentContainerView>(R.id.fragmentContainer)

        // Ocultar RecyclerView y fragmentContainer antes de la transición
        recyclerView.visibility = View.INVISIBLE
        fragmentContainer?.visibility = View.INVISIBLE

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
                putString("algorithmCode", strategy.algorithmCode)
                putStringArray("strategySymbols", strategy.symbols.toTypedArray())
            }
        }

        // Iniciar la transacción del fragmento con animaciones
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            )
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()

        // Retrasar la visibilidad del contenedor del fragmento
        fragmentContainer?.postDelayed({
            fragmentContainer.visibility = View.VISIBLE
        }, 300)
    }

    private fun showDeleteConfirmationDialog(strategy: Strategy) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Estrategia")
            .setMessage("¿Deseas borrar tu estrategia?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteStrategy(strategy)
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }

    private fun showEditConfirmationDialog(strategy: Strategy) {
        AlertDialog.Builder(requireContext())
            .setTitle("Editar Estrategia")
            .setMessage("¿Deseas editar tu estrategia?")
            .setPositiveButton("Editar") { _, _ ->
                openEditStrategyActivity(strategy)
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }

    private fun openEditStrategyActivity(strategy: Strategy) {
        val intent = Intent(requireContext(), RegisterStrategyActivity::class.java).apply {
            putExtra("strategyId", strategy.id)
            putExtra("strategyTitle", strategy.title)
            putExtra("strategyDescription", strategy.description)
            putExtra("strategyTradingStyles", strategy.tradingStyles.toTypedArray())
            putExtra("strategyIndicators", strategy.indicators.toTypedArray())
            putExtra("strategyTimeframes", strategy.timeframes.toTypedArray())
            putExtra("strategySymbols", strategy.symbols.toTypedArray())
            putExtra("algorithmCode", strategy.algorithmCode)
            putExtra("entryConditionImageUrl", strategy.entryConditionImageUrl)
            putExtra("exitConditionImageUrl", strategy.exitConditionImageUrl)
        }
        // Iniciar la actividad con una animación personalizada
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun deleteStrategy(strategy: Strategy) {
        db.collection("strategies").document(strategy.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Estrategia eliminada", Toast.LENGTH_SHORT).show()

                strategies.remove(strategy)
                adapter.notifyDataSetChanged()
                listener?.onStrategyDeleted()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al eliminar estrategia", Toast.LENGTH_SHORT).show()
            }
    }

    interface OnStrategyDeletedListener {
        fun onStrategyDeleted()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnStrategyDeletedListener) {
            listener = context
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserStrategies()
    }
}




