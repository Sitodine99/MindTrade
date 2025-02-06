import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Account
import com.example.mindtrade.model.Movement
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class MyAccountsAdapter(
    private var accounts: List<Account>,
    private val onEditClicked: (Account) -> Unit,
    private val onDeleteClicked: (Account) -> Unit,
    private val onAccountClick: (Account) -> Unit
) : RecyclerView.Adapter<MyAccountsAdapter.MyAccountsViewHolder>() {

    class MyAccountsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val accountName: TextView = view.findViewById(R.id.accountTitleTextView)
        val accountProfitability: TextView = view.findViewById(R.id.accountProfitabilityTextView)
        val accountOperationsCount: TextView = view.findViewById(R.id.accountOperationsCountTextView)
        val accountCreationDate: TextView = view.findViewById(R.id.accountCreationDateTextView) // ✅ Agregado
        val editIcon: ImageView = view.findViewById(R.id.editIcon)
        val deleteIcon: ImageView = view.findViewById(R.id.deleteIcon)
        val container: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAccountsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_accounts, parent, false)
        return MyAccountsViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyAccountsViewHolder, position: Int) {
        val account = accounts[position]
        val context = holder.itemView.context

        holder.accountName.text = account.name

        // ✅ Formatear y mostrar la fecha de creación
        val creationDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Date(account.createdAt))
        holder.accountCreationDate.text = "Creado: $creationDate"

        // Mostrar número de operaciones
        val operationsCount = account.movements.size
        holder.accountOperationsCount.text = "Operaciones: $operationsCount"

        // Calcular y mostrar la rentabilidad
        calculateProfitability(context, account) { profitability ->
            holder.accountProfitability.text = "Rentabilidad: ${String.format("%.2f", profitability)}%"

            if (profitability >= 0) {
                holder.accountProfitability.setTextColor(
                    ContextCompat.getColor(context, android.R.color.holo_green_dark)
                )
            } else {
                holder.accountProfitability.setTextColor(
                    ContextCompat.getColor(context, android.R.color.holo_red_dark)
                )
            }
        }

        // Configurar clics en editar y eliminar
        holder.editIcon.setOnClickListener { onEditClicked(account) }
        holder.deleteIcon.setOnClickListener { onDeleteClicked(account) }
        holder.container.setOnClickListener { onAccountClick(account) }
    }

    override fun getItemCount(): Int = accounts.size

    fun updateAccounts(newAccounts: List<Account>) {
        accounts = newAccounts
        notifyDataSetChanged()
    }

    private fun calculateProfitability(
        context: android.content.Context,
        account: Account,
        onCalculated: (Double) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val sharedPreferences =
            context.getSharedPreferences("MindTradePrefs", android.content.Context.MODE_PRIVATE)
        val initialBalance =
            sharedPreferences.getFloat("initialBalance_${account.id}", -1f).toDouble()

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

                val currentBalance = initialBalance + totalProfit
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
