package adapters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Strategy
import strategycards.RegisterStrategyActivity
import strategycards.StrategyDetailFragment

class SimpleStrategyAdapter(
    private val strategies: List<Strategy>,
    private val fragmentActivity: FragmentActivity, // Recibe una instancia de FragmentActivity
    private val onDeleteClick: (Strategy) -> Unit, // Callback para manejar eliminación
    private val onEditClick: (Strategy) -> Unit // Callback para manejar la edición
) : RecyclerView.Adapter<SimpleStrategyAdapter.ViewHolder>() {

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

        // Evento de clic en todo el elemento para redirigir a StrategyDetailFragment
        holder.itemView.setOnClickListener {
            val fragment = StrategyDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("strategyId", strategy.id)
                    putString("strategyTitle", strategy.title)
                    putString("strategyDescription", strategy.description)
                    putString("strategyAuthor", strategy.author)
                    putString("strategyAvatarName", strategy.avatarName)
                    putString("strategyAvatarUrl", strategy.avatarUrl)
                    putStringArray("strategyIndicators", strategy.indicators.toTypedArray())
                    putStringArray("strategyTimeframes", strategy.timeframes.toTypedArray())
                    putStringArray("tradingStyles", strategy.tradingStyles.toTypedArray())
                    putDouble("strategyRating", strategy.rating)
                }
            }

            // Transacción con animaciones
            fragmentActivity.supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.fade_in, // Animación al entrar
                    R.anim.fade_out, // Animación al salir
                    R.anim.fade_in, // Animación al regresar
                    R.anim.fade_out  // Animación al salir al regresar
                )
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }


        // Configurar eventos de clic para íconos (opcional)
        holder.editIcon.setOnClickListener {
            val intent = Intent(fragmentActivity, RegisterStrategyActivity::class.java).apply {
                putExtra("strategyId", strategy.id)
                putExtra("strategyTitle", strategy.title)
                putExtra("strategyDescription", strategy.description)
                putExtra("strategyTradingStyles", strategy.tradingStyles.toTypedArray())
                putExtra("strategyIndicators", strategy.indicators.toTypedArray())
                putExtra("strategyTimeframes", strategy.timeframes.toTypedArray())
                putExtra("strategyAlgorithmCode", strategy.algorithmCode)
            }
            fragmentActivity.startActivity(intent)
            // Implementar funcionalidad de edición si es necesario
        }

        //Eliminar estrategia:
        holder.deleteIcon.setOnClickListener {
            onDeleteClick(strategy)
        }

        holder.editIcon.setOnClickListener{
            onEditClick(strategy)
        }
    }

    override fun getItemCount(): Int = strategies.size
}
