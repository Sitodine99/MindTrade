package com.example.mindtrade

import YourAdapter
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var avatarImage: ShapeableImageView
    private lateinit var tradingStyleImage: ShapeableImageView
    private lateinit var psicoImage: ShapeableImageView
    private lateinit var emotionImage: ShapeableImageView
    private lateinit var accountsRecyclerView: RecyclerView
    private lateinit var strategiesRecyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null

    // Variables para almacenar datos dinámicos del usuario
    private var userAlias: String? = null // Alias del usuario
    private var userAvatarName: String? = null
    private var userTradingStyle: String? = null
    private var userPsico: String? = null
    private var userEmotion: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configurar Toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configurar DrawerLayout y NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.navigationView)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)

        // Configurar referencias de UI
        avatarImage = findViewById(R.id.userAvatar)
        tradingStyleImage = findViewById(R.id.tradingStyleImage)
        psicoImage = findViewById(R.id.psicoImage)
        emotionImage = findViewById(R.id.emotionImage)
        accountsRecyclerView = findViewById(R.id.accountsRecyclerView)
        strategiesRecyclerView = findViewById(R.id.strategiesRecyclerView)

        // Obtener ID del usuario autenticado
        val currentUser = FirebaseAuth.getInstance().currentUser
        userId = currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Configurar RecyclerView para las cuentas
        setupAccountsRecyclerView()

        // Configurar RecyclerView para las estrategias
        setupStrategiesRecyclerView()

        // Configurar clic en imágenes para abrir detalles
        setupImageClickListeners()

        // Recuperar datos del usuario
        loadUserData()
    }

    private fun setupAccountsRecyclerView() {
        val accountsList = listOf("Cuenta 1", "Cuenta 2", "Cuenta 3", "Cuenta 4", "Cuenta 5")
        accountsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        accountsRecyclerView.adapter = YourAdapter(accountsList)
    }

    private fun setupStrategiesRecyclerView() {
        val strategiesList = listOf("Estrategia 1", "Estrategia 2", "Estrategia 3")
        strategiesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        strategiesRecyclerView.adapter = YourAdapter(strategiesList)
    }

    private fun setupImageClickListeners() {
        avatarImage.setOnClickListener {
            val alias = userAlias ?: "Sin alias" // Mostrar el alias del usuario
            val avatarImageResource = getAvatarImageResource(userAvatarName)
            openImageDetail(avatarImageResource, alias)
        }

        tradingStyleImage.setOnClickListener {
            val tradingStyle = userTradingStyle ?: "Sin nombre"
            val tradingStyleImageResource = getTradingStyleImageResource(tradingStyle)
            val tradingStyleText = when (tradingStyle) {
                "Day Trading" -> "Daytrader"
                "Scalping" -> "Scalper"
                "Swing Trading" -> "Swingtrader"
                else -> tradingStyle // Si no coincide, muestra el texto recibido
            }
            openImageDetail(tradingStyleImageResource, tradingStyleText)
        }

        psicoImage.setOnClickListener {
            val psico = userPsico ?: "Sin nombre"
            val psicoImageResource = getPsicoImageResource(psico)
            openImageDetail(psicoImageResource, psico)
        }

        emotionImage.setOnClickListener {
            val emotion = userEmotion ?: "Sin nombre"
            val emotionImageResource = getEmotionImageResource(emotion)
            val emotionText = when (emotion) {
                "Ansiedad" -> "Trader ansioso"
                "Impaciencia" -> "Trader impaciente"
                "Descontrol" -> "Trader descontrolado"
                "Avaricia" -> "Trader avaricioso"
                "Insatisfacción" -> "Trader insatisfecho"
                "Rabia" -> "Trader rabioso"
                "Vergüenza" -> "Trader avergonzado"
                "Confusion" -> "Trader confundido"
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
                else -> emotion
            }
            openImageDetail(emotionImageResource, emotionText)
        }
    }

    private fun openImageDetail(imageResId: Int?, imageName: String) {
        val intent = Intent(this, ImageDetailActivity::class.java)
        intent.putExtra("imageResId", imageResId ?: 0) // Pasa 0 si la imagen es null
        intent.putExtra("imageName", imageName)
        startActivityWithFade(intent)
    }

    private fun loadUserData() {
        userId?.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userAlias = document.getString("alias") // Recuperar el alias del usuario
                        userAvatarName = document.getString("avatarName")
                        userTradingStyle = document.getString("trading_style")
                        userPsico = document.getString("psico")
                        userEmotion = document.getString("emotion")

                        // Actualizar las imágenes dinámicamente
                        setAvatarImage(userAvatarName)
                        setTradingStyleImage(userTradingStyle)
                        setPsicoImage(userPsico)
                        setEmotionImage(userEmotion)
                    } else {
                        println("El documento del usuario no existe.")
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setAvatarImage(avatarName: String?) {
        val avatarResource = getAvatarImageResource(avatarName)
        avatarResource?.let { avatarImage.setImageResource(it) }
    }

    private fun setTradingStyleImage(tradingStyle: String?) {
        val tradingResource = getTradingStyleImageResource(tradingStyle)
        tradingResource?.let { tradingStyleImage.setImageResource(it) }
    }

    private fun setPsicoImage(psico: String?) {
        val psicoResource = getPsicoImageResource(psico)
        psicoResource?.let { psicoImage.setImageResource(it) }
    }

    private fun setEmotionImage(emotion: String?) {
        val emotionResource = getEmotionImageResource(emotion)
        emotionResource?.let { emotionImage.setImageResource(it) }
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

    private fun getTradingStyleImageResource(tradingStyle: String?): Int? {
        return when (tradingStyle) {
            "Day Trading" -> R.drawable.daytrader
            "Scalping" -> R.drawable.scalper
            "Swing Trading" -> R.drawable.swingtarder
            else -> null
        }
    }

    private fun getPsicoImageResource(psico: String?): Int? {
        return when (psico) {
            "Psico +" -> R.drawable.positive
            "Psico -" -> R.drawable.negative
            else -> null
        }
    }

    private fun getEmotionImageResource(emotion: String?): Int? {
        return when (emotion) {
            "Ansiedad" -> R.drawable.ansiedad
            "Impaciencia" -> R.drawable.impaciencia
            "Descontrol" -> R.drawable.descontrol
            "Avaricia" -> R.drawable.avaricia
            "Insatisfacción" -> R.drawable.insatisfaccion
            "Rabia" -> R.drawable.rabia
            "Vergüenza" -> R.drawable.verguenza
            "Confusion" -> R.drawable.confusion
            "Miedo" -> R.drawable.miedo
            "Fatalismo" -> R.drawable.fatalismo
            "Frustración" -> R.drawable.frustracion
            "Ineficacia" -> R.drawable.ineficacia
            "Autocontrol" -> R.drawable.autocontrol
            "Confianza" -> R.drawable.confianza
            "Eficiencia" -> R.drawable.eficiencia
            "Optimismo" -> R.drawable.optimismo
            "Paciencia" -> R.drawable.paciencia
            "Realización" -> R.drawable.realizacion
            "Satisfacción" -> R.drawable.satisfaccion
            "Seguridad" -> R.drawable.seguridad
            "Sintonía" -> R.drawable.sintonia
            "Tranquilidad" -> R.drawable.tranquilidad
            "Aceptación" -> R.drawable.aceptacion
            "Afirmación" -> R.drawable.afirmacion
            else -> null
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
