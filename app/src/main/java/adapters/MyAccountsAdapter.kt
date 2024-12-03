import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Account

class MyAccountsAdapter(
    private var accounts: List<Account>, // Cambiado a var para permitir reasignación
    private val onEditClicked: (Account) -> Unit,
    private val onDeleteClicked: (Account) -> Unit
) : RecyclerView.Adapter<MyAccountsAdapter.MyAccountsViewHolder>() {

    class MyAccountsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val accountName: TextView = view.findViewById(R.id.accountTitleTextView)
        val editIcon: ImageView = view.findViewById(R.id.editIcon)
        val deleteIcon: ImageView = view.findViewById(R.id.deleteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAccountsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_accounts, parent, false)
        return MyAccountsViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyAccountsViewHolder, position: Int) {
        val account = accounts[position]
        holder.accountName.text = account.name
        holder.editIcon.setOnClickListener { onEditClicked(account) }
        holder.deleteIcon.setOnClickListener { onDeleteClicked(account) }
    }

    override fun getItemCount(): Int = accounts.size

    fun updateAccounts(newAccounts: List<Account>) {
        accounts = newAccounts
        notifyDataSetChanged() // Notifica cambios al RecyclerView
    }
}

