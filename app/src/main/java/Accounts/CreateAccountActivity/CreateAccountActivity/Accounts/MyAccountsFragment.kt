import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Account
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyAccountsFragment : Fragment() {

    private lateinit var accountsRecyclerView: RecyclerView
    private lateinit var myAccountsAdapter: MyAccountsAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_accounts, container, false)

        // Configura el RecyclerView
        accountsRecyclerView = view.findViewById(R.id.myAccountsRecyclerView)
        accountsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inicializa el adaptador
        myAccountsAdapter = MyAccountsAdapter(
            accounts = emptyList(),
            onEditClicked = { account -> editAccount(account) },
            onDeleteClicked = { account -> deleteAccount(account) }
        )
        accountsRecyclerView.adapter = myAccountsAdapter

        // Carga las cuentas desde Firestore
        fetchAccounts()

        return view
    }

    private fun fetchAccounts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("accounts").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { documents ->
                val accounts = documents.map { it.toObject(Account::class.java) }
                myAccountsAdapter = MyAccountsAdapter(
                    accounts = accounts,
                    onEditClicked = { account -> editAccount(account) },
                    onDeleteClicked = { account -> deleteAccount(account) }
                )
                accountsRecyclerView.adapter = myAccountsAdapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al cargar cuentas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editAccount(account: Account) {
        Toast.makeText(context, "Editar cuenta: ${account.name}", Toast.LENGTH_SHORT).show()
        // Agrega lógica para editar
    }

    private fun deleteAccount(account: Account) {
        db.collection("accounts").document(account.id).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Cuenta eliminada", Toast.LENGTH_SHORT).show()
                fetchAccounts()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al eliminar cuenta: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

