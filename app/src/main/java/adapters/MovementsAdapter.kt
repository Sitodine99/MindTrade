import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mindtrade.R
import com.example.mindtrade.model.Movement
import com.google.firebase.firestore.FirebaseFirestore


class MovementsAdapter(
    private val context: Context,
    private val movements: MutableList<Movement>,
    private val listener: MovementActionListener

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
        val firstEmotionTextView: TextView = view.findViewById(R.id.firstEmotionTextView)



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

        // Configuración del texto y datos del movimiento...
        holder.typeTextView.text = "${movement.type}, ${movement.lotes}"

        // Configuración del texto y datos del movimiento...
        holder.typeTextView.text = "${movement.type}, ${movement.lotes}"
        // Resto del código existente para configurar las vistas...

        // Agregar el evento de pulsación larga
        holder.itemView.setOnLongClickListener {
            val options = arrayOf("Eliminar movimiento", "Ajustar beneficio")
            AlertDialog.Builder(context)
                .setTitle("Opciones del movimiento")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> listener.confirmDeleteMovement(position) // Llama a la interfaz
                        1 -> listener.showAdjustProfitDialog(position) // Llama a la interfaz
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }



        // Tipo (Buy/Sell) y lotes
        holder.typeTextView.text = "${movement.type}, ${movement.lotes}"
        holder.typeTextView.setTextColor(
            if (movement.type == "Buy") ContextCompat.getColor(context, R.color.blue_normal)
            else ContextCompat.getColor(context, R.color.my_red)
        )
        holder.symbolTextView.text = movement.symbol
        holder.entryPriceTextView.text = movement.entryPrice.toString()
        holder.exitPriceTextView.text = movement.exitPrice?.toString() ?: "N/A"
        holder.swapTextView.text = movement.swap.toString()
        holder.commissionTextView.text = movement.commission.toString()
        holder.profitTextView.text = movement.profit?.toString() ?: "N/A"
        holder.profitTextView.setTextColor(
            if (movement.profit >= 0) ContextCompat.getColor(context, R.color.blue_normal)
            else ContextCompat.getColor(context, R.color.my_red)
        )

        // Configuración de la emoción
        val emotion = movement.emotion ?: "Sin emoción" // Asigna "Sin emoción" si es nulo
        holder.firstEmotionTextView.text = emotion

        // Lista de emociones positivas y negativas
        val positiveEmotions = listOf("Autocontrol", "Confianza", "Eficiencia", "Optimismo", "Paciencia",
            "Realización", "Satisfacción", "Seguridad", "Sintonía", "Tranquilidad",
            "Aceptación", "Afirmación")
        val isPositiveEmotion = positiveEmotions.contains(emotion)

        // Aplicar color y estilo según la emoción en la vista colapsada
        if (isPositiveEmotion) {
            holder.firstEmotionTextView.setTextColor(ContextCompat.getColor(context, R.color.forest_green)) // Azul para positivo
            holder.firstEmotionTextView.setTypeface(null, Typeface.BOLD)
        } else {
            holder.firstEmotionTextView.setTextColor(ContextCompat.getColor(context, R.color.my_red)) // Rojo para negativo
            holder.firstEmotionTextView.setTypeface(null, Typeface.BOLD)
        }

        // Cambiar el color y estilo según la emoción
        if (isPositiveEmotion) {
            holder.emotionTextView.setTextColor(ContextCompat.getColor(context, R.color.forest_green)) // Azul para positivo
            holder.emotionTextView.setTypeface(null, Typeface.BOLD)
        } else {
            holder.emotionTextView.setTextColor(ContextCompat.getColor(context, R.color.my_red)) // Rojo para negativo
            holder.emotionTextView.setTypeface(null, Typeface.BOLD)
        }



        // Vista expandida
        holder.entryDateTextView.text = Html.fromHtml("<b>Fecha de entrada:</b> ${formatDate(movement.entryTime)}")
        holder.exitDateTextView.text = Html.fromHtml("<b>Fecha de salida:</b> ${formatDate(movement.exitTime)}")

        holder.tradingStyleTextView.text = Html.fromHtml("<b>Estilo:</b> ${determineTradingStyle(movement.entryTime, movement.exitTime)}")
        holder.emotionalStateTextView.text = "${movement.emotionalState}"
        holder.emotionTextView.text = "${movement.emotion}"
        holder.commentsTextView.text = Html.fromHtml("<b>Comentario:</b> ${movement.comments ?: "Sin comentarios"}")

        // Cambiar color según el estado emocional
        val emotionalState = movement.emotionalState ?: "Desconocido"
        if (emotionalState.equals("Psico+", ignoreCase = true)) {
            holder.emotionalStateTextView.setTextColor(ContextCompat.getColor(context, R.color.forest_green)) // Verde para Psico+
            holder.emotionalStateTextView.setTypeface(null, Typeface.BOLD) // Negrita
        } else if (emotionalState.equals("Psico-", ignoreCase = true)) {
            holder.emotionalStateTextView.setTextColor(ContextCompat.getColor(context, R.color.my_red)) // Rojo para Psico-
            holder.emotionalStateTextView.setTypeface(null, Typeface.BOLD) // Negrita
        }

        // Obtener el estilo de trading
        val tradingStyle = determineTradingStyle(movement.entryTime, movement.exitTime)

// Determinar el color según el estilo
        val colorRes = when (tradingStyle) {
            "Scalping" -> R.color.orange // Naranja para Scalping
            "Intradia" -> R.color.blue_normal // Azul para Intradia
            // Verde para Swing Trading
            else -> R.color.forest_green
        }

        // Asignar el estilo y color al TextView
        holder.tradingStyleTextView.text = tradingStyle
        holder.tradingStyleTextView.setTextColor(ContextCompat.getColor(context, colorRes))
        holder.tradingStyleTextView.setTypeface(null, Typeface.BOLD) // Aplicar negrita

        // Mostrar el enlace "Ver fotos" únicamente si el usuario ha subido imágenes válidas
        val validPhotos = movement.photos?.filter { it.isNotEmpty() && it != "DEFAULT_IMAGE_URL" } ?: emptyList()

        if (validPhotos.isNotEmpty()) {
            holder.photosLinkTextView.visibility = View.VISIBLE
            holder.photosLinkTextView.setOnClickListener {
                showPhotosDialog(validPhotos) // Mostrar el diálogo con las fotos válidas
            }
        } else {
            holder.photosLinkTextView.visibility = View.GONE // Ocultar el enlace si no hay fotos válidas
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

    // Confirmar eliminación del movimiento
    private fun confirmDeleteMovement(position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Eliminar movimiento")
            .setMessage("¿Estás seguro de que deseas eliminar este movimiento?")
            .setPositiveButton("Sí") { _, _ ->
                deleteMovement(position)
            }
            .setNegativeButton("No", null)
            .show()
    }

    // Eliminar el movimiento
    private fun deleteMovement(position: Int) {
        val movement = movements[position]
        // Eliminar de Firestore o la base de datos
        FirebaseFirestore.getInstance()
            .collection("accounts")
            .document(movement.accountId)
            .collection("movements")
            .document(movement.id)
            .delete()
            .addOnSuccessListener {
                movements.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(context, "Movimiento eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Mostrar un diálogo para ajustar el beneficio
    private fun showAdjustProfitDialog(position: Int) {
        val movement = movements[position]
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_adjust_profit, null)
        val profitEditText = dialogView.findViewById<EditText>(R.id.profitEditText)
        profitEditText.setText(movement.profit?.toString() ?: "")

        AlertDialog.Builder(context)
            .setTitle("Ajustar beneficio")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val newProfit = profitEditText.text.toString().toDoubleOrNull()
                if (newProfit != null) {
                    updateMovementProfit(position, newProfit)
                } else {
                    Toast.makeText(context, "Por favor, introduce un valor válido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Actualizar el beneficio del movimiento
    // Actualizar el beneficio del movimiento
    private fun updateMovementProfit(position: Int, newProfit: Double) {
        val movement = movements[position] // Recuperar el movimiento actual
        FirebaseFirestore.getInstance()
            .collection("accounts")
            .document(movement.accountId)
            .collection("movements")
            .document(movement.id)
            .update("profit", newProfit) // Actualizar el campo "profit" en Firestore
            .addOnSuccessListener {
                movements[position] = movement.copy(profit = newProfit) // Actualizar la lista local
                notifyItemChanged(position) // Notificar al adaptador sobre el cambio
                Toast.makeText(context, "Beneficio actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
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

    interface MovementActionListener {
        fun confirmDeleteMovement(position: Int)
        fun showAdjustProfitDialog(position: Int)
    }


}