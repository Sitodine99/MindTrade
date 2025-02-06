package com.example.mindtrade

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileAvatar: ShapeableImageView
    private lateinit var profileAlias: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profileTradingStyle: TextView
    private lateinit var profilePsicoState: TextView
    private lateinit var profileEmotion: TextView
    private lateinit var profileAge: TextView
    private lateinit var profileStrategies: TextView
    private lateinit var profileAccounts: TextView
    private lateinit var profileTrades: TextView
    private lateinit var profileComments: TextView
    private lateinit var profileAverageRating: TextView
    private lateinit var profileFavoriteAsset: TextView
    private lateinit var profileContainer: View

    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mantener la actividad oculta al principio

        setContentView(R.layout.activity_profile)

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) // Aplicar transición suave


        supportActionBar?.title = "Mi Perfil"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Contenedor principal que agrupa todos los elementos
        profileContainer = findViewById(R.id.profileContainer)

        // Inicializar vistas
        profileAvatar = findViewById(R.id.profileAvatar)
        profileAlias = findViewById(R.id.profileAlias)
        profileEmail = findViewById(R.id.profileEmail)
        profileTradingStyle = findViewById(R.id.profileTradingStyle)
        profilePsicoState = findViewById(R.id.profilePsicoState)
        profileEmotion = findViewById(R.id.profileEmotion)
        profileAge = findViewById(R.id.profileAge)
        profileStrategies = findViewById(R.id.profileStrategies)
        profileAccounts = findViewById(R.id.profileAccounts)
        profileTrades = findViewById(R.id.profileTrades)
        profileComments = findViewById(R.id.profileComments)
        profileAverageRating = findViewById(R.id.profileAverageRating)
        profileFavoriteAsset = findViewById(R.id.profileFavoriteAsset)

        //Ocultar el contenido antes de la carga
        profileContainer.visibility = View.INVISIBLE

        //Cargar perfil con efecto de fade-in
        loadUserProfile()
    }

    private fun loadUserProfile() {
        user?.let { firebaseUser ->
            val userId = firebaseUser.uid

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val avatarUrl = document.getString("avatarUrl") ?: ""
                        val avatarName = document.getString("avatarName") ?: ""
                        val alias = document.getString("alias") ?: "Sin alias"
                        val email = firebaseUser.email ?: "Sin email"
                        val tradingStyle = document.getString("trading_style") ?: "No definido"
                        val psicoState = document.getString("psico") ?: "No definido"
                        val emotion = document.getString("emotion") ?: "No definido"
                        val birthdate = document.getString("dateOfBirth") ?: ""


                        profileAlias.text = alias
                        profileEmail.text = email
                        profileEmail.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                        setFormattedText(profileTradingStyle, formatTradingStyle(tradingStyle))
                        setFormattedText(profilePsicoState, psicoState)
                        setFormattedText(profileEmotion, formatEmotion(emotion))

                        // Calcular y mostrar la edad
                        profileAge.text = "Edad: ${calculateAge(birthdate)}"

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

                        // Calcular y mostrar la edad
                        val edad = calculateAge(birthdate)
                        profileAge.text = if (edad >= 0) "Edad: $edad años" else "Edad: No disponible"

                        // Consultar estadísticas del usuario
                        fetchStrategiesCount(userId)
                        fetchAccountsAndTrades(userId)
                        fetchCommentsCount(userId)
                        fetchAverageRating(userId)
                        fetchFavoriteAsset(userId)

                        // Aplicar fade-in cuando los datos ya están listos
                        Handler(Looper.getMainLooper()).postDelayed({
                            profileContainer.visibility = View.VISIBLE
                            profileContainer.alpha = 0f
                            profileContainer.animate().alpha(1f).setDuration(500).start()
                        }, 200) // Pequeño delay para asegurar que todo esté cargado
                    }
                }
                .addOnFailureListener {
                    profileAlias.text = "Error al cargar perfil"
                }
        }
    }

    private fun calculateAge(dateOfBirth: String): Int {
        if (dateOfBirth.isEmpty()) return -1  // Si la fecha está vacía, retornar -1

        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Formato correcto
            val birthDate: Date = dateFormat.parse(dateOfBirth) ?: return -1 // Convertir String a Date

            val birthCalendar = Calendar.getInstance().apply { time = birthDate }
            val today = Calendar.getInstance()

            var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)

            // Ajuste si el cumpleaños aún no ha pasado este año
            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--
            }

            age
        } catch (e: Exception) {
            e.printStackTrace()
            -1  // Si hay un error, retornar -1
        }
    }



    private fun fetchStrategiesCount(userId: String) {
        db.collection("strategies").whereEqualTo("createdBy", userId).get()
            .addOnSuccessListener { documents ->
                val strategiesCount = documents.size() // Contar el número de estrategias del usuario
                profileStrategies.text = "Estrategias publicadas: $strategiesCount"
            }
            .addOnFailureListener {
                profileStrategies.text = "Estrategias publicadas: Error"
            }
    }


    private fun fetchAccountsAndTrades(userId: String) {
        db.collection("accounts").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { accounts ->
                profileAccounts.text = "Cuentas creadas: ${accounts.size()}"

                var totalTrades = 0
                for (account in accounts) {
                    totalTrades += (account.getLong("movementsCount") ?: 0).toInt()
                }
                profileTrades.text = "Nº de operaciones: $totalTrades"
            }
    }

    private fun fetchCommentsCount(userId: String) {
        db.collection("strategies").get()
            .addOnSuccessListener { documents ->
                var totalComments = 0

                for (document in documents) {
                    val commentsList = document["comments"] as? List<Map<String, Any>> ?: emptyList()

                    for (comment in commentsList) {
                        val commentUserId = comment["userId"] as? String
                        if (commentUserId == userId) {
                            totalComments++
                        }

                        // Contar respuestas dentro de replies
                        val repliesList = comment["replies"] as? List<Map<String, Any>> ?: emptyList()
                        for (reply in repliesList) {
                            val replyUserId = reply["userId"] as? String
                            if (replyUserId == userId) {
                                totalComments++
                            }
                        }
                    }
                }

                profileComments.text = "Nº de comentarios: $totalComments"
            }
            .addOnFailureListener {
                profileComments.text = "Nº de comentarios: Error"
            }
    }


    private fun fetchAverageRating(userId: String) {
        db.collection("strategies").whereEqualTo("createdBy", userId).get()
            .addOnSuccessListener { documents ->
                var totalRating = 0.0
                var count = 0

                for (document in documents) {
                    val rating = document.getDouble("rating") ?: 0.0
                    val totalVotes = document.getLong("totalVotes") ?: 0

                    // Consideramos solo estrategias que han sido votadas al menos una vez
                    if (totalVotes > 0) {
                        totalRating += rating
                        count++
                    }
                }

                // Calcular la media solo si hay estrategias con votos
                val average = if (count > 0) totalRating / count else 0.0
                profileAverageRating.text = "Valoración media: ${String.format("%.2f", average)}"
            }
            .addOnFailureListener {
                profileAverageRating.text = "Valoración media: Error"
            }
    }


    private fun fetchFavoriteAsset(userId: String) {
        db.collection("accounts").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { accounts ->
                val accountIds = accounts.map { it.id }

                if (accountIds.isEmpty()) {
                    profileFavoriteAsset.text = "Activo favorito: No disponible"
                    println("No se encontraron cuentas para el usuario.")
                    return@addOnSuccessListener
                }

                println("Se encontraron ${accounts.size()} cuentas.")

                val assetFrequency = mutableMapOf<String, Pair<Int, Long>>() // Símbolo -> (Frecuencia, createdAt más reciente)
                var pendingRequests = accountIds.size

                // Recorrer cada cuenta y obtener su subcolección de movimientos
                for (accountId in accountIds) {
                    db.collection("accounts").document(accountId).collection("movements").get()
                        .addOnSuccessListener { movements ->
                            for (movement in movements) {
                                val symbol = movement.getString("symbol") ?: continue
                                val createdAt = movement.getLong("createdAt") ?: 0L

                                println("Movimiento encontrado - Símbolo: $symbol, Fecha: $createdAt")

                                // Si el símbolo ya existe, aumentar el contador y actualizar createdAt si es más reciente
                                val current = assetFrequency[symbol]
                                if (current != null) {
                                    assetFrequency[symbol] = Pair(current.first + 1, maxOf(current.second, createdAt))
                                } else {
                                    assetFrequency[symbol] = Pair(1, createdAt)
                                }
                            }

                            pendingRequests--
                            if (pendingRequests == 0) {
                                determineFavoriteAsset(assetFrequency)
                            }
                        }
                        .addOnFailureListener {
                            println("❌ Error al obtener movimientos para la cuenta $accountId: ${it.message}")
                            pendingRequests--
                            if (pendingRequests == 0) {
                                determineFavoriteAsset(assetFrequency)
                            }
                        }
                }
            }
            .addOnFailureListener {
                profileFavoriteAsset.text = "Activo favorito: Error al obtener cuentas"
                println("❌ Error al obtener cuentas: ${it.message}")
            }
    }

    /**
     * Determina el activo favorito basándose en la frecuencia y el `createdAt` más reciente en caso de empate.
     */
    private fun determineFavoriteAsset(assetFrequency: Map<String, Pair<Int, Long>>) {
        if (assetFrequency.isEmpty()) {
            profileFavoriteAsset.text = "Activo favorito: No disponible"
            println("🔴 No se pudo calcular un activo favorito, mapa vacío.")
            return
        }

        // Encontrar el activo más repetido y en caso de empate, el más reciente
        val favoriteAsset = assetFrequency.maxWithOrNull(
            compareBy({ it.value.first }, { it.value.second })
        )?.key ?: "--"

        println("Activo favorito determinado: $favoriteAsset")
        profileFavoriteAsset.text = "Activo favorito: $favoriteAsset"
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
