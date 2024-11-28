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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Strategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import strategycards.RegisterStrategyActivity

class MyStrategiesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimpleStrategyAdapter // Instancia global del adaptador
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null
    private var userAlias: String = "Anónimo" // Alias por defecto
    private val strategies = mutableListOf<Strategy>() // Lista local sincronizada
    private var listener: OnStrategyDeletedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_my_strategies, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewMyStrategies)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inicializa el adaptador con la lista vacía
        adapter = SimpleStrategyAdapter(
            strategies, requireActivity(),
            onDeleteClick = { strategy -> showDeleteConfirmationDialog(strategy) },
            onEditClick = { strategy -> showEditConfirmationDialog(strategy) }
        )
        recyclerView.adapter = adapter

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
        db.collection("strategies")
            .whereEqualTo("createdBy", userId)
            .get()
            .addOnSuccessListener { result ->
                strategies.clear() // Limpia la lista local
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
                            ?: emptyList()
                    )
                })
                adapter.notifyDataSetChanged() // Notifica al adaptador que los datos han cambiado
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar estrategias", Toast.LENGTH_SHORT).show()
            }
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
        }
        startActivity(intent)
    }

    private fun deleteStrategy(strategy: Strategy) {
        db.collection("strategies").document(strategy.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Estrategia eliminada", Toast.LENGTH_SHORT).show()

                // Actualiza la lista local y el adaptador
                strategies.remove(strategy) // Elimina de la lista local
                adapter.notifyDataSetChanged() // Notifica al adaptador del cambio
                listener?.onStrategyDeleted() // Notifica al MainActivity
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
        loadUserStrategies() // Recarga las estrategias al volver al fragmento
    }
}





