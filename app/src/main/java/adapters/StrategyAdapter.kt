package adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mindtrade.R
import com.example.mindtrade.model.Strategy

class StrategyAdapter(
    private val strategies: List<Strategy>,
    private val onItemClick: (Strategy) -> Unit // Se usa esta función para manejar clics
) : RecyclerView.Adapter<StrategyAdapter.StrategyViewHolder>() {

    class StrategyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatarImageView: ImageView = view.findViewById(R.id.avatarImageView)
        val strategyTitleTextView: TextView = view.findViewById(R.id.strategyTitleTextView)
        val strategyAuthorTextView: TextView = view.findViewById(R.id.strategyAuthorTextView)
        val strategyRatingBar: RatingBar = view.findViewById(R.id.strategyRatingBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StrategyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_strategy, parent, false)
        return StrategyViewHolder(view)
    }

    override fun onBindViewHolder(holder: StrategyViewHolder, position: Int) {
        val strategy = strategies[position]
        holder.strategyTitleTextView.text = strategy.title
        holder.strategyAuthorTextView.text = "Por: ${strategy.author}"

        // Asignar el valor al RatingBar
        holder.strategyRatingBar.rating = strategy.rating.toFloat()

        // Cargar avatar del autor de la estrategia
        if (!strategy.avatarUrl.isNullOrEmpty() && strategy.avatarUrl.startsWith("https://")) {
            Glide.with(holder.avatarImageView.context)
                .load(strategy.avatarUrl)
                .placeholder(R.drawable.interrogacion)
                .error(R.drawable.interrogacion)
                .circleCrop()
                .into(holder.avatarImageView)
        } else {
            val avatarResId = getAvatarResource(strategy.avatarName ?: "default_avatar")
            Glide.with(holder.avatarImageView.context)
                .load(avatarResId)
                .circleCrop()
                .into(holder.avatarImageView)
        }

        // Manejar clics mediante onItemClick
        holder.itemView.setOnClickListener {
            onItemClick(strategy)
        }
    }



    // Mapea avatarName a recursos drawable
    private fun getAvatarResource(avatarName: String): Int {
        return when (avatarName) {
            "avatar_hombre" -> R.drawable.avatarhombre
            "avatar_mujer" -> R.drawable.avatarmujer
            "avatar_bebe" -> R.drawable.avatarbebe
            "avatar_alien" -> R.drawable.avataralien
            "avatar_frankenstein" -> R.drawable.avatarfrankenstein
            "avatar_lobo" -> R.drawable.avatarlobo
            "avatar_vampira" -> R.drawable.avatarvampira
            else -> R.drawable.ic_placeholder
        }
    }

    override fun getItemCount(): Int = strategies.size
}