import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Movement

class RecordsAdapter(
    private val context: Context,
    private val records: MutableList<Movement>
) : RecyclerView.Adapter<RecordsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Vista colapsada
        val typeTextView: TextView = view.findViewById(R.id.typeTextView)
        val symbolTextView: TextView = view.findViewById(R.id.symbolTextView)
        val entryPriceTextView: TextView = view.findViewById(R.id.entryPriceTextView)
        val exitPriceTextView: TextView = view.findViewById(R.id.exitPriceTextView)
        val profitTextView: TextView = view.findViewById(R.id.profitTextView)

        // Vista expandida
        val expandedView: View = view.findViewById(R.id.expandedView)
        val entryDateTextView: TextView = view.findViewById(R.id.entryDateTextView)
        val exitDateTextView: TextView = view.findViewById(R.id.exitDateTextView)
        val emotionalStateTextView: TextView = view.findViewById(R.id.emotionalStateTextView)
        val emotionTextView: TextView = view.findViewById(R.id.emotionTextView)
        val commentsTextView: TextView = view.findViewById(R.id.commentsTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_movement_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]

        // Configurar las vistas colapsadas
        holder.typeTextView.text = "${record.type}, ${record.lotes}"
        holder.symbolTextView.text = record.symbol
        holder.entryPriceTextView.text = record.entryPrice.toString()
        holder.exitPriceTextView.text = record.exitPrice?.toString() ?: "N/A"
        holder.profitTextView.text = "${record.profit ?: "N/A"} $  "

        // Configurar el color del beneficio
        val isPositive = record.profit ?: 0.0 >= 0
        holder.profitTextView.setTextColor(
            if (isPositive) ContextCompat.getColor(context, R.color.forest_green)
            else ContextCompat.getColor(context, R.color.my_red)
        )

        // Configurar vistas expandidas
        holder.entryDateTextView.text = "Entrada: ${formatDate(record.entryTime)}"
        holder.exitDateTextView.text = "Salida: ${formatDate(record.exitTime)}"
        holder.emotionalStateTextView.text = "Estado emocional: ${record.emotionalState ?: "N/A"}"
        holder.emotionTextView.text = "Emoción: ${record.emotion ?: "N/A"}"
        holder.commentsTextView.text = "Comentarios: ${record.comments ?: "Sin comentarios"}"

        // Expandir/colapsar vista
        holder.itemView.setOnClickListener {
            val isExpanded = holder.expandedView.visibility == View.VISIBLE
            holder.expandedView.visibility = if (isExpanded) View.GONE else View.VISIBLE
        }
    }

    override fun getItemCount(): Int = records.size

    private fun formatDate(timestamp: Long?): String {
        return timestamp?.let {
            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(it)
        } ?: "N/A"
    }
}
