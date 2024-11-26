package com.example.mindtrade.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mindtrade.R
import com.example.mindtrade.StrategyDetailActivity
import com.example.mindtrade.model.Strategy

class StrategyAdapter(
    private val strategies: List<Strategy>,
    private val onItemClick: (Strategy) -> Unit
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

        // Mapea avatarName a recursos locales
        val avatarResId = getAvatarResource(strategy.avatarName ?: "default_avatar")
        Glide.with(holder.avatarImageView.context)
            .load(avatarResId)
            .circleCrop()
            .into(holder.avatarImageView)

        // Manejar clics en la estrategia
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, StrategyDetailActivity::class.java)
            intent.putExtra("strategyId", strategy.id)
            intent.putExtra("strategyTitle", strategy.title)
            intent.putExtra("strategyDescription", strategy.description)
            intent.putExtra("strategyAuthor", strategy.author)
            intent.putExtra("tradingStyles", strategy.tradingStyles.toTypedArray())
            intent.putExtra(
                "strategyAvatarName",
                strategy.avatarName
            ) // Pasar avatarName directamente
            intent.putExtra(
                "strategyIndicators",
                strategy.indicators.toTypedArray()
            ) // Convertir a Array
            intent.putExtra(
                "strategyTimeframes",
                strategy.timeframes.toTypedArray()
            ) // Convertir a Array
            intent.putExtra("strategyRating", strategy.rating)
            holder.itemView.context.startActivity(intent)
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
