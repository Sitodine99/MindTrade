import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Account

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
        holder.accountName.text = account.name
        holder.accountBalance.text = "Balance: $${account.balance}"

        // Formatear la fecha de creación
        val creationDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(account.createdAt))
        holder.accountCreationDate.text = "Creado: $creationDate"

        // Rentabilidad por defecto es 0%
        val profitability = calculateProfitability(account)
        holder.accountProfitability.text = "Rentabilidad: $profitability%"

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

    private fun calculateProfitability(account: Account): Double {
        // Simulación: Puedes reemplazar esto con una fórmula real basada en los movimientos
        return 0.0
    }
}


