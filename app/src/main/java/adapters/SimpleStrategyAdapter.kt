package adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Strategy

class SimpleStrategyAdapter(
    private val strategies: List<Strategy>,
    private val onItemClick: (Strategy) -> Unit, // Callback para manejar clics
    private val onDeleteClick: (Strategy) -> Unit, // Callback para manejar eliminación
    private val onEditClick: (Strategy) -> Unit // Callback para manejar edición
) : RecyclerView.Adapter<SimpleStrategyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val strategyTitle: TextView = view.findViewById(R.id.strategyTitleTextView)
        val editIcon: ImageView = view.findViewById(R.id.editIcon)
        val deleteIcon: ImageView = view.findViewById(R.id.deleteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_strategies, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val strategy = strategies[position]

        // Asignar datos
        holder.strategyTitle.text = strategy.title

        // Manejar clic en el ítem
        holder.itemView.setOnClickListener {
            onItemClick(strategy)
        }

        // Manejar clic en el ícono de edición
        holder.editIcon.setOnClickListener {
            onEditClick(strategy)
        }

        // Manejar clic en el ícono de eliminación
        holder.deleteIcon.setOnClickListener {
            onDeleteClick(strategy)
        }
    }

    override fun getItemCount(): Int = strategies.size
}


