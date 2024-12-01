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

class StrategyWithImageAdapter(
    private val strategies: List<Strategy>,
    private val onItemClick: (Strategy) -> Unit
) : RecyclerView.Adapter<StrategyWithImageAdapter.StrategyWithImageViewHolder>() {

    class StrategyWithImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatarImageView: ImageView = view.findViewById(R.id.avatarImageView)
        val strategyTitleTextView: TextView = view.findViewById(R.id.strategyTitleTextView)
        val strategyAuthorTextView: TextView = view.findViewById(R.id.strategyAuthorTextView)
        val strategyImageView: ImageView = view.findViewById(R.id.strategyImageView)
        val strategyRatingBar: RatingBar = view.findViewById(R.id.strategyRatingBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StrategyWithImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_strategy_with_image, parent, false)
        return StrategyWithImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: StrategyWithImageViewHolder, position: Int) {
        val strategy = strategies[position]

        holder.strategyTitleTextView.text = strategy.title
        holder.strategyAuthorTextView.text = "Por: ${strategy.author}"
        holder.strategyRatingBar.rating = strategy.rating.toFloat()

        // Cargar avatar del autor
        if (!strategy.avatarUrl.isNullOrEmpty() && strategy.avatarUrl.startsWith("https://")) {
            Glide.with(holder.avatarImageView.context)
                .load(strategy.avatarUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .circleCrop()
                .into(holder.avatarImageView)
        } else {
            holder.avatarImageView.setImageResource(R.drawable.ic_placeholder)
        }

        // Cargar la imagen grande de la estrategia (entry o exit)
        val imageUrl = strategy.entryConditionImageUrl ?: strategy.exitConditionImageUrl
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(holder.strategyImageView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.strategyImageView)
        } else {
            holder.strategyImageView.setImageResource(R.drawable.ic_placeholder)
        }

        // Manejar clic en el elemento
        holder.itemView.setOnClickListener {
            onItemClick(strategy)
        }
    }

    override fun getItemCount(): Int = strategies.size
}
