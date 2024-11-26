package com.example.mindtrade

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class StrategyDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_strategy_detail)

        // Vincular vistas
        val avatarImageView: ImageView = findViewById(R.id.avatarImageView)
        val strategyTitleTextView: TextView = findViewById(R.id.strategyTitleTextView)
        val strategyAuthorTextView: TextView = findViewById(R.id.strategyAuthorTextView)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        // Obtener datos del Intent
        val strategyTitle = intent.getStringExtra("strategyTitle") ?: "Sin título"
        val strategyDescription = intent.getStringExtra("strategyDescription") ?: "Sin descripción"
        val strategyAuthor = intent.getStringExtra("strategyAuthor") ?: "Anónimo"
        val strategyAvatarName = intent.getStringExtra("strategyAvatarName") ?: "default_avatar"
        val strategyIndicators = intent.getStringArrayExtra("strategyIndicators") ?: arrayOf("Sin indicadores")
        val strategyTimeframes = intent.getStringArrayExtra("strategyTimeframes") ?: arrayOf("Sin temporalidades")
        val tradingStyles = intent.getStringArrayExtra("tradingStyles") ?: arrayOf("Sin estilos")
        val strategyRating = intent.getDoubleExtra("strategyRating", 0.0)

        // Mostrar los datos de la cabecera
        strategyTitleTextView.text = strategyTitle
        strategyAuthorTextView.text = "Por: $strategyAuthor"
        val avatarResId = getAvatarResource(strategyAvatarName)
        Glide.with(this)
            .load(avatarResId)
            .circleCrop()
            .into(avatarImageView)

        // Configurar el ViewPager con los fragments
        val adapter = StrategyPagerAdapter(this)
        adapter.addFragment(GeneralFragment().apply {
            arguments = Bundle().apply {
                putStringArray("tradingStyles", tradingStyles)
                putStringArray("indicators", strategyIndicators)
                putStringArray("timeframes", strategyTimeframes)
                putFloat("rating", strategyRating.toFloat())
            }
        }, "General")

        adapter.addFragment(DescriptionFragment().apply {
            arguments = Bundle().apply {
                putString("description", strategyDescription)
            }
        }, "Descripción")

        viewPager.adapter = adapter

        // Vincular TabLayout con ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
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
