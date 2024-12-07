import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.AccountMovementsActivity
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
            onDeleteClicked = { account -> deleteAccount(account) },
            onAccountClick = { account -> openAccountMovementsActivity(account) } // Callback para clic en cuenta
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
                val accounts = documents.map { doc ->
                    val account = doc.toObject(Account::class.java)
                    account.copy(
                        movements = (doc.get("movements") as? List<String>) ?: emptyList()
                    )
                }
                myAccountsAdapter.updateAccounts(accounts)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al cargar cuentas: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }


    private fun openAccountMovementsActivity(account: Account) {
        val intent = Intent(requireContext(), AccountMovementsActivity::class.java).apply {
            putExtra("accountName", account.name)
            putExtra("accountId", account.id) // ID único de la cuenta
            putExtra("accountBalance", account.balance) // Balance inicial
            putExtra("accountCurrency", account.currency) // Divisa asociada
            putExtra("accountProfitTarget", account.profitTarget ?: 0.0) // Objetivo de beneficio (opcional)
            putExtra("accountMaxDailyLoss", account.maxDailyLoss ?: 0.0) // Pérdida diaria máxima (opcional)
            putExtra("accountCreatedAt", account.createdAt) // Fecha de creación
            putStringArrayListExtra("accountMovements", ArrayList(account.movements)) // Movimientos asociados
            putExtra("accountIsActive", account.isActive) // Estado de la cuenta (activa/inactiva)
        }
        startActivity(intent)
    }


    private fun editAccount(account: Account) {
        // Crear un AlertDialog para editar el nombre de la cuenta
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Editar Cuenta")

        // Agregar un EditText al diálogo con un filtro de longitud
        val input = android.widget.EditText(requireContext())
        input.hint = "Nuevo nombre de la cuenta"
        input.setText(account.name) // Mostrar el nombre actual
        input.filters = arrayOf(android.text.InputFilter.LengthFilter(10)) // Limitar a 10 caracteres
        builder.setView(input)

        // Botón de Confirmar
        builder.setPositiveButton("Guardar") { _, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                // Actualizar el nombre en Firestore
                db.collection("accounts").document(account.id)
                    .update("name", newName)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Nombre actualizado", Toast.LENGTH_SHORT).show()
                        fetchAccounts() // Actualizar el RecyclerView
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Error al actualizar el nombre: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón de Cancelar
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        // Mostrar el diálogo
        builder.create().show()
    }

    private fun deleteAccount(account: Account) {
        // Crear el AlertDialog
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Eliminar Cuenta")
        builder.setMessage("¿Deseas eliminar la cuenta \"${account.name}\"?.")

        // Botón para confirmar la eliminación
        builder.setPositiveButton("Aceptar") { _, _ ->
            db.collection("accounts").document(account.id).delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Cuenta eliminada", Toast.LENGTH_SHORT).show()
                    fetchAccounts() // Actualizar las cuentas después de eliminar
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Error al eliminar cuenta: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        // Botón para cancelar
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss() // Cerrar el diálogo
        }

        // Mostrar el diálogo
        builder.create().show()
    }
}



