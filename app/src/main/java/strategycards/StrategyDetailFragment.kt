package strategycards

import adapters.StrategyPagerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.mindtrade.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class StrategyDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_strategy_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val avatarImageView: ImageView = view.findViewById(R.id.avatarImageView)
        val strategyTitleTextView: TextView = view.findViewById(R.id.strategyTitleTextView)
        val strategyAuthorTextView: TextView = view.findViewById(R.id.strategyAuthorTextView)
        val tabLayout: TabLayout = view.findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = view.findViewById(R.id.viewPager)

        val args = arguments
        val strategyTitle = args?.getString("strategyTitle") ?: "Sin título"
        val strategyDescription = args?.getString("strategyDescription") ?: "Sin descripción"
        val strategyAuthor = args?.getString("strategyAuthor") ?: "Anónimo"
        val strategyAvatarName = args?.getString("strategyAvatarName") ?: "default_avatar"
        val strategyAvatarUrl = args?.getString("strategyAvatarUrl")
        val strategyIndicators = args?.getStringArray("strategyIndicators") ?: arrayOf("Sin indicadores")
        val strategyTimeframes = args?.getStringArray("strategyTimeframes") ?: arrayOf("Sin temporalidades")
        val tradingStyles = args?.getStringArray("tradingStyles") ?: arrayOf("Sin estilos")
        val strategyRating = args?.getDouble("strategyRating") ?: 0.0

        strategyTitleTextView.text = strategyTitle
        strategyAuthorTextView.text = "Por: $strategyAuthor"

        loadAvatar(strategyAvatarUrl, strategyAvatarName, avatarImageView)

        val adapter = StrategyPagerAdapter(requireActivity())
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

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
    }

    private fun loadAvatar(avatarUrl: String?, avatarName: String, imageView: ImageView) {
        if (!avatarUrl.isNullOrEmpty()) {
            Glide.with(this).load(avatarUrl).circleCrop().into(imageView)
        } else {
            val avatarResId = when (avatarName) {
                "avatar_hombre" -> R.drawable.avatarhombre
                "avatar_mujer" -> R.drawable.avatarmujer
                "avatar_bebe" -> R.drawable.avatarbebe
                "avatar_alien" -> R.drawable.avataralien
                "avatar_frankenstein" -> R.drawable.avatarfrankenstein
                "avatar_lobo" -> R.drawable.avatarlobo
                "avatar_vampira" -> R.drawable.avatarvampira
                else -> R.drawable.ic_placeholder
            }
            Glide.with(this).load(avatarResId).circleCrop().into(imageView)
        }
    }
}
