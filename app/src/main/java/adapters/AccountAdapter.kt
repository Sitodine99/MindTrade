import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Account

class AccountAdapter(private var accounts: List<Account>) :
    RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val accountName: TextView = itemView.findViewById(R.id.accountName)
        val accountBalance: TextView = itemView.findViewById(R.id.accountBalance)
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
    }

    override fun getItemCount(): Int = accounts.size

    fun updateAccounts(newAccounts: List<Account>) {
        accounts = newAccounts
        notifyDataSetChanged() // Refresca los datos
    }

    fun updateSelectedAccounts(selectedAccounts: List<Account>) {
        accounts = selectedAccounts.take(2) // Asegúrate de tomar solo dos cuentas
        notifyDataSetChanged()
    }

}
