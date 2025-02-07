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

class FavoriteStrategyAdapter(
    private val strategies: List<Strategy>,
    private val onItemClick: (Strategy) -> Unit // Se usa esta función para manejar clics
) : RecyclerView.Adapter<FavoriteStrategyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatarImageView: ImageView = view.findViewById(R.id.avatarImageView)
        val titleTextView: TextView = view.findViewById(R.id.strategyTitleTextView)
        val authorTextView: TextView = view.findViewById(R.id.strategyAuthorTextView)
        val ratingBar: RatingBar = view.findViewById(R.id.strategyRatingBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_strategy, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val strategy = strategies[position]

        holder.titleTextView.text = strategy.title
        holder.authorTextView.text = "Por: ${strategy.author}"
        holder.ratingBar.rating = strategy.rating.toFloat()

        // Verificar si hay un avatarUrl disponible
        if (!strategy.avatarUrl.isNullOrEmpty() && strategy.avatarUrl.startsWith("https://")) {
            // Cargar desde URL
            Glide.with(holder.avatarImageView.context)
                .load(strategy.avatarUrl)
                .placeholder(R.drawable.interrogacion_icon) // Placeholder mientras carga
                .error(R.drawable.interrogacion_icon) // Imagen si hay error
                .circleCrop()
                .into(holder.avatarImageView)
        } else {
            // Si no hay URL, usar el recurso local basado en avatarName
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

    private fun getAvatarResource(avatarName: String): Int {
        return when (avatarName) {
            "avatar_hombre" -> R.drawable.avatarhombre
            "avatar_mujer" -> R.drawable.avatarmujer
            "avatar_bebe" -> R.drawable.avatarbebe
            "avatar_alien" -> R.drawable.avataralien
            "avatar_frankenstein" -> R.drawable.avatarfrankenstein
            "avatar_lobo" -> R.drawable.avatarlobo
            "avatar_vampira" -> R.drawable.avatar_vampira
            else -> R.drawable.ic_placeholder // Recurso predeterminado en caso de que el avatar no coincida
        }
    }



    override fun getItemCount(): Int = strategies.size
}
