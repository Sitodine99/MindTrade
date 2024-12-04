import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Movement


class MovementsAdapter(
    private val context: Context,
    private val movements: List<Movement>
) : RecyclerView.Adapter<MovementsAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Vista colapsada
        val typeTextView: TextView = view.findViewById(R.id.typeTextView)
        val symbolTextView: TextView = view.findViewById(R.id.symbolTextView)
        val entryPriceTextView: TextView = view.findViewById(R.id.entryPriceTextView)
        val exitPriceTextView: TextView = view.findViewById(R.id.exitPriceTextView)
        val swapTextView: TextView = view.findViewById(R.id.swapTextView)
        val commissionTextView: TextView = view.findViewById(R.id.commissionTextView)
        val profitTextView: TextView = view.findViewById(R.id.profitTextView)

        // Vista expandida
        val expandedView: View = view.findViewById(R.id.expandedView)
        val entryDateTextView: TextView = view.findViewById(R.id.entryDateTextView)
        val exitDateTextView: TextView = view.findViewById(R.id.exitDateTextView)
        val tradingStyleTextView: TextView = view.findViewById(R.id.tradingStyleTextView)
        val emotionalStateTextView: TextView = view.findViewById(R.id.emotionalStateTextView)
        val emotionTextView: TextView = view.findViewById(R.id.emotionTextView)
        val commentsTextView: TextView = view.findViewById(R.id.commentsTextView)
        val photosLinkTextView: TextView = view.findViewById(R.id.photosLinkTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movement = movements[position]

        // Vista colapsada
        holder.typeTextView.text = movement.type
        holder.symbolTextView.text = movement.symbol
        holder.entryPriceTextView.text = movement.entryPrice.toString()
        holder.exitPriceTextView.text = movement.exitPrice?.toString() ?: "N/A"
        holder.swapTextView.text = movement.swap.toString()
        holder.commissionTextView.text = movement.commission.toString()
        holder.profitTextView.text = movement.profit?.toString() ?: "N/A"

        // Vista expandida
        holder.entryDateTextView.text = "Fecha entrada: ${formatDate(movement.entryTime)}"
        holder.exitDateTextView.text = "Fecha salida: ${formatDate(movement.exitTime)}"
        holder.tradingStyleTextView.text = "Estilo: ${determineTradingStyle(movement.entryTime, movement.exitTime)}"
        holder.emotionalStateTextView.text = "Estado emocional: ${movement.emotionalState}"
        holder.emotionTextView.text = "Emoción: ${movement.emotion}"
        holder.commentsTextView.text = "Comentario: ${movement.comments ?: "Sin comentarios"}"

        // Mostrar fotos si existen
        if (movement.photos != null && movement.photos.isNotEmpty()) {
            holder.photosLinkTextView.visibility = View.VISIBLE
            holder.photosLinkTextView.setOnClickListener {
                showPhotosDialog(movement.photos)
            }
        } else {
            holder.photosLinkTextView.visibility = View.GONE
        }

        // Expandir/Colapsar al hacer clic
        holder.itemView.setOnClickListener {
            val isExpanded = holder.expandedView.visibility == View.VISIBLE
            holder.expandedView.visibility = if (isExpanded) View.GONE else View.VISIBLE
        }
    }

    private fun formatDate(timestamp: Long?): String {
        return timestamp?.let {
            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(it)
        } ?: "N/A"
    }

    private fun determineTradingStyle(entryTime: Long, exitTime: Long?): String {
        if (exitTime == null) return "Desconocido" // Si no hay tiempo de salida, no se puede calcular

        val durationInMinutes = (exitTime - entryTime) / (1000 * 60) // Diferencia en minutos

        return when {
            durationInMinutes < 10 -> "Scalping"
            durationInMinutes < 1440 -> "Intradia" // Menos de 1 día (1440 minutos)
            else -> "Swing Trading"
        }
    }

    private fun showPhotosDialog(photos: List<String>) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_photos, null)
        val photosRecyclerView = dialogView.findViewById<RecyclerView>(R.id.photosRecyclerView)

        photosRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        photosRecyclerView.adapter = PhotosAdapter(photos)

        AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .show()
    }


    override fun getItemCount() = movements.size
}

