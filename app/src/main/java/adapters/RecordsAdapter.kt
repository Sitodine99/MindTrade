import android.content.Context
import android.graphics.Typeface
import android.text.Html
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

        // **✅ Obtener el estilo de trading con su color original**
        val tradingStyle = determineTradingStyle(record.entryTime, record.exitTime)
        val tradingStyleColor = when (tradingStyle) {
            "Scalping" -> ContextCompat.getColor(context, R.color.orange)
            "Day Trading" -> ContextCompat.getColor(context, R.color.blue_normal)
            "Swing Trading" -> ContextCompat.getColor(context, R.color.forest_green)
            else -> ContextCompat.getColor(context, R.color.black)
        }

        // **✅ Configurar el color de "Buy" en azul y "Sell" en rojo sin afectar estilos de trading**
        val typeColor = if (record.type.equals("Buy", ignoreCase = true)) {
            ContextCompat.getColor(context, R.color.blue_normal) // Azul para Buy
        } else {
            ContextCompat.getColor(context, R.color.my_red) // Rojo para Sell
        }

        // **✅ Aplicar colores a Buy/Sell y al estilo de trading por separado**
        val formattedTypeText = Html.fromHtml(
            "<font color=${typeColor}>${record.type}, ${record.lotes}</font> - " +
                    "<font color=${tradingStyleColor}>$tradingStyle</font>"
        )
        holder.typeTextView.text = formattedTypeText
        holder.typeTextView.setTypeface(null, Typeface.BOLD)

        // **✅ Configurar la vista colapsada**
        holder.symbolTextView.text = record.symbol
        holder.entryPriceTextView.text = record.entryPrice.toString()
        holder.exitPriceTextView.text = record.exitPrice?.toString() ?: "N/A"
        holder.profitTextView.text = "${record.profit ?: "N/A"} $"

        // **✅ Configurar el color del beneficio**
        val isPositive = record.profit ?: 0.0 >= 0
        holder.profitTextView.setTextColor(
            if (isPositive) ContextCompat.getColor(context, R.color.forest_green)
            else ContextCompat.getColor(context, R.color.my_red)
        )

        // **✅ Vista expandida con negritas en los títulos**
        holder.entryDateTextView.text = Html.fromHtml("<b>Fecha de entrada:</b> ${formatDate(record.entryTime)}")
        holder.exitDateTextView.text = Html.fromHtml("<b>Fecha de salida:</b> ${formatDate(record.exitTime)}")
        holder.commentsTextView.text = Html.fromHtml("<b>Comentarios:</b> ${record.comments ?: "Sin comentarios"}")

        // **✅ Configuración del estado emocional y emoción**
        val emotionalState = record.emotionalState ?: "N/A"
        val emotion = record.emotion ?: "Sin emoción"

        holder.emotionalStateTextView.text = emotionalState
        holder.emotionTextView.text = emotion

        // **✅ Aplicar color y estilo según el estado emocional**
        if (emotionalState.equals("Psico +", ignoreCase = true)) {
            holder.emotionalStateTextView.setTextColor(ContextCompat.getColor(context, R.color.forest_green))
            holder.emotionalStateTextView.setTypeface(null, Typeface.BOLD)
        } else if (emotionalState.equals("Psico -", ignoreCase = true)) {
            holder.emotionalStateTextView.setTextColor(ContextCompat.getColor(context, R.color.my_red))
            holder.emotionalStateTextView.setTypeface(null, Typeface.BOLD)
        }

        // **✅ Lista de emociones positivas**
        val positiveEmotions = listOf(
            "Autocontrol", "Confianza", "Eficiencia", "Optimismo", "Paciencia",
            "Realización", "Satisfacción", "Seguridad", "Sintonía", "Tranquilidad",
            "Aceptación", "Afirmación"
        )
        val isPositiveEmotion = positiveEmotions.contains(emotion)

        // **✅ Aplicar color y negrita a la emoción**
        if (isPositiveEmotion) {
            holder.emotionTextView.setTextColor(ContextCompat.getColor(context, R.color.forest_green))
            holder.emotionTextView.setTypeface(null, Typeface.BOLD)
        } else {
            holder.emotionTextView.setTextColor(ContextCompat.getColor(context, R.color.my_red))
            holder.emotionTextView.setTypeface(null, Typeface.BOLD)
        }

        // **✅ Expandir/colapsar vista**
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

    private fun determineTradingStyle(entryTime: Long, exitTime: Long?): String {
        if (exitTime == null) return "Desconocido"

        val durationInMinutes = (exitTime - entryTime) / (1000 * 60)

        return when {
            durationInMinutes < 10 -> "Scalping"
            durationInMinutes < 1440 -> "Day Trading"
            else -> "Swing Trading"
        }
    }
}
