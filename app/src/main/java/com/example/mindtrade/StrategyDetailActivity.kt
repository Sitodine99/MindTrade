package com.example.mindtrade

import android.os.Bundle
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mindtrade.R

class StrategyDetailActivity : AppCompatActivity() {

    private lateinit var avatarImageView: ImageView
    private lateinit var strategyTitleTextView: TextView
    private lateinit var strategyAuthorTextView: TextView
    private lateinit var strategyDescriptionTextView: TextView
    private lateinit var strategyIndicatorsTextView: TextView
    private lateinit var strategyTimeframesTextView: TextView
    private lateinit var strategyRatingBar: RatingBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_strategy_detail)

        // Vincular vistas
        avatarImageView = findViewById(R.id.avatarImageView)
        strategyTitleTextView = findViewById(R.id.strategyTitleTextView)
        strategyAuthorTextView = findViewById(R.id.strategyAuthorTextView)
        strategyDescriptionTextView = findViewById(R.id.strategyDescriptionTextView)
        strategyIndicatorsTextView = findViewById(R.id.strategyIndicatorsTextView)
        strategyTimeframesTextView = findViewById(R.id.strategyTimeframesTextView)
        strategyRatingBar = findViewById(R.id.strategyRatingBar)

        // Obtener datos del Intent
        val strategyTitle = intent.getStringExtra("strategyTitle") ?: "Sin título"
        val strategyDescription = intent.getStringExtra("strategyDescription") ?: "Sin descripción"
        val strategyAuthor = intent.getStringExtra("strategyAuthor") ?: "Anónimo"
        val strategyAvatarName = intent.getStringExtra("strategyAvatarName") ?: "default_avatar"
        val strategyIndicators = intent.getStringArrayExtra("strategyIndicators") ?: arrayOf()
        val strategyTimeframes = intent.getStringArrayExtra("strategyTimeframes") ?: arrayOf()
        val strategyRating = intent.getDoubleExtra("strategyRating", 0.0)

        // Mostrar los datos en la UI
        strategyTitleTextView.text = strategyTitle
        strategyAuthorTextView.text = "Por: $strategyAuthor"
        strategyDescriptionTextView.text = strategyDescription
        strategyIndicatorsTextView.text = strategyIndicators.joinToString(", ")
        strategyTimeframesTextView.text = strategyTimeframes.joinToString(", ")
        strategyRatingBar.rating = strategyRating.toFloat()

        // Cargar el avatar
        val avatarResId = getAvatarResource(strategyAvatarName)
        Glide.with(this)
            .load(avatarResId)
            .circleCrop()
            .into(avatarImageView)
    }

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
}

