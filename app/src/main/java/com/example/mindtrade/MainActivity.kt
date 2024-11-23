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

    private fun loadUserData() {
        userId?.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val avatarName = document.getString("avatarName")
                        val tradingStyle = document.getString("trading_style")
                        val psico = document.getString("psico")
                        val emotion = document.getString("emotion")

                        // Actualizar las imágenes dinámicamente
                        setAvatarImage(avatarName)
                        setTradingStyleImage(tradingStyle)
                        setPsicoImage(psico)
                        setEmotionImage(emotion)
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
        val avatarResource = when (avatarName) {
            "avatar_alien" -> R.drawable.avataralien
            "avatar_bebe" -> R.drawable.avatarbebe
            "avatar_hombre" -> R.drawable.avatarhombre
            "avatar_mujer" -> R.drawable.avatarmujer
            "avatar_frankenstein" -> R.drawable.avatarfrankenstein
            "avatar_lobo" -> R.drawable.avatarlobo
            "avatar_vampira" -> R.drawable.avatarvampira
            else -> null
        }
        avatarResource?.let { avatarImage.setImageResource(it) }
    }

    private fun setTradingStyleImage(tradingStyle: String?) {
        val tradingResource = when (tradingStyle) {
            "Day Trading" -> R.drawable.daytrader
            "Scalping" -> R.drawable.scalper
            "Swing Trading" -> R.drawable.swingtarder
            else -> null
        }
        tradingResource?.let { tradingStyleImage.setImageResource(it) }
    }

    private fun setPsicoImage(psico: String?) {
        val psicoResource = when (psico) {
            "Psico +" -> R.drawable.positive
            "Psico -" -> R.drawable.negative
            else -> null
        }
        psicoResource?.let { psicoImage.setImageResource(it) }
    }

    private fun setEmotionImage(emotion: String?) {
        val emotionResource = when (emotion) {
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
        emotionResource?.let { emotionImage.setImageResource(it) }
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
