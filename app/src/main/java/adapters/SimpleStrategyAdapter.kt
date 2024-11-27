package adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Strategy
import strategycards.StrategyDetailActivity

class SimpleStrategyAdapter(private val strategies: List<Strategy>) :
    RecyclerView.Adapter<SimpleStrategyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val strategyTitle: TextView = view.findViewById(R.id.strategyTitleTextView)
        val editIcon: ImageView = view.findViewById(R.id.editIcon)
        val deleteIcon: ImageView = view.findViewById(R.id.deleteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simple_strategy, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val strategy = strategies[position]

        // Asignar datos
        holder.strategyTitle.text = strategy.title

        // Evento de clic en todo el elemento para redirigir a StrategyDetailActivity
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, StrategyDetailActivity::class.java)
            intent.putExtra("strategyTitle", strategy.title)
            intent.putExtra("strategyDescription", strategy.description)
            intent.putExtra("strategyAuthor", strategy.author)
            intent.putExtra("strategyAvatarName", strategy.avatarName)
            intent.putExtra("strategyAvatarUrl", strategy.avatarUrl)
            intent.putExtra("strategyIndicators", strategy.indicators.toTypedArray())
            intent.putExtra("strategyTimeframes", strategy.timeframes.toTypedArray())
            intent.putExtra("tradingStyles", strategy.tradingStyles.toTypedArray())
            intent.putExtra("strategyRating", strategy.rating)
            context.startActivity(intent)
        }

        // Configurar eventos de clic para íconos (opcional)
        holder.editIcon.setOnClickListener {
            // Implementar funcionalidad de edición si es necesario
        }

        holder.deleteIcon.setOnClickListener {
            // Implementar funcionalidad de eliminación si es necesario
        }
    }

    override fun getItemCount(): Int = strategies.size
}

