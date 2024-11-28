package com.example.mindtrade

import MyStrategiesFragment
import adapters.AccountAdapter
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import auth.LoginActivity
import adapters.StrategyAdapter
import android.view.View
import androidx.fragment.app.FragmentContainerView
import com.example.mindtrade.model.Strategy
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import strategycards.FavoriteStrategiesFragment
import strategycards.RegisterStrategyActivity
import strategycards.StrategyDetailFragment

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MyStrategiesFragment.OnStrategyDeletedListener  {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var avatarImage: ShapeableImageView
    private lateinit var tradingStyleImage: ShapeableImageView
    private lateinit var psicoImage: ShapeableImageView
    private lateinit var emotionImage: ShapeableImageView
    private lateinit var accountsRecyclerView: RecyclerView
    private lateinit var strategiesRecyclerView: RecyclerView
    private lateinit var addStrategyButton: Button // Nuevo botón para añadir estrategia
    private lateinit var navAvatarImage: ImageView
    private lateinit var navUserNameText: TextView
    private lateinit var navTradingStyleText: TextView
    private lateinit var navPsicoStateText: TextView
    private lateinit var navEmotionText: TextView
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null

    private var userAlias: String? = null
    private var userAvatarName: String? = null
    private var userTradingStyle: String? = null
    private var userPsico: String? = null
    private var userEmotion: String? = null
    private lateinit var registerStrategyLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.navigationView)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)

        avatarImage = findViewById(R.id.userAvatar)
        tradingStyleImage = findViewById(R.id.tradingStyleImage)
        psicoImage = findViewById(R.id.psicoImage)
        emotionImage = findViewById(R.id.emotionImage)
        accountsRecyclerView = findViewById(R.id.accountsRecyclerView)
        strategiesRecyclerView = findViewById(R.id.strategiesRecyclerView)
        addStrategyButton = findViewById(R.id.addStrategyButton) // Inicializar botón

        val headerView = navigationView.getHeaderView(0)
        navAvatarImage = headerView.findViewById(R.id.navAvatarImage)
        navUserNameText = headerView.findViewById(R.id.navUserNameText)
        navTradingStyleText = headerView.findViewById(R.id.navTradingStyleText)
        navPsicoStateText = headerView.findViewById(R.id.navPsicoState)
        navEmotionText = headerView.findViewById(R.id.navEmotion)

        val currentUser = FirebaseAuth.getInstance().currentUser
        userId = currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        registerStrategyLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val newStrategyId = result.data?.getStringExtra("newStrategyId")
                if (newStrategyId != null) {
                    // Recargar las estrategias y resaltar la nueva
                    setupStrategiesRecyclerView()
                    Toast.makeText(this, "Nueva estrategia añadida", Toast.LENGTH_SHORT).show()
                }
            }
        }


        setupAccountsRecyclerView()
        setupStrategiesRecyclerView()
        setupImageClickListeners()
        setupAddStrategyButton() // Configurar botón "Añadir Estrategia"
        loadUserData()
    }

    private fun setupAccountsRecyclerView() {
        val accountsList = listOf("Cuenta 1", "Cuenta 2", "Cuenta 3", "Cuenta 4", "Cuenta 5")
        accountsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        accountsRecyclerView.adapter = AccountAdapter(accountsList)
    }

    private fun startAutoScroll(itemCount: Int) {
        val handler = android.os.Handler()
        val inactivityHandler = android.os.Handler() // Handler para la inactividad
        var currentIndex = 0

        val runnable = object : Runnable {
            override fun run() {
                if (currentIndex < itemCount) {
                    strategiesRecyclerView.smoothScrollToPosition(currentIndex)
                    currentIndex++
                } else {
                    currentIndex = 0 // Reiniciar al inicio cuando lleguemos al final
                    strategiesRecyclerView.smoothScrollToPosition(currentIndex)
                }
                handler.postDelayed(this, 3000) // Cambiar cada 3 segundos
            }
        }

        handler.postDelayed(runnable, 3000)

        // Configurar el tiempo de inactividad antes de reanudar el scroll (5 segundos)
        val INACTIVITY_DELAY = 5000L

        // Opción para detener el scroll si el usuario interactúa
        strategiesRecyclerView.setOnTouchListener { _, _ ->
            handler.removeCallbacks(runnable) // Detener el scroll automático
            inactivityHandler.removeCallbacksAndMessages(null) // Cancelar reinicios previos

            // Configurar el reinicio automático después de la inactividad
            inactivityHandler.postDelayed({
                handler.postDelayed(runnable, 3000) // Reanudar el scroll automático
            }, INACTIVITY_DELAY)

            false // Permitir que el RecyclerView maneje el evento táctil
        }



// Sobrescribir el performClick en el RecyclerView
        strategiesRecyclerView.setOnClickListener {
            strategiesRecyclerView.performClick()
        }
    }

    private fun openStrategyDetailFragment(strategy: Strategy) {
        // Ocultar vistas principales
        findViewById<RecyclerView>(R.id.accountsRecyclerView).visibility = View.GONE
        findViewById<TextView>(R.id.accountsSummary).visibility = View.GONE
        findViewById<RecyclerView>(R.id.strategiesRecyclerView).visibility = View.GONE
        findViewById<TextView>(R.id.tradingStrategiesSummary).visibility = View.GONE
        findViewById<Button>(R.id.addStrategyButton).visibility = View.GONE

        // Mostrar el contenedor de fragmentos
        findViewById<FragmentContainerView>(R.id.fragmentContainer).visibility = View.VISIBLE

        // Crear y agregar el fragmento
        val fragment = StrategyDetailFragment().apply {
            arguments = Bundle().apply {
                putString("strategyId", strategy.id) // Añadir el ID de la estrategia
                putString("strategyTitle", strategy.title)
                putString("strategyDescription", strategy.description)
                putString("strategyAuthor", strategy.author)
                putString("strategyAvatarName", strategy.avatarName)
                putStringArray("strategyIndicators", strategy.indicators.toTypedArray())
                putStringArray("strategyTimeframes", strategy.timeframes.toTypedArray())
                putStringArray("tradingStyles", strategy.tradingStyles.toTypedArray())
                putDouble("strategyRating", strategy.rating)
            }
        }

        // Añade animaciones para la transacción del fragmento
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in, // Animación de entrada
                R.anim.fade_out, // Animación de salida
                R.anim.fade_in, // Animación al retroceder (popEnter)
                R.anim.fade_out  // Animación al salir (popExit)
            )
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null) // Agrega el fragmento a la pila de retroceso
            .commit()
    }


    private fun setupStrategiesRecyclerView() {
        strategiesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Consulta Firestore para obtener las últimas 10 estrategias subidas
        db.collection("strategies")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                val strategies = result.map { document ->
                    Strategy(
                        id = document.id,
                        title = document.getString("title") ?: "Sin título",
                        description = document.getString("description") ?: "Sin descripción",
                        author = document.getString("authorAlias") ?: "Anónimo",
                        avatarName = document.getString("avatarName"),
                        avatarUrl = document.getString("avatarUrl"),
                        rating = document.getDouble("rating") ?: 0.0,
                        createdBy = document.getString("createdBy") ?: "",
                        indicators = (document.get("indicators") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        timeframes = (document.get("timeframes") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        tradingStyles = (document.get("tradingStyles") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    )
                }

                // Actualizar el adaptador
                strategiesRecyclerView.adapter = StrategyAdapter(strategies) { strategy ->
                    openStrategyDetailFragment(strategy)
                }

                // Configurar el adaptador
                val adapter = StrategyAdapter(strategies) { strategy ->
                    openStrategyDetailFragment(strategy)
                }
                strategiesRecyclerView.adapter = adapter

                // Iniciar scroll automático
                startAutoScroll(strategies.size)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar estrategias: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun setupImageClickListeners() {
        avatarImage.setOnClickListener {
            val alias = userAlias ?: "Sin alias"
            val avatarImageResource = getAvatarImageResource(userAvatarName)
            openImageDetail(avatarImageResource, alias)
        }

        tradingStyleImage.setOnClickListener {
            val tradingStyle = userTradingStyle ?: "Sin nombre"
            val tradingStyleImageResource = getTradingStyleImageResource(tradingStyle)
            val tradingStyleText = when (tradingStyle) {
                "Day Trading" -> "Day trader"
                "Scalping" -> "Scalper"
                "Swing Trading" -> "Swing trader"
                else -> tradingStyle
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
                else -> emotion
            }
            openImageDetail(emotionImageResource, emotionText)
        }
    }

    private fun setupAddStrategyButton() {
        addStrategyButton.setOnClickListener {
            val intent = Intent(this, RegisterStrategyActivity::class.java)
            registerStrategyLauncher.launch(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }


    private fun openImageDetail(imageResId: Int?, imageName: String) {
        val intent = Intent(this, ImageDetailActivity::class.java)
        intent.putExtra("imageResId", imageResId ?: 0)
        intent.putExtra("imageName", imageName)
        startActivityWithFade(intent)
    }

    private fun loadUserData() {
        userId?.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userAlias = document.getString("alias")
                        userAvatarName = document.getString("avatarName")
                        userTradingStyle = document.getString("trading_style")
                        userPsico = document.getString("psico")
                        userEmotion = document.getString("emotion")

                        setAvatarImage(userAvatarName)
                        setTradingStyleImage(userTradingStyle)
                        setPsicoImage(userPsico)
                        setEmotionImage(userEmotion)

                        updateNavigationView()
                    } else {
                        println("El documento del usuario no existe.")
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun updateNavigationView() {
        val avatarResource = getAvatarImageResource(userAvatarName)
        avatarResource?.let { navAvatarImage.setImageResource(it) }

        navUserNameText.text = userAlias ?: "Sin alias"

        val tradingStyleText = when (userTradingStyle) {
            "Day Trading" -> "Day trader"
            "Scalping" -> "Scalper"
            "Swing Trading" -> "Swing trader"
            else -> "Sin estilo"
        }
        navTradingStyleText.text = tradingStyleText
        when (tradingStyleText) {
            "Day trader" -> navTradingStyleText.setTextColor(
                resources.getColor(
                    R.color.turquoise_blue,
                    theme
                )
            )

            "Scalper" -> navTradingStyleText.setTextColor(
                resources.getColor(
                    R.color.orange,
                    theme
                )
            )

            "Swing trader" -> navTradingStyleText.setTextColor(
                resources.getColor(
                    R.color.forest_green,
                    theme
                )
            )

            else -> navTradingStyleText.setTextColor(
                resources.getColor(
                    android.R.color.white,
                    theme
                )
            )
        }

        val psicoText = userPsico ?: "Sin estado"
        navPsicoStateText.text = psicoText
        when (psicoText) {
            "Psico +" -> navPsicoStateText.setTextColor(resources.getColor(R.color.highlight_green, theme))
            "Psico -" -> navPsicoStateText.setTextColor(resources.getColor(R.color.my_red, theme))
            else -> navPsicoStateText.setTextColor(resources.getColor(android.R.color.white, theme))
        }

        val emotionText = when (userEmotion) {
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
            else -> userEmotion ?: "Sin emoción"
        }
        navEmotionText.text = emotionText

        val negativeEmotionTexts = listOf(
            "Trader ansioso", "Trader impaciente", "Trader descontrolado",
            "Trader avaricioso", "Trader insatisfecho", "Trader rabioso",
            "Trader avergonzado", "Trader confundido", "Trader atemorizado",
            "Trader fatalista", "Trader frustrado", "Trader ineficiente"
        )
        val positiveEmotionTexts = listOf(
            "Trader autocontrolado", "Trader confiado", "Trader eficiente",
            "Trader optimista", "Trader paciente", "Trader realizado",
            "Trader satisfecho", "Trader seguro", "Trader en sintonía",
            "Trader tranquilo", "Trader aceptado", "Trader afirmativo"
        )
        when (emotionText) {
            in negativeEmotionTexts -> navEmotionText.setTextColor(resources.getColor(R.color.my_red, theme))
            in positiveEmotionTexts -> navEmotionText.setTextColor(resources.getColor(R.color.highlight_green, theme))
            else -> navEmotionText.setTextColor(resources.getColor(android.R.color.white, theme))
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
            "Swing Trading" -> R.drawable.swingtrader
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
            "Confusión" -> R.drawable.confusion
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

    private fun restoreMainView() {
        // Mostrar las vistas principales del MainActivity
        findViewById<RecyclerView>(R.id.accountsRecyclerView).visibility = View.VISIBLE
        findViewById<TextView>(R.id.accountsSummary).visibility = View.VISIBLE
        findViewById<RecyclerView>(R.id.strategiesRecyclerView).visibility = View.VISIBLE
        findViewById<TextView>(R.id.tradingStrategiesSummary).visibility = View.VISIBLE
        findViewById<Button>(R.id.addStrategyButton).visibility = View.VISIBLE

        // Ocultar el contenedor de fragmentos
        findViewById<FragmentContainerView>(R.id.fragmentContainer).visibility = View.GONE
    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Regresar al estado principal de la MainActivity
                supportFragmentManager.popBackStack(
                    null,
                    androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                restoreMainView() // Asegúrate de que las vistas principales se muestren
            }
            R.id.nav_strategies -> {
                // Ocultar vistas del MainActivity
                findViewById<RecyclerView>(R.id.accountsRecyclerView).visibility = View.GONE
                findViewById<TextView>(R.id.accountsSummary).visibility = View.GONE
                findViewById<RecyclerView>(R.id.strategiesRecyclerView).visibility = View.GONE
                findViewById<TextView>(R.id.tradingStrategiesSummary).visibility = View.GONE
                findViewById<Button>(R.id.addStrategyButton).visibility = View.GONE

                // Mostrar contenedor de fragmentos
                findViewById<FragmentContainerView>(R.id.fragmentContainer).visibility = View.VISIBLE

                // Reemplazar el fragmento
                val fragment = MyStrategiesFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null) // Esto permite regresar al MainActivity
                    .commit()
            }

            R.id.nav_favorite_strategies -> {
                findViewById<RecyclerView>(R.id.accountsRecyclerView).visibility = View.GONE
                findViewById<TextView>(R.id.accountsSummary).visibility = View.GONE
                findViewById<RecyclerView>(R.id.strategiesRecyclerView).visibility = View.GONE
                findViewById<TextView>(R.id.tradingStrategiesSummary).visibility = View.GONE
                findViewById<Button>(R.id.addStrategyButton).visibility = View.GONE

                // Mostrar contenedor de fragmentos
                findViewById<FragmentContainerView>(R.id.fragmentContainer).visibility = View.VISIBLE

                // Reemplazar el fragmento
                val fragment = FavoriteStrategiesFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }


            R.id.nav_accounts -> {
                // Restaurar vistas principales y ocultar el contenedor de fragmentos
                findViewById<RecyclerView>(R.id.accountsRecyclerView).visibility = View.VISIBLE
                findViewById<TextView>(R.id.accountsSummary).visibility = View.VISIBLE
                findViewById<RecyclerView>(R.id.strategiesRecyclerView).visibility = View.VISIBLE
                findViewById<TextView>(R.id.tradingStrategiesSummary).visibility = View.VISIBLE
                findViewById<Button>(R.id.addStrategyButton).visibility = View.VISIBLE
                findViewById<FragmentContainerView>(R.id.fragmentContainer).visibility = View.GONE
            }

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


    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager

        if (fragmentManager.backStackEntryCount > 0) {
            // Retrocede al fragmento anterior en la pila
            fragmentManager.popBackStack()

            // Verificar si no queda ningún fragmento visible después de retroceder
            fragmentManager.executePendingTransactions()
            val currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer)
            if (currentFragment == null) {
                // Si no hay más fragmentos, restaurar la vista principal
                restoreMainView()
            }
        } else {
            // Restaurar la vista principal si no hay más fragmentos en la pila
            restoreMainView()
        }
    }

    override fun onStrategyDeleted() {
        // Recargar el RecyclerView de estrategias
        setupStrategiesRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        // Actualizar el RecyclerView de estrategias al volver al MainActivity
        setupStrategiesRecyclerView()
    }

}