import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Account
import com.example.mindtrade.model.Movement
import com.google.firebase.firestore.FirebaseFirestore

class AccountAdapter(
    private var accounts: List<Account>,
    private val onAccountClick: (Account) -> Unit // Callback para manejar clics
) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val accountName: TextView = itemView.findViewById(R.id.accountName)
        val accountBalance: TextView = itemView.findViewById(R.id.accountBalance)
        val accountCreationDate: TextView = itemView.findViewById(R.id.accountCreationDate)
        val accountProfitability: TextView = itemView.findViewById(R.id.accountProfitability)
        val accountOperations: TextView = itemView.findViewById(R.id.accountOperations)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account, parent, false) // Usa item_account como diseño
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accounts[position]
        val context = holder.itemView.context  // Obtenemos el contexto desde el ViewHolder

        holder.accountName.text = account.name
        holder.accountBalance.text = "Balance: $${String.format("%.2f", account.balance)}"

        // Formatear la fecha de creación
        val creationDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(account.createdAt))
        holder.accountCreationDate.text = "Creado: $creationDate"

        // Ahora pasamos el contexto a calculateProfitability
        calculateProfitability(context, account) { profitability ->
            holder.accountProfitability.text = "Rentabilidad: ${String.format("%.2f", profitability)}%"
            if (profitability >= 0) {
                holder.accountProfitability.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark)
                )
            } else {
                holder.accountProfitability.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark)
                )
            }
        }

        // Mostrar número de operaciones
        holder.accountOperations.text = "Operaciones: ${account.movements.size}"

        // Asignar el listener de clic
        holder.itemView.setOnClickListener {
            onAccountClick(account) // Llamar al callback con la cuenta seleccionada
        }
    }


    override fun getItemCount(): Int = accounts.size

    fun updateAccounts(newAccounts: List<Account>) {
        accounts = newAccounts
        notifyDataSetChanged() // Refresca los datos
    }

    private fun calculateProfitability(
        context: android.content.Context,
        account: Account,
        onCalculated: (Double) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()

        // Recuperamos el balance inicial desde SharedPreferences
        val sharedPreferences =
            context.getSharedPreferences("MindTradePrefs", android.content.Context.MODE_PRIVATE)
        val initialBalance =
            sharedPreferences.getFloat("initialBalance_${account.id}", -1f).toDouble()

        // Si no encontramos un balance inicial guardado, evitamos un cálculo incorrecto
        if (initialBalance == -1.0) {
            onCalculated(0.0)
            return
        }

        firestore.collection("accounts").document(account.id)
            .collection("movements")
            .get()
            .addOnSuccessListener { movementsSnapshot ->
                val totalProfit = movementsSnapshot.documents.sumOf { doc ->
                    val movement = doc.toObject(Movement::class.java)
                    movement?.profit ?: 0.0
                }

                val currentBalance =
                    initialBalance + totalProfit // 🔹 Usamos initialBalance correctamente

                val profitability = if (initialBalance > 0) {
                    ((currentBalance - initialBalance) / initialBalance) * 100
                } else {
                    0.0
                }

                onCalculated(profitability)
            }
            .addOnFailureListener {
                onCalculated(0.0)
            }
    }
}






