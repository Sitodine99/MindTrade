package com.example.mindtrade

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileAvatar: ShapeableImageView
    private lateinit var profileAlias: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profileTradingStyle: TextView
    private lateinit var profilePsicoState: TextView
    private lateinit var profileEmotion: TextView

    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.title = "Mi Perfil"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        profileAvatar = findViewById(R.id.profileAvatar)
        profileAlias = findViewById(R.id.profileAlias)
        profileEmail = findViewById(R.id.profileEmail)
        profileTradingStyle = findViewById(R.id.profileTradingStyle)
        profilePsicoState = findViewById(R.id.profilePsicoState)
        profileEmotion = findViewById(R.id.profileEmotion)

        loadUserProfile()
    }

    private fun loadUserProfile() {
        user?.let {
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val avatarUrl = document.getString("avatarUrl") ?: ""
                        val avatarName = document.getString("avatarName") ?: ""
                        val alias = document.getString("alias") ?: "Sin alias"
                        val email = it.email ?: "Sin email"
                        val tradingStyle = document.getString("trading_style") ?: "No definido"
                        val psicoState = document.getString("psico") ?: "No definido"
                        val emotion = document.getString("emotion") ?: "No definido"

                        profileAlias.text = alias
                        profileEmail.text = email
                        profileEmail.setTextColor(ContextCompat.getColor(this, android.R.color.black))

                        setFormattedText(profileTradingStyle, formatTradingStyle(tradingStyle))
                        setFormattedText(profilePsicoState, psicoState)
                        setFormattedText(profileEmotion, formatEmotion(emotion))

                        // Cargar imagen del avatar
                        if (avatarUrl.isNotEmpty() && avatarUrl.startsWith("https://")) {
                            Glide.with(this)
                                .load(avatarUrl)
                                .circleCrop()
                                .into(profileAvatar)
                        } else {
                            val avatarResId = getAvatarImageResource(avatarName)
                            if (avatarResId != null) {
                                profileAvatar.setImageResource(avatarResId)
                            } else {
                                profileAvatar.setImageResource(R.drawable.interrogacion) // Imagen por defecto
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    profileAlias.text = "Error al cargar perfil"
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
            else -> null
        }
    }

    private fun setFormattedText(textView: TextView, value: String) {
        textView.text = value
        textView.setTypeface(null, android.graphics.Typeface.BOLD)

        when (value) {
            "Day trader" -> textView.setTextColor(ContextCompat.getColor(this, R.color.turquoise_blue))
            "Scalper" -> textView.setTextColor(ContextCompat.getColor(this, R.color.orange))
            "Swing trader" -> textView.setTextColor(ContextCompat.getColor(this, R.color.blue_light))
            "Psico +" -> textView.setTextColor(ContextCompat.getColor(this, R.color.highlight_green))
            "Psico -" -> textView.setTextColor(ContextCompat.getColor(this, R.color.my_red))
            "Trader autocontrolado", "Trader confiado", "Trader eficiente",
            "Trader optimista", "Trader paciente", "Trader realizado",
            "Trader satisfecho", "Trader seguro", "Trader en sintonía",
            "Trader tranquilo", "Trader aceptado", "Trader afirmativo" ->
                textView.setTextColor(ContextCompat.getColor(this, R.color.highlight_green))
            "Trader ansioso", "Trader impaciente", "Trader descontrolado",
            "Trader avaricioso", "Trader insatisfecho", "Trader rabioso",
            "Trader avergonzado", "Trader confundido", "Trader atemorizado",
            "Trader fatalista", "Trader frustrado", "Trader ineficiente" ->
                textView.setTextColor(ContextCompat.getColor(this, R.color.my_red))
            else -> textView.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        }
    }

    private fun formatTradingStyle(style: String): String {
        return when (style) {
            "Day Trading" -> "Day trader"
            "Scalping" -> "Scalper"
            "Swing Trading" -> "Swing trader"
            else -> "Sin estilo"
        }
    }

    private fun formatEmotion(emotion: String): String {
        return when (emotion) {
            "Ansiedad" -> "Trader ansioso"
            "Impaciencia" -> "Trader impaciente"
            "Descontrol" -> "Trader descontrolado"
            "Avaricia" -> "Trader avaricioso"
            "Insatisfacción" -> "Trader insatisfecho"
            "Rabia" -> "Trader rabioso"
            "Vergüenza" -> "Trader avergonzado"
            "Confusión" -> "Trader confundido"
            "Miedo" -> "Trader atemorizado"
            "Fatalismo" -> "Trader fatalista"
            "Frustración" -> "Trader frustrado"
            "Ineficacia" -> "Trader ineficiente"
            "Autocontrol" -> "Trader autocontrolado"
            "Confianza" -> "Trader confiado"
            "Eficiencia" -> "Trader eficiente"
            "Optimismo" -> "Trader optimista"
            "Paciencia" -> "Trader paciente"
            "Realización" -> "Trader realizado"
            "Satisfacción" -> "Trader satisfecho"
            "Seguridad" -> "Trader seguro"
            "Sintonía" -> "Trader en sintonía"
            "Tranquilidad" -> "Trader tranquilo"
            "Aceptación" -> "Trader aceptado"
            "Afirmación" -> "Trader afirmativo"
            else -> "Sin emoción"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
