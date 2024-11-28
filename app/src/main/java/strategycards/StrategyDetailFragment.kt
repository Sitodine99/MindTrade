package strategycards

import adapters.StrategyPagerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.mindtrade.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

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
        val favoriteIcon: ImageView = view.findViewById(R.id.favoriteIcon) // Inicializar el botón de favoritos
        val tabLayout: TabLayout = view.findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = view.findViewById(R.id.viewPager)

        val args = arguments
        val strategyId = args?.getString("strategyId") ?: "" // Extraer el ID de la estrategia
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

        // Manejar favoritos
        initializeFavoriteButton(strategyId, favoriteIcon)
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
    private fun initializeFavoriteButton(strategyId: String, favoriteIcon: ImageView) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // Comprobar si la estrategia ya es favorita
        db.collection("strategies").document(strategyId).get()
            .addOnSuccessListener { document ->
                val favoritedBy = document.get("favoritedBy") as? List<*>
                val isFavorite = favoritedBy?.contains(userId) == true

                // Actualizar el ícono según el estado
                updateFavoriteIcon(isFavorite, favoriteIcon)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar favoritos", Toast.LENGTH_SHORT).show()
            }

        // Manejar clics en el ícono de favoritos
        favoriteIcon.setOnClickListener {
            db.collection("strategies").document(strategyId).get()
                .addOnSuccessListener { document ->
                    val favoritedBy = document.get("favoritedBy") as? List<*>
                    val isFavorite = favoritedBy?.contains(userId) == true

                    if (isFavorite) {
                        // Quitar de favoritos
                        db.collection("strategies").document(strategyId)
                            .update("favoritedBy", FieldValue.arrayRemove(userId))
                            .addOnSuccessListener {
                                updateFavoriteIcon(false, favoriteIcon)
                                Toast.makeText(requireContext(), "Estrategia eliminada de favoritos", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Error al eliminar de favoritos", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Añadir a favoritos
                        db.collection("strategies").document(strategyId)
                            .update("favoritedBy", FieldValue.arrayUnion(userId))
                            .addOnSuccessListener {
                                updateFavoriteIcon(true, favoriteIcon)
                                Toast.makeText(requireContext(), "Estrategia añadida a favoritos", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Error al añadir a favoritos", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al cargar estrategia", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun updateFavoriteIcon(isFavorite: Boolean, favoriteIcon: ImageView) {
            val iconRes = if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_empty
            favoriteIcon.setImageResource(iconRes)
        }
    }