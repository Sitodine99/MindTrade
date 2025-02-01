package com.example.mindtrade

import AccountAdapter
import MyAccountsAdapter
import MyAccountsFragment
import StrategyWithImageAdapter
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
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import com.bumptech.glide.Glide
import com.example.mindtrade.model.Strategy
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import strategycards.FavoriteStrategiesFragment
import strategycards.MyStrategiesFragment
import strategycards.RegisterStrategyActivity
import strategycards.StrategyDetailFragment
import welcome.AvatarSelectionActivity
import com.example.mindtrade.model.Account
import com.example.mindtrade.model.Movement


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MyStrategiesFragment.OnStrategyDeletedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var avatarImage: ShapeableImageView
    private lateinit var tradingStyleImage: ShapeableImageView
    private lateinit var psicoImage: ShapeableImageView
    private lateinit var emotionImage: ShapeableImageView
    private lateinit var accountsRecyclerView: RecyclerView
    private lateinit var strategiesRecyclerView: RecyclerView
    private lateinit var strategiesWithImagesRecyclerView: RecyclerView
    private lateinit var addStrategyButton: ImageButton
    private lateinit var searchStrategyButton: ImageButton
    private lateinit var notificationButton: ImageButton


    private lateinit var navAvatarImage: ImageView
    private lateinit var navUserNameText: TextView
    private lateinit var navTradingStyleText: TextView
    private lateinit var navPsicoStateText: TextView
    private lateinit var navEmotionText: TextView
    private var listenerRegistration: ListenerRegistration? = null
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null

    private var userAlias: String? = null
    private var userAvatarName: String? = null
    private var avatarUrl: String? = null
    private var userTradingStyle: String? = null
    private var userPsico: String? = null
    private var userEmotion: String? = null
    private lateinit var registerStrategyLauncher: ActivityResultLauncher<Intent>

    companion object {
        const val REQUEST_UPDATE_IMAGES = 1001
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        notificationButton = findViewById(R.id.notificationButton)
        checkForNewComments() // Llamamos a la función para verificar si hay comentarios no leídos
        listenForNewComments()

        // Inicializar el botón de búsqueda
        searchStrategyButton = findViewById(R.id.searchStrategyButton)

        // Asignar acción al botón de búsqueda
        searchStrategyButton.setOnClickListener {
            val intent = Intent(this, SearchStrategyActivity::class.java)
            startActivity(intent)
        }

        // Habilitar logs de Firestore (opcional para depuración)
        FirebaseFirestore.setLoggingEnabled(true)

        // Configurar la barra de herramientas
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        // Llama al método para obtener cuentas
        fetchAccounts { accounts ->
            setupAccountsRecyclerView(accounts)
        }


        // Inicializar botones principales
        val addAccountButton: ImageButton = findViewById(R.id.addAccountButton)
        addStrategyButton = findViewById(R.id.addStrategyButton)
        val changeAccountsButton: ImageButton = findViewById(R.id.changeAccountsButton)

        // Configurar listener para el botón
        addAccountButton.setOnClickListener {
            showCreateAccountDialog()
        }

        notificationButton.setOnClickListener {
            showNewCommentsDialog()
        }


        searchStrategyButton.setOnClickListener {
            val intent = Intent(this, SearchStrategyActivity::class.java)
            startActivity(intent)
            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            ) // Transición suave
        }


        // Listener para añadir estrategias
        addStrategyButton.setOnClickListener {
            val intent = Intent(this, RegisterStrategyActivity::class.java)
            registerStrategyLauncher.launch(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Listener para cambiar las cuentas visibles en el RecyclerView
        changeAccountsButton.setOnClickListener {
            fetchAccounts { allAccounts ->
                showAccountSelectionDialog(allAccounts) { selectedAccounts ->
                    val accountsRecyclerView = findViewById<RecyclerView>(R.id.accountsRecyclerView)
                    val adapter = accountsRecyclerView.adapter
                    if (adapter is AccountAdapter) {
                        adapter.updateAccounts(selectedAccounts)
                    }
                }
            }
        }


        // Configurar el drawer layout y la navegación
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.navigationView)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)

        // Referencias a las vistas principales
        avatarImage = findViewById(R.id.userAvatar)
        tradingStyleImage = findViewById(R.id.tradingStyleImage)
        psicoImage = findViewById(R.id.psicoImage)
        emotionImage = findViewById(R.id.emotionImage)
        accountsRecyclerView = findViewById(R.id.accountsRecyclerView)
        strategiesRecyclerView = findViewById(R.id.strategiesRecyclerView)
        strategiesWithImagesRecyclerView = findViewById(R.id.strategiesWithImagesRecyclerView)


        // Referencias a las vistas del header del menú de navegación
        val headerView = navigationView.getHeaderView(0)
        navAvatarImage = headerView.findViewById(R.id.navAvatarImage)
        navUserNameText = headerView.findViewById(R.id.navUserNameText)
        navTradingStyleText = headerView.findViewById(R.id.navTradingStyleText)
        navPsicoStateText = headerView.findViewById(R.id.navPsicoState)
        navEmotionText = headerView.findViewById(R.id.navEmotion)

        // Obtener el usuario actual de Firebase Authentication
        val currentUser = FirebaseAuth.getInstance().currentUser
        userId = currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Configurar el lanzador para la actividad de registrar estrategias
        registerStrategyLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val newStrategyId = result.data?.getStringExtra("newStrategyId")
                    if (newStrategyId != null) {
                        // Recargar las estrategias y resaltar la nueva
                        setupRecyclerViews()
                        Toast.makeText(this, "Nueva estrategia añadida", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        // Configurar RecyclerViews y otros listeners
        fetchAccounts { accounts ->
            setupAccountsRecyclerView(accounts)
        }

        setupRecyclerViews()
        setupImageClickListeners()
        setupAddStrategyButton() // Configurar botón "Añadir Estrategia"

        // Cargar datos del usuario desde Firestore
        loadUserData()

        // Configurar el listener en la imagen del avatar del header para abrir AvatarSelectionActivity
        navAvatarImage.setOnClickListener {
            db.collection("users").document(userId!!).get()
                .addOnSuccessListener { document ->
                    val avatarUrl = document.getString("avatarUrl") ?: ""
                    val avatarName = document.getString("avatarName") ?: "default_avatar"
                    val alias = document.getString("alias") ?: "Sin alias"
                    val dateOfBirth = document.getString("dateOfBirth") ?: ""

                    val intent = Intent(this, AvatarSelectionActivity::class.java).apply {
                        putExtra("avatarUrl", avatarUrl)
                        putExtra("avatarName", avatarName)
                        putExtra("alias", alias)
                        putExtra("dateOfBirth", dateOfBirth)
                    }
                    startActivityWithFade(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }


    private fun getUserDateOfBirthFromFirestore(): String? {
        // Devuelve la fecha de nacimiento en el formato que uses en Firestore (por ejemplo, "dd/MM/yyyy")
        // Este valor debe haberse extraído previamente en loadUserData si existe en Firestore
        return db.collection("users").document(userId!!).get()
            .result
            ?.getString("dateOfBirth")
    }

    // Método para actualizar las estrategias del usuario
    private fun updateUserStrategies(
        newAlias: String,
        newAvatarUrl: String?,
        newAvatarName: String
    ) {
        userId?.let { id ->
            // Buscar todas las estrategias creadas por el usuario
            db.collection("strategies").whereEqualTo("createdBy", id).get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        // Actualizar cada estrategia con el nuevo alias, avatar URL y avatar name
                        val updates = hashMapOf<String, Any>(
                            "authorAlias" to newAlias,
                            "avatarUrl" to (newAvatarUrl ?: ""),
                            "avatarName" to newAvatarName
                        )
                        db.collection("strategies").document(document.id).update(updates)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error actualizando estrategias: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private val initialBalances =
        mutableMapOf<String, Double>() // HashMap para almacenar balances iniciales

    private fun fetchAccounts(onAccountsLoaded: (List<Account>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("accounts").whereEqualTo("userId", userId)
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Log.e("FetchAccounts", "Error al obtener cuentas: ${error.message}")
                    return@addSnapshotListener
                }

                if (documents != null) {
                    val accounts = documents.map { doc ->
                        val account = doc.toObject(Account::class.java)

                        // Si la cuenta no tiene registrado un balance inicial, lo guardamos
                        if (!initialBalances.containsKey(account.id)) {
                            initialBalances[account.id] = account.balance
                        }

                        account
                    }
                    onAccountsLoaded(accounts)
                }
            }


        db.collection("accounts").whereEqualTo("userId", userId)
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Toast.makeText(
                        this,
                        "Error al obtener cuentas: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                if (documents != null) {
                    val accounts = documents.map { doc -> doc.toObject(Account::class.java) }
                    onAccountsLoaded(accounts)
                }
            }
    }


    private fun setupAccountsRecyclerView(allAccounts: List<Account>) {
        val accountsRecyclerView = findViewById<RecyclerView>(R.id.accountsRecyclerView)
        val selectedAccounts = allAccounts.take(2) // Por defecto, tomamos las primeras 2 cuentas
        val adapter = accountsRecyclerView.adapter

        if (adapter is AccountAdapter) {
            // Actualiza los datos si ya existe un adaptador
            adapter.updateAccounts(selectedAccounts)
        } else {
            // Configura un nuevo AccountAdapter
            accountsRecyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            accountsRecyclerView.adapter = AccountAdapter(selectedAccounts) { selectedAccount ->
                // Abrir la nueva actividad
                val intent = Intent(this, AccountMovementsActivity::class.java).apply {
                    putExtra("accountName", selectedAccount.name)
                    putExtra("accountCreatedAt", selectedAccount.createdAt)
                    putExtra(
                        "accountInitialBalance",
                        initialBalances[selectedAccount.id] ?: selectedAccount.balance
                    )
                    putExtra(
                        "accountId",
                        selectedAccount.id
                    ) // Enviar también el ID si es necesario
                    putExtra("accountCreationDate", selectedAccount.createdAt)
                    putExtra("accountCurrency", selectedAccount.currency)
                    putExtra("accountProfitTarget", selectedAccount.profitTarget ?: 0.0)
                    putExtra("accountMaxDailyLoss", selectedAccount.maxDailyLoss ?: 0.0)
                    putStringArrayListExtra(
                        "accountMovements",
                        ArrayList(selectedAccount.movements)
                    )
                    putExtra("accountIsActive", selectedAccount.isActive)
                }
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }

        // Mostrar un diálogo para cambiar las cuentas seleccionadas
        setupAccountSelectionDialog(allAccounts)
    }


    private fun setupAccountSelectionDialog(allAccounts: List<Account>) {
        val accountsRecyclerView = findViewById<RecyclerView>(R.id.accountsRecyclerView)

        findViewById<View>(R.id.changeAccountsButton)?.setOnClickListener {
            val accountNames = allAccounts.map { it.name }.toTypedArray()
            val selectedIndices = mutableListOf<Int>() // Almacena las cuentas seleccionadas
            val selectedItems =
                BooleanArray(accountNames.size) // Estados seleccionados para el diálogo

            val builder = AlertDialog.Builder(this)
                .setTitle("Seleccionar cuentas")

            // Crear y almacenar una referencia explícita al AlertDialog
            val dialog = builder.setMultiChoiceItems(
                accountNames,
                selectedItems
            ) { dialogInterface, index, isChecked ->
                val alertDialog = dialogInterface as AlertDialog // Cast explícito
                if (isChecked) {
                    if (selectedIndices.size < 2) {
                        selectedIndices.add(index)
                    } else {
                        // Si ya hay 2 seleccionadas, desmarcamos visualmente y mostramos mensaje
                        selectedItems[index] = false
                        alertDialog.listView.setItemChecked(index, false) // Desmarcamos visualmente
                        Toast.makeText(
                            this,
                            "Solo puedes seleccionar un máximo de dos cuentas",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    selectedIndices.remove(index)
                }
            }
                .setPositiveButton("OK") { _, _ ->
                    // Tomamos solo las primeras dos cuentas seleccionadas
                    val selectedAccounts = selectedIndices.map { allAccounts[it] }.take(2)

                    // Actualizamos el adapter con las cuentas seleccionadas
                    val adapter = accountsRecyclerView.adapter
                    if (adapter is AccountAdapter) {
                        adapter.updateAccounts(selectedAccounts)
                    }
                }
                .setNegativeButton("Cancelar", null)
                .create()

            dialog.show()
        }
    }

    fun showAccountSelectionDialog(
        allAccounts: List<Account>,
        onSelectionDone: (List<Account>) -> Unit
    ) {
        val accountNames = allAccounts.map { it.name }.toTypedArray()
        val selectedIndices = mutableListOf<Int>() // Almacena las cuentas seleccionadas
        val selectedItems = BooleanArray(accountNames.size) // Estados seleccionados para el diálogo

        val builder = AlertDialog.Builder(this)
            .setTitle("Seleccionar cuentas")

        // Crear y almacenar una referencia explícita al AlertDialog
        val dialog = builder.setMultiChoiceItems(
            accountNames,
            selectedItems
        ) { dialogInterface, index, isChecked ->
            val alertDialog = dialogInterface as AlertDialog // Cast explícito
            if (isChecked) {
                if (selectedIndices.size < 2) {
                    selectedIndices.add(index)
                } else {
                    // Si ya hay 2 seleccionadas, desmarcamos visualmente y mostramos mensaje
                    selectedItems[index] = false
                    alertDialog.listView.setItemChecked(index, false) // Desmarcamos visualmente
                    Toast.makeText(
                        this,
                        "Solo puedes seleccionar un máximo de dos cuentas",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                selectedIndices.remove(index)
            }
        }
            .setPositiveButton("OK") { _, _ ->
                // Tomamos solo las cuentas seleccionadas
                val selectedAccounts = selectedIndices.map { allAccounts[it] }
                onSelectionDone(selectedAccounts)
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }


    private fun startAutoScroll(recyclerView: RecyclerView, itemCount: Int) {
        if (itemCount <= 1) return // Si no hay suficientes elementos, no iniciar el scroll

        val handler = android.os.Handler()
        val inactivityHandler = android.os.Handler() // Handler para la inactividad
        var currentIndex = 0

        val runnable = object : Runnable {
            override fun run() {
                if (currentIndex < itemCount) {
                    recyclerView.smoothScrollToPosition(currentIndex)
                    currentIndex++
                } else {
                    currentIndex = 0 // Reiniciar al inicio cuando lleguemos al final
                    recyclerView.smoothScrollToPosition(currentIndex)
                }
                handler.postDelayed(this, 3000) // Cambiar cada 3 segundos
            }
        }

        handler.postDelayed(runnable, 3000)

        // Configurar el tiempo de inactividad antes de reanudar el scroll (5 segundos)
        val INACTIVITY_DELAY = 5000L

        // Opción para detener el scroll si el usuario interactúa
        recyclerView.setOnTouchListener { _, _ ->
            handler.removeCallbacks(runnable) // Detener el scroll automático
            inactivityHandler.removeCallbacksAndMessages(null) // Cancelar reinicios previos

            // Configurar el reinicio automático después de la inactividad
            inactivityHandler.postDelayed({
                handler.postDelayed(runnable, 3000) // Reanudar el scroll automático
            }, INACTIVITY_DELAY)

            false // Permitir que el RecyclerView maneje el evento táctil
        }
    }


    private fun openStrategyDetailFragment(strategy: Strategy) {
        // Retrasar la transición hasta que los datos estén listos (aunque ya los tienes)
        val strategyId = strategy.id

        // Solo para asegurarte de que los datos son válidos:
        if (strategyId.isNotEmpty()) {
            // Ocultar vistas principales
            findViewById<RecyclerView>(R.id.accountsRecyclerView).visibility = View.GONE
            findViewById<TextView>(R.id.accountsSummary).visibility = View.GONE
            findViewById<RecyclerView>(R.id.strategiesRecyclerView).visibility = View.GONE
            findViewById<RecyclerView>(R.id.strategiesWithImagesRecyclerView).visibility = View.GONE
            findViewById<TextView>(R.id.tradingStrategiesSummary).visibility = View.GONE
            findViewById<ImageButton>(R.id.addStrategyButton).visibility = View.GONE
            findViewById<ImageButton>(R.id.addAccountButton).visibility = View.GONE
            findViewById<ImageButton>(R.id.searchStrategyButton).visibility = View.GONE
            findViewById<ImageButton>(R.id.changeAccountsButton).visibility = View.GONE

            // Mostrar el contenedor de fragmentos pero inicialmente invisible
            val fragmentContainer = findViewById<FragmentContainerView>(R.id.fragmentContainer)
            fragmentContainer.visibility = View.INVISIBLE // Mantén el contenedor invisible

            // Crear y agregar el fragmento
            val fragment = StrategyDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("strategyId", strategy.id) // Añadir el ID de la estrategia
                    putString("strategyTitle", strategy.title)
                    putString("strategyDescription", strategy.description)
                    putString("strategyAuthor", strategy.author)
                    putString("strategyAvatarName", strategy.avatarName)
                    putString("strategyAvatarUrl", strategy.avatarUrl) // PASA EL AVATAR URL AQUÍ
                    putStringArray("strategyIndicators", strategy.indicators.toTypedArray())
                    putStringArray("strategyTimeframes", strategy.timeframes.toTypedArray())
                    putStringArray("tradingStyles", strategy.tradingStyles.toTypedArray())
                    putDouble("strategyRating", strategy.rating)
                    putStringArray("strategySymbols", strategy.symbols.toTypedArray())
                    putString(
                        "algorithmCode",
                        strategy.algorithmCode
                    ) // Pasar el campo algorithmCode
                }
            }

            // Añade el fragmento a la pila pero sin mostrarlo inmediatamente
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

            // Retrasar la visibilidad del contenedor hasta que el fragmento esté cargado
            fragmentContainer.postDelayed({
                fragmentContainer.visibility = View.VISIBLE // Muestra el fragmento cargado
            }, 300) // Retraso de 300ms (puedes ajustar este valor si es necesario)
        } else {
            Toast.makeText(this, "ID de estrategia no válido.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupRecyclerViews() {
        // Configurar LayoutManagers
        strategiesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        strategiesWithImagesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Listas locales para almacenar estrategias
        val strategiesWithImages = mutableListOf<Strategy>()
        val strategiesWithoutImages = mutableListOf<Strategy>()

        // Configurar adaptadores
        val withImagesAdapter = StrategyWithImageAdapter(strategiesWithImages) { strategy ->
            openStrategyDetailFragment(strategy)
        }
        strategiesWithImagesRecyclerView.adapter = withImagesAdapter

        val withoutImagesAdapter = StrategyAdapter(strategiesWithoutImages) { strategy ->
            openStrategyDetailFragment(strategy)
        }
        strategiesRecyclerView.adapter = withoutImagesAdapter

        // Escucha en tiempo real desde Firestore
        db.collection("strategies")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    if (FirebaseAuth.getInstance().currentUser != null) {
                        Toast.makeText(
                            this,
                            "Error al escuchar estrategias: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    // Limpiar las listas para reflejar los cambios
                    strategiesWithImages.clear()
                    strategiesWithoutImages.clear()

                    for (document in snapshots) {
                        // Crear el objeto Strategy a partir de los datos del documento
                        val strategy = Strategy(
                            id = document.id,
                            title = document.getString("title") ?: "Sin título",
                            description = document.getString("description") ?: "Sin descripción",
                            author = document.getString("authorAlias") ?: "Anónimo",
                            avatarName = document.getString("avatarName"),
                            avatarUrl = document.getString("avatarUrl"),
                            rating = document.getDouble("rating") ?: 0.0,
                            createdBy = document.getString("createdBy") ?: "",
                            indicators = (document.get("indicators") as? List<*>)?.filterIsInstance<String>()
                                ?: emptyList(),
                            timeframes = (document.get("timeframes") as? List<*>)?.filterIsInstance<String>()
                                ?: emptyList(),
                            tradingStyles = (document.get("tradingStyles") as? List<*>)?.filterIsInstance<String>()
                                ?: emptyList(),
                            symbols = (document.get("symbols") as? List<*>)?.filterIsInstance<String>()
                                ?: emptyList(),
                            algorithmCode = document.getString("algorithmCode") ?: "",
                            entryConditionImageUrl = document.getString("entryConditionImageUrl"),
                            exitConditionImageUrl = document.getString("exitConditionImageUrl")
                        )

                        if (!strategy.entryConditionImageUrl.isNullOrEmpty()) {
                            strategiesWithImages.add(strategy) // Imagen de entrada presente
                        } else if (!strategy.exitConditionImageUrl.isNullOrEmpty()) {
                            strategiesWithImages.add(strategy) // Solo imagen de salida presente
                        } else {
                            strategiesWithoutImages.add(strategy) // Sin imágenes
                        }
                    }


                    // Actualizar adaptadores
                    withImagesAdapter.notifyDataSetChanged()
                    withoutImagesAdapter.notifyDataSetChanged()

                    // Llamar al autoscroll después de actualizar los adaptadores
                    startAutoScroll(strategiesWithImagesRecyclerView, strategiesWithImages.size)
                    startAutoScroll(strategiesRecyclerView, strategiesWithoutImages.size)
                }
            }
    }


    private fun setupImageClickListeners() {
        avatarImage.setOnClickListener {
            val alias = userAlias ?: "Sin alias"

            // Comprobamos si el avatar es una URL subida o un recurso local
            if (!userAvatarName.isNullOrEmpty() && userAvatarName == "default_avatar" && !avatarUrl.isNullOrEmpty()) {
                // Es una URL (imagen subida por el usuario)
                val intent = Intent(this, ImageDetailActivity::class.java).apply {
                    putExtra("imageUrl", avatarUrl) // Pasar la URL al detalle
                    putExtra("imageName", alias) // Alias del usuario
                }
                startActivityWithFade(intent)
            } else {
                // Es un recurso local
                val avatarImageResource = getAvatarImageResource(userAvatarName)
                val intent = Intent(this, ImageDetailActivity::class.java).apply {
                    putExtra(
                        "imageResId",
                        avatarImageResource ?: R.drawable.interrogacion
                    ) // Recurso local o imagen predeterminada
                    putExtra("imageName", alias) // Alias del usuario
                }
                startActivityWithFade(intent)
            }
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


    private fun openImageDetail(imageResId: Int?, imageName: String, imageUrl: String? = null) {
        val intent = Intent(this, ImageDetailActivity::class.java)

        // Envía la URL si está disponible, de lo contrario envía el recurso local
        if (!imageUrl.isNullOrEmpty()) {
            intent.putExtra("imageUrl", imageUrl)
        } else {
            intent.putExtra("imageResId", imageResId ?: 0)
        }

        intent.putExtra("imageName", imageName)
        startActivityWithFade(intent)
    }


    private fun loadUserData() {
        userId?.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Obtener datos del usuario desde Firestore
                        val newAlias = document.getString("alias") ?: "Anónimo"
                        val newAvatarUrl = document.getString("avatarUrl")
                        val newAvatarName = document.getString("avatarName") ?: "default_avatar"
                        userAlias = newAlias
                        avatarUrl = newAvatarUrl
                        userAvatarName = newAvatarName
                        userTradingStyle = document.getString("trading_style")
                        userPsico = document.getString("psico")
                        userEmotion = document.getString("emotion")

                        // Actualizar imágenes de avatar y otros elementos visuales
                        setAvatarImage(avatarUrl, userAvatarName)
                        setTradingStyleImage(userTradingStyle)
                        setPsicoImage(userPsico)
                        setEmotionImage(userEmotion)

                        // Actualizar las estrategias del usuario con el nuevo alias, avatar URL y avatar name
                        updateUserStrategies(newAlias, newAvatarUrl, newAvatarName)

                        // Actualizar el NavigationView con los nuevos datos
                        updateNavigationView(avatarUrl, userAvatarName)
                    } else {
                        println("El documento del usuario no existe.")
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }


    private fun updateNavigationView(avatarUrl: String?, avatarName: String?) {
        // Actualizar avatar en el NavigationView
        if (!avatarUrl.isNullOrEmpty() && avatarUrl.startsWith("https://")) {
            // Si hay una URL válida, cargar desde la URL
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.interrogacion) // Imagen de carga
                .error(R.drawable.interrogacion) // Imagen en caso de error
                .into(navAvatarImage)
        } else if (!avatarName.isNullOrEmpty()) {
            // Si no hay URL, usar un recurso local basado en avatarName
            val avatarResource = getAvatarImageResource(avatarName)
            avatarResource?.let { navAvatarImage.setImageResource(it) }
        } else {
            // Si no hay avatar definido, usar imagen predeterminada
            navAvatarImage.setImageResource(R.drawable.interrogacion)
        }

        // Actualizar alias del usuario
        navUserNameText.text = userAlias ?: "Sin alias"

        // Actualizar estilo de trading
        val tradingStyleText = when (userTradingStyle) {
            "Day Trading" -> "Day trader"
            "Scalping" -> "Scalper"
            "Swing Trading" -> "Swing trader"
            else -> "Sin estilo"
        }
        navTradingStyleText.apply {
            text = tradingStyleText
            setTypeface(typeface, android.graphics.Typeface.BOLD) // Texto en negrita
        }
        when (tradingStyleText) {
            "Day trader" -> navTradingStyleText.setTextColor(
                resources.getColor(R.color.turquoise_blue, theme)
            )

            "Scalper" -> navTradingStyleText.setTextColor(
                resources.getColor(R.color.orange, theme)
            )

            "Swing trader" -> navTradingStyleText.setTextColor(
                resources.getColor(R.color.blue_light, theme)
            )

            else -> navTradingStyleText.setTextColor(
                resources.getColor(android.R.color.white, theme)
            )
        }

        // Actualizar estado psicológico (Psico)
        val psicoText = userPsico ?: "Sin estado"
        navPsicoStateText.apply {
            text = psicoText
            setTypeface(typeface, android.graphics.Typeface.BOLD) // Texto en negrita
        }
        when (psicoText) {
            "Psico +" -> navPsicoStateText.setTextColor(
                resources.getColor(R.color.highlight_green, theme)
            )

            "Psico -" -> navPsicoStateText.setTextColor(
                resources.getColor(R.color.my_red, theme)
            )

            else -> navPsicoStateText.setTextColor(
                resources.getColor(android.R.color.white, theme)
            )
        }

        // Actualizar emoción
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
        navEmotionText.apply {
            text = emotionText
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

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
            in negativeEmotionTexts -> navEmotionText.setTextColor(
                resources.getColor(R.color.my_red, theme)
            )

            in positiveEmotionTexts -> navEmotionText.setTextColor(
                resources.getColor(R.color.highlight_green, theme)
            )

            else -> navEmotionText.setTextColor(
                resources.getColor(android.R.color.white, theme)
            )
        }
    }


    private fun setAvatarImage(avatarUrl: String?, avatarName: String?) {
        if (!avatarUrl.isNullOrEmpty() && avatarUrl.startsWith("https://")) {
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.interrogacion)
                .error(R.drawable.interrogacion)
                .circleCrop()
                .into(avatarImage)
        } else if (!avatarName.isNullOrEmpty()) {
            val avatarResource = getAvatarImageResource(avatarName)
            if (avatarResource != null) {
                Glide.with(this)
                    .load(avatarResource)
                    .circleCrop()
                    .into(avatarImage)
            } else {
                avatarImage.setImageResource(R.drawable.interrogacion)
            }
        } else {
            avatarImage.setImageResource(R.drawable.interrogacion)
        }
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
        findViewById<RecyclerView>(R.id.strategiesWithImagesRecyclerView).visibility = View.VISIBLE
        findViewById<TextView>(R.id.tradingStrategiesSummary).visibility = View.VISIBLE
        findViewById<ImageButton>(R.id.addStrategyButton).visibility = View.VISIBLE
        findViewById<ImageButton>(R.id.addAccountButton).visibility = View.VISIBLE
        findViewById<ImageButton>(R.id.searchStrategyButton).visibility = View.VISIBLE
        findViewById<ImageButton>(R.id.changeAccountsButton).visibility = View.VISIBLE


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

            R.id.nav_profile -> {

                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

                    // Restaura la visibilidad de MainActivity después de iniciar ProfileActivity
                    findViewById<View>(R.id.mainContentLayout).postDelayed({
                        findViewById<View>(R.id.mainContentLayout).visibility = View.VISIBLE
                    }, 300)
                }, 150) // Retraso antes de iniciar la actividad (ajústalo según prefieras)
            }


            R.id.nav_strategies -> {
                // Ocultar vistas del MainActivity
                findViewById<RecyclerView>(R.id.accountsRecyclerView).visibility = View.GONE
                findViewById<TextView>(R.id.accountsSummary).visibility = View.GONE
                findViewById<RecyclerView>(R.id.strategiesRecyclerView).visibility = View.GONE
                findViewById<RecyclerView>(R.id.strategiesWithImagesRecyclerView).visibility =
                    View.GONE
                findViewById<TextView>(R.id.tradingStrategiesSummary).visibility = View.GONE
                findViewById<ImageButton>(R.id.addStrategyButton).visibility = View.GONE
                findViewById<ImageButton>(R.id.addAccountButton).visibility = View.GONE
                findViewById<ImageButton>(R.id.searchStrategyButton).visibility = View.GONE
                findViewById<ImageButton>(R.id.changeAccountsButton).visibility = View.GONE


                // Mostrar contenedor de fragmentos
                findViewById<FragmentContainerView>(R.id.fragmentContainer).visibility =
                    View.VISIBLE

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
                findViewById<RecyclerView>(R.id.strategiesWithImagesRecyclerView).visibility =
                    View.GONE
                findViewById<TextView>(R.id.tradingStrategiesSummary).visibility = View.GONE
                findViewById<ImageButton>(R.id.addStrategyButton).visibility = View.GONE
                findViewById<ImageButton>(R.id.addAccountButton).visibility = View.GONE
                findViewById<ImageButton>(R.id.searchStrategyButton).visibility = View.GONE
                findViewById<ImageButton>(R.id.changeAccountsButton).visibility = View.GONE


                // Mostrar contenedor de fragmentos
                findViewById<FragmentContainerView>(R.id.fragmentContainer).visibility =
                    View.VISIBLE

                // Reemplazar el fragmento
                val fragment = FavoriteStrategiesFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }


            R.id.nav_accounts -> {
                // Ocultar las vistas principales del MainActivity
                findViewById<RecyclerView>(R.id.accountsRecyclerView).visibility = View.GONE
                findViewById<TextView>(R.id.accountsSummary).visibility = View.GONE
                findViewById<RecyclerView>(R.id.strategiesRecyclerView).visibility = View.GONE
                findViewById<RecyclerView>(R.id.strategiesWithImagesRecyclerView).visibility =
                    View.GONE
                findViewById<TextView>(R.id.tradingStrategiesSummary).visibility = View.GONE
                findViewById<ImageButton>(R.id.addStrategyButton).visibility = View.GONE
                findViewById<ImageButton>(R.id.addAccountButton).visibility = View.GONE
                findViewById<ImageButton>(R.id.searchStrategyButton).visibility = View.GONE
                findViewById<ImageButton>(R.id.changeAccountsButton).visibility = View.GONE


                // Asegurarte de que el contenedor de fragmentos esté visible
                findViewById<FragmentContainerView>(R.id.fragmentContainer).visibility =
                    View.VISIBLE

                // Reemplazar el contenido del contenedor con el fragmento MyAccountsFragment
                val fragment = MyAccountsFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null) // Agrega el fragmento a la pila para navegación
                    .commit()
            }

            R.id.nav_guide -> { // Nuevo caso para la Guía de Uso
                val intent = Intent(this, GuideActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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
            super.onBackPressed()
        }
    }

    override fun onStrategyDeleted() {
        // Recargar el RecyclerView de estrategias
        setupRecyclerViews()
    }

    override fun onResume() {
        super.onResume()
        // Actualizar el RecyclerView de estrategias al volver al MainActivity
        setupRecyclerViews()
        updateDynamicImages()
        fetchAccounts { accounts ->
            setupAccountsRecyclerView(accounts)
        }

    }

    // Método para eliminar la escucha al cerrar sesión
    private fun removeListener() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    // Llama a removeListener en onDestroy y en logout
    override fun onDestroy() {
        super.onDestroy()
        removeListener()
    }

    // En tu lógica de logout (por ejemplo, en el botón de logout):
    private fun logout() {
        removeListener()
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showCreateAccountDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_create_account)
        dialog.setCancelable(true)

        // Inicializar vistas del diálogo
        val accountNameInput: EditText = dialog.findViewById(R.id.accountNameInput)
        val accountBalanceInput: EditText = dialog.findViewById(R.id.accountBalanceInput)
        val currencySpinner: Spinner = dialog.findViewById(R.id.currencySpinner)
        val profitTargetInput: EditText = dialog.findViewById(R.id.profitTargetInput)
        val maxDailyLossInput: EditText = dialog.findViewById(R.id.maxDailyLossInput)
        val saveAccountButton: Button = dialog.findViewById(R.id.saveAccountButton)
        val cancelButton: Button = dialog.findViewById(R.id.cancelButton)

        // Establecer filtro para el máximo de 10 caracteres en el nombre de la cuenta
        accountNameInput.filters = arrayOf(InputFilter.LengthFilter(10))

        // Establecer filtro para el balance: máximo 8 dígitos numéricos
        accountBalanceInput.filters = arrayOf(
            InputFilter.LengthFilter(8),
            InputFilter { source, _, _, _, _, _ ->
                if (source.matches(Regex("[0-9]*"))) source else ""
            }
        )

        // Establecer filtro para la pérdida máxima diaria: máximo 8 dígitos numéricos
        maxDailyLossInput.filters = arrayOf(
            InputFilter.LengthFilter(8),
            InputFilter { source, _, _, _, _, _ ->
                if (source.matches(Regex("[0-9]*"))) source else ""
            }
        )

        // Configurar Spinner (si es necesario)
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.currencies_array, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner.adapter = adapter

        // Botón Guardar
        saveAccountButton.setOnClickListener {
            val name = accountNameInput.text.toString()
            val balance = accountBalanceInput.text.toString().toDoubleOrNull() ?: 0.0
            val currency = currencySpinner.selectedItem.toString()
            val profitTarget = profitTargetInput.text.toString().toDoubleOrNull() ?: 0.0
            val maxDailyLoss = maxDailyLossInput.text.toString().toDoubleOrNull() ?: 0.0

            if (name.isEmpty()) {
                Toast.makeText(this, "El nombre de la cuenta es obligatorio", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (balance <= 0) {
                Toast.makeText(this, "El balance debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (maxDailyLoss > balance) {
                Toast.makeText(
                    this,
                    "La pérdida máxima diaria no puede ser mayor que el balance",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (profitTarget < balance) {
                Toast.makeText(
                    this,
                    "El objetivo de beneficio no puede ser menor que el balance",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Guardar en Firestore
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val account = Account(
                    id = FirebaseFirestore.getInstance().collection("accounts").document().id,
                    userId = userId,
                    name = name,
                    balance = balance,
                    currency = currency,
                    profitTarget = profitTarget,
                    maxDailyLoss = maxDailyLoss,
                    createdAt = System.currentTimeMillis(),
                    movements = emptyList()
                )
                FirebaseFirestore.getInstance().collection("accounts").document(account.id)
                    .set(account)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
                        dialog.dismiss() // Cierra el diálogo
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al guardar la cuenta: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Cancelar
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // Mostrar el diálogo
        dialog.show()
    }

    private fun updateDynamicImages() {
        userId?.let { uid ->
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val predominantPsico = snapshot.getString("psico") ?: "No definido"
                        val predominantEmotion = snapshot.getString("emotion") ?: "No definido"
                        val predominantTradingStyle =
                            snapshot.getString("trading_style") ?: "No definido"

                        Log.d(
                            "DynamicImages",
                            "Psico: $predominantPsico, Emotion: $predominantEmotion, Style: $predominantTradingStyle"
                        )

                        // Aquí actualizamos las imágenes con los nuevos datos
                        setPsicoImage(predominantPsico)
                        setEmotionImage(predominantEmotion)
                        setTradingStyleImage(predominantTradingStyle)
                    } else {
                        Log.e("DynamicImages", "No se encontraron datos para el usuario.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DynamicImages", "Error al obtener datos: ${e.message}")
                }
        }
    }

    fun checkForNewComments() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val notificationRef = db.collection("notifications").document(userId)

        notificationRef.get().addOnSuccessListener { document ->
            val newComments = document.get("newComments") as? List<String> ?: emptyList()

            updateNotificationIcon(newComments.isNotEmpty()) // 🔔 Actualiza el icono de notificación

            if (newComments.isNotEmpty()) {
                val sharedPreferences = getSharedPreferences("MindTradePrefs", MODE_PRIVATE)
                sharedPreferences.edit()
                    .putStringSet("new_comments_strategies", newComments.toSet()).apply()
            }
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error al obtener notificaciones: ${e.message}")
        }
    }


    private fun updateNotificationIcon(hasNewComments: Boolean) {
        val notificationButton = findViewById<ImageButton>(R.id.notificationButton)
        if (hasNewComments) {
            notificationButton.setImageResource(R.drawable.ic_notifications_active)
        } else {
            notificationButton.setImageResource(R.drawable.ic_notifications)
        }
    }

    private fun showNewCommentsDialog() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val notificationRef = db.collection("notifications").document(userId)

        notificationRef.get().addOnSuccessListener { document ->
            val newComments = document.get("newComments") as? List<Map<String, String>> ?: emptyList()

            if (newComments.isEmpty()) {
                Toast.makeText(this, "No tienes nuevas notificaciones", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val titles = newComments.map { "Tienes nuevos comentarios en la estrategia: ${it["title"]}" }

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Nuevas notificaciones")
            builder.setItems(titles.toTypedArray()) { _, _ -> }
            builder.setPositiveButton("OK") { _, _ ->
                markAllCommentsAsRead(userId) // 🔹 Marcar todos los comentarios como leídos
            }
            builder.show()
        }
    }


    private fun markCommentsAsRead(strategyId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val notificationRef = db.collection("notifications").document(userId)

        notificationRef.get().addOnSuccessListener { document ->
            val newComments = document.get("newComments") as? MutableList<String> ?: mutableListOf()
            newComments.remove(strategyId)

            notificationRef.set(mapOf("newComments" to newComments)).addOnSuccessListener {
                updateNotificationIcon(newComments.isNotEmpty())
            }
        }
    }


    private fun listenForNewComments() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("strategies")
            .whereEqualTo("createdBy", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                checkForNewComments() // Revisar si hay nuevos comentarios no leídos
            }
    }

    private fun markAllCommentsAsRead(userId: String) {
        val notificationRef = db.collection("notifications").document(userId)
        notificationRef.update("newComments", emptyList<String>())
            .addOnSuccessListener {
                updateNotificationIcon(false) // Restablece el icono de notificación
            }
    }


}