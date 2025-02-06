package strategycards

import RecordsFragment
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
        val strategyId = args?.getString("strategyId") ?: ""



            // Cargar datos desde Firebase
        val db = FirebaseFirestore.getInstance()
        db.collection("strategies").document(strategyId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val strategyTitle = document.getString("title") ?: "Sin título"
                    val strategyDescription = document.getString("description") ?: "Sin descripción"
                    val strategyAuthor = document.getString("authorAlias") ?: "Anónimo"
                    val strategyAvatarName = document.getString("avatarName") ?: "default_avatar"
                    val strategyAvatarUrl = document.getString("avatarUrl")
                    val strategyIndicators = (document.get("indicators") as? List<*>)?.filterIsInstance<String>()?.toTypedArray() ?: arrayOf("Sin indicadores")
                    val strategyTimeframes = (document.get("timeframes") as? List<*>)?.filterIsInstance<String>()?.toTypedArray() ?: arrayOf("Sin temporalidades")
                    val tradingStyles = (document.get("tradingStyles") as? List<*>)?.filterIsInstance<String>()?.toTypedArray() ?: arrayOf("Sin estilos")
                    val strategyRating = document.getDouble("rating") ?: 0.0
                    val algorithmCode = document.getString("algorithmCode") ?: ""
                    val strategySymbols = (document.get("symbols") as? List<*>)?.filterIsInstance<String>()?.toTypedArray() ?: arrayOf("Sin símbolos")
                    val entryImageUrl = document.getString("entryConditionImageUrl") ?: ""
                    val exitImageUrl = document.getString("exitConditionImageUrl") ?: ""


                    // Actualizar vistas con datos obtenidos de Firebase
                    strategyTitleTextView.text = strategyTitle
                    strategyAuthorTextView.text = "Por: $strategyAuthor"
                    loadAvatar(strategyAvatarUrl, strategyAvatarName, avatarImageView)

                    // Configurar el adapter del ViewPager con los datos actualizados
                    val adapter = StrategyPagerAdapter(requireActivity())

                    // Añade pestaña Datos generales
                    adapter.addFragment(GeneralFragment().apply {
                        arguments = Bundle().apply {
                            putString("strategyId", strategyId)
                            putStringArray("tradingStyles", tradingStyles)
                            putStringArray("indicators", strategyIndicators)
                            putStringArray("timeframes", strategyTimeframes)
                            putFloat("rating", strategyRating.toFloat())
                            putStringArray("symbols", strategySymbols)
                        }
                    }, "General")

                    // Añade pestaña descripción
                    adapter.addFragment(DescriptionFragment().apply {
                        arguments = Bundle().apply {
                            putString("description", strategyDescription)
                            putString("entryConditionImageUrl", entryImageUrl)
                            putString("exitConditionImageUrl", exitImageUrl)
                        }
                    }, "Descripción")

                    // Añade pestaña foro
                    adapter.addFragment(ForumFragment().apply {
                        arguments = Bundle().apply {
                            putString("strategyId", strategyId)
                        }
                    }, "Foro")

                    // Añade pestaña "Registros"
                    adapter.addFragment(RecordsFragment().apply {
                        arguments = Bundle().apply {
                            putString("STRATEGY_ID", strategyId) // Pasar el strategyId al RecordsFragment
                        }
                    }, "Registros")


                    // Añade la pestaña "Trading Algorítmico" solo si el campo no está vacío
                    if (algorithmCode.isNotBlank()) {
                        adapter.addFragment(AlgorithmFragment().apply {
                            arguments = Bundle().apply {
                                putString("algorithmCode", algorithmCode)
                            }
                        }, "Bot")
                    }

                    viewPager.adapter = adapter
                    viewPager.offscreenPageLimit = adapter.itemCount

                    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                        tab.text = adapter.getPageTitle(position)
                    }.attach()
                } else {
                    Toast.makeText(requireContext(), "La estrategia no existe.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al cargar estrategia: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Manejar favoritos
        initializeFavoriteButton(strategyId, favoriteIcon)

    }



    private fun loadAvatar(avatarUrl: String?, avatarName: String, imageView: ImageView) {
        if (!avatarUrl.isNullOrEmpty() && avatarUrl.startsWith("https://")) {
            // Cargar desde URL
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.interrogacion) // Imagen de carga
                .error(R.drawable.interrogacion) // Imagen de error
                .circleCrop()
                .into(imageView)
        } else {
            // Intentar cargar desde recursos locales
            val avatarResId = getAvatarImageResource(avatarName)
            if (avatarResId != null) {
                Glide.with(this)
                    .load(avatarResId) // Recurso local
                    .circleCrop()
                    .into(imageView)
            } else {
                // Usar imagen predeterminada si no se encuentra el recurso local
                Toast.makeText(requireContext(), "Avatar local no encontrado, usando predeterminado", Toast.LENGTH_SHORT).show()
                Glide.with(this)
                    .load(R.drawable.interrogacion)
                    .circleCrop()
                    .into(imageView)
            }
        }
    }

    private fun getAvatarImageResource(avatarName: String?): Int? {
        return when (avatarName) {
            "avatar_alien" -> R.drawable.avataralien
            "avatar_bebe" -> R.drawable.avatarbebe
            "avatar_hombre" -> R.drawable.avatarhombre
            "avatar_mujer" -> R.drawable.avatarmujer
            "avatar_frankenstein" -> R.drawable.avatarfrankenstein
            "avatar_lobo" -> R.drawable.avatarlobo
            "avatar_vampira" -> R.drawable.avatarvampira
            else -> {
                // Log para depuración si el nombre no coincide
                println("Nombre de avatar no reconocido: $avatarName")
                null
            }
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