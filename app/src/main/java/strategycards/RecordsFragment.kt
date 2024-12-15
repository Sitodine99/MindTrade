import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Movement
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RecordsFragment : Fragment() {

    private lateinit var recordsRecyclerView: RecyclerView
    private lateinit var noRecordsTextView: TextView
    private lateinit var recordsAdapter: RecordsAdapter
    private val recordsList = mutableListOf<Movement>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para el fragmento
        return inflater.inflate(R.layout.fragment_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias a las vistas
        recordsRecyclerView = view.findViewById(R.id.movementsRecyclerView)
        noRecordsTextView = view.findViewById(R.id.noMovementsTextView)

        // Configurar el Adapter y RecyclerView
        recordsAdapter = RecordsAdapter(requireContext(), recordsList)
        recordsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        recordsRecyclerView.adapter = recordsAdapter

        // Obtener el strategyId desde los argumentos
        val strategyId = arguments?.getString("STRATEGY_ID") ?: return
        loadRecords(strategyId)
    }

    private fun loadRecords(strategyId: String) {
        db.collectionGroup("movements")
            .whereEqualTo("strategyId", strategyId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                // Usamos un conjunto temporal para evitar duplicados
                val uniqueRecords = mutableSetOf<String>() // Usamos IDs para identificar duplicados
                val tempList = mutableListOf<Movement>() // Lista temporal para nuevos registros

                for (doc in snapshot.documents) {
                    val record = doc.toObject(Movement::class.java)
                    record?.let {
                        if (!uniqueRecords.contains(it.id)) { // Si el ID no está ya agregado
                            uniqueRecords.add(it.id)
                            tempList.add(it)
                            Log.d("RecordsFragment", "Registro cargado: ${it.symbol}, Profit: ${it.profit}")
                        } else {
                            Log.d("RecordsFragment", "Registro duplicado omitido: ${it.id}")
                        }
                    }
                }

                // Actualizamos la lista principal solo con los nuevos registros únicos
                recordsList.clear()
                recordsList.addAll(tempList)
                updateUI()
            }
            .addOnFailureListener { e ->
                recordsList.clear()
                updateUI()
                Log.e("RecordsFragment", "Error al cargar los registros", e)
                noRecordsTextView.text = "Error al cargar los registros: ${e.message}"
            }
    }


    private fun updateUI() {
        if (recordsList.isEmpty()) {
            noRecordsTextView.visibility = View.VISIBLE
            recordsRecyclerView.visibility = View.GONE
        } else {
            noRecordsTextView.visibility = View.GONE
            recordsRecyclerView.visibility = View.VISIBLE
            recordsAdapter.notifyDataSetChanged() // Notificar al Adapter
        }
    }

    companion object {
        // Método para crear una nueva instancia con strategyId como argumento
        fun newInstance(strategyId: String): RecordsFragment {
            val fragment = RecordsFragment()
            val args = Bundle()
            args.putString("STRATEGY_ID", strategyId)
            fragment.arguments = args
            return fragment
        }
    }
}
