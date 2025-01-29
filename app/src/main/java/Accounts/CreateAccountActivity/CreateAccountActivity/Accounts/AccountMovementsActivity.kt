package com.example.mindtrade

import MovementsAdapter
import ScreenPagerAdapter
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.mindtrade.model.Movement
import com.example.mindtrade.model.Strategy
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import java.util.UUID


class AccountMovementsActivity : AppCompatActivity(), MovementsAdapter.MovementActionListener {

    private val movementsList = mutableListOf<Movement>() // Lista de movimientos
    private lateinit var movementsAdapter: MovementsAdapter
    private lateinit var depositFixedValue: String // Guarda el depósito inicial como valor fijo
    private val strategies = mutableListOf<Strategy>() // Lista de estrategias disponibles
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var selectedEmotion: String = "Selecciona un estado emocional"
    var isEmotionSpinnerInitialized =
        false // Controlar que el spinner no dispare al abrir el formulario
    private var movementImageView01: ImageView? = null
    private var movementImageView02: ImageView? = null
    private var selectedImageView: ImageView? = null
    private var selectedStrategy: Strategy? = null
    private var selectedEmotionDetail: String? = null
    private val emotionCounts = mutableMapOf<String, Int>() // Contador de emociones


    private val symbolGroups = mapOf(
        "Forex" to listOf(
            "EUR/USD", "USD/JPY", "GBP/USD", "USD/CHF",
            "AUD/USD", "USD/CAD", "NZD/USD"
        ).map { it.trim().uppercase() }, // Normalizar a mayúsculas
        "Exotics" to listOf("USD/SEK", "USD/NOK", "USD/ZAR", "EUR/TRY").map {
            it.trim().uppercase()
        },
        "Metals" to listOf("XAU/USD", "XAG/USD", "XPT/USD", "XPD/USD").map {
            it.trim().uppercase()
        },
        "Crypto" to listOf(
            "BTC/USD",
            "ETH/USD",
            "LTC/USD",
            "XRP/USD",
            "ADA/USD",
            "DOT/USD"
        ).map { it.trim().uppercase() },
        "Cash CFD" to listOf(
            "US30.cash", "SPX500.cash", "NAS100.cash", "GER30.cash", "FRA40.cash",
            "UK100.cash", "ESP35.cash", "JPN225.cash", "AUS200.cash"
        ).map { it.trim().uppercase() },
        "Commodities" to listOf(
            "SOYBEAN", "WHEAT", "CORN", "COFFEE", "COCOA", "USOIL", "NATGAS"
        ).map { it.trim().uppercase() },
        "Equities" to listOf(
            "AAPL",
            "MSFT",
            "GOOGL",
            "AMZN",
            "TSLA",
            "META",
            "NFLX",
            "NVDA"
        ).map { it.trim().uppercase() }
    )

    private var selectedSymbol: String? = null


    // Definir las emociones para Psico+ y Psico-
    private val emotionsPsicoPlus = listOf(
        "Autocontrol", "Confianza", "Eficiencia", "Optimismo", "Paciencia",
        "Realización", "Satisfacción", "Seguridad", "Sintonía", "Tranquilidad",
        "Aceptación", "Afirmación"
    )

    private val emotionsPsicoMinus = listOf(
        "Ansiedad", "Impaciencia", "Descontrol", "Avaricia", "Insatisfacción",
        "Rabia", "Vergüenza", "Confusión", "Miedo", "Fatalismo",
        "Frustración", "Ineficacia"
    )

    private var image01Url: String? = null
    private var image02Url: String? = null
    private var selectedImageType: String? = null

    companion object {
        private const val REQUEST_CODE_IMAGE_PICKER = 1001
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_movements)
        val accountId = intent.getStringExtra("accountId") ?: "default_account_id"


        // Configurar el ViewPager2
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val layouts = listOf(
            R.layout.layout_screen_movements,
            R.layout.layout_screen_details,
            R.layout.layout_screen_summary
        )
        val adapter = ScreenPagerAdapter(layouts)
        viewPager.adapter = adapter

        // Datos de la cuenta recibidos como parámetros (Intent)
        val accountName = intent.getStringExtra("accountName") ?: "Nombre no disponible"
        val accountBalance = intent.getDoubleExtra("accountBalance", 0.0).toFloat()
        val accountCurrency = intent.getStringExtra("accountCurrency") ?: "USD"
        val accountProfitTarget = intent.getDoubleExtra("accountProfitTarget", 0.0)
        val accountMaxDailyLoss = intent.getDoubleExtra("accountMaxDailyLoss", 0.0)
        val accountCreatedAt = intent.getLongExtra("accountCreatedAt", System.currentTimeMillis())
        val accountMovements = intent.getStringArrayListExtra("accountMovements") ?: arrayListOf()

        // Formatea la fecha de creación
        val formattedDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(accountCreatedAt))

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> { // Pantalla de movimientos
                        val screenMovementsView = viewPager.findViewWithTag<View>("f0")
                        screenMovementsView?.let { movementsView ->
                            movementsView.findViewById<TextView>(R.id.accountMovementsCountTextView).text =
                                "Movimientos: ${accountMovements.size}"

                            val sharedPreferences =
                                getSharedPreferences("MindTradePrefs", MODE_PRIVATE)

                            var accountInitialBalance =
                                sharedPreferences.getFloat("initialBalance_$accountId", -1f)

                            if (accountInitialBalance == -1f) {
                                accountInitialBalance =
                                    intent.getDoubleExtra("accountInitialBalance", 0.0).toFloat()
                                sharedPreferences.edit()
                                    .putFloat("initialBalance_$accountId", accountInitialBalance)
                                    .apply()
                            }

                            Log.d("BalanceDebug", "Depósito Inicial: $accountInitialBalance")

                            depositFixedValue =
                                "%.2f".format(accountInitialBalance)

                            val depositEditText =
                                movementsView.findViewById<EditText>(R.id.depositEditText)

                            val savedDeposit =
                                sharedPreferences.getFloat("initialBalance_$accountId", 0f)

                            depositEditText.setText("$%.2f".format(savedDeposit))

                            setupMovementsView(movementsView, accountId)
                        }
                    }

                    1 -> { // Pantalla de detalles
                        val screenDetailsView = viewPager.findViewWithTag<View>("f1")
                        screenDetailsView?.let {
                            val initialBalance = intent.getDoubleExtra("accountBalance", 0.0)

                            val (dataX, dataY) = generateChartData(movementsList, initialBalance)
                            setupECharts(it, dataX, dataY)

                            setupEmotionChart(
                                rootView = it,
                                emotionData = emotionCounts
                            )

                            it.findViewById<TextView>(R.id.accountProfitTargetTextView).text =
                                "Objetivo: $%.2f".format(accountProfitTarget)
                            it.findViewById<TextView>(R.id.accountMaxDailyLossTextView).text =
                                "Máx pérdida diaria: $%.2f".format(accountMaxDailyLoss)
                        }
                    }

                    2 -> { // Pantalla 3: Gráfico de ratio de éxito
                        val screenSummaryView = viewPager.findViewWithTag<View>("f2")
                        if (screenSummaryView == null) {
                            Log.e("ViewPager", "No se encontró la vista de la pantalla 3 (f2)")
                        } else {
                            Log.d("ViewPager", "Actualizando pantalla 3 con el gráfico de éxito")
                            setupSuccessRatioChart(screenSummaryView)
                        }
                    }

                    else -> {
                        Log.d("PageChange", "Página seleccionada: $position")
                    }
                }
            }
        })
    }

        private fun setupMovementsView(rootView: View, accountId: String) {
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.movementsRecyclerView)
        val addMovementButton = rootView.findViewById<ImageButton>(R.id.addMovementButton)

        // Configurar el adaptador
        movementsAdapter =
            MovementsAdapter(this, movementsList, this) // 'this' implementa MovementActionListener

        recyclerView.adapter = movementsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar movimientos desde Firestore
        loadMovementsFromFirestore(accountId) {
            calculateMetrics()
        }

        // Botón para añadir un nuevo movimiento
        addMovementButton.setOnClickListener {
            showMovementDialog(accountId, strategies)
        }
    }

    private fun loadMovementsFromFirestore(accountId: String, onComplete: () -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("accounts")
            .document(accountId)
            .collection("movements")
            .orderBy("createdAt") // Ordena por el campo `createdAt`
            .get()
            .addOnSuccessListener { snapshot ->
                movementsList.clear()
                for (doc in snapshot) {
                    val movement = doc.toObject(Movement::class.java)
                    movementsList.add(movement)
                    // Actualizar el mapa de emociones
                    movement.emotion?.let { emotion ->
                        emotionCounts[emotion] = (emotionCounts[emotion] ?: 0) + 1
                        updateEmotionChart(emotionCounts) // Actualizar el gráfico de emociones
                    }
                    movementsAdapter.notifyItemInserted(movementsList.size - 1)
                    updateMovementsCount()
                    calculateMetrics()
                }
                movementsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al cargar movimientos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun showMovementDialog(accountId: String, strategies: List<Strategy>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_register_movement, null)
        // Referencias a los elementos de la vista
        val symbolsSpinner = dialogView.findViewById<Spinner>(R.id.symbolsSpinner)
        val addSymbolEditText = dialogView.findViewById<EditText>(R.id.addSymbolEditText)
        val addSymbolButton = dialogView.findViewById<Button>(R.id.addSymbolButton)
        val customSymbolsChipGroup = dialogView.findViewById<ChipGroup>(R.id.customSymbolsChipGroup)
        val entryDateButton = dialogView.findViewById<Button>(R.id.entryDateButton)
        val exitDateButton = dialogView.findViewById<Button>(R.id.exitDateButton)
        val entryPriceEditText = dialogView.findViewById<EditText>(R.id.entryPriceEditText)
        val exitPriceEditText = dialogView.findViewById<EditText>(R.id.exitPriceEditText)
        val swapEditText = dialogView.findViewById<EditText>(R.id.swapEditText)


        // Establecer un filtro para permitir números negativos y decimales
        swapEditText.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source.isEmpty() || source.toString().matches(Regex("-?\\d*(\\.\\d*)?"))) {
                source // Permitir entrada válida
            } else {
                "" // Bloquear entrada inválida
            }
        })

        val commissionEditText = dialogView.findViewById<EditText>(R.id.commissionEditText)
        val operationTypeSpinner = dialogView.findViewById<Spinner>(R.id.operationTypeSpinner)

        val strategySpinner = dialogView.findViewById<Spinner>(R.id.strategySpinner)
        val emotionalStateSpinner = dialogView.findViewById<Spinner>(R.id.emotionalStateSpinner)
        val commentsEditText = dialogView.findViewById<EditText>(R.id.commentsEditText)
        val emotionSpinner = dialogView.findViewById<Spinner>(R.id.emotionSpinner)
        val uploadImageButton = dialogView.findViewById<ImageButton>(R.id.uploadImageButton)
        // Asocia los ImageView del diálogo
        val movementImageView01 = dialogView.findViewById<ImageView>(R.id.MovementeImageView01)
        val movementImageView02 = dialogView.findViewById<ImageView>(R.id.MovementeImageView02)
        val lotesEditText = dialogView.findViewById<EditText>(R.id.lotesEditText)
        val multiplierEditText = dialogView.findViewById<EditText>(R.id.multiplierEditText)
        // Ajuste al guardar el movimiento
        val finalMultiplier = if (customSymbolsChipGroup.childCount > 0) {
            val multiplier = multiplierEditText.text.toString().toDoubleOrNull()
            if (multiplier == null || multiplier <= 0) {
                Toast.makeText(
                    this,
                    "Introduce un multiplicador válido para el símbolo personalizado",
                    Toast.LENGTH_SHORT
                ).show()
                return // Salimos del método showMovementDialog
            }
            multiplier
        } else {
            calcularMultiplicadorPorSimbolo(selectedSymbol)
        }

        var entryDateTime: Long? = null
        var exitDateTime: Long? = null


        val symbolsWithHeaders = mutableListOf<String>().apply {
            add("Selecciona un Activo") // Opción inicial
            symbolGroups.forEach { (header, symbols) ->
                add("**$header**") // Categorías como títulos
                addAll(symbols)    // Símbolos dentro de las categorías
            }
        }

        val symbolsAdapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            symbolsWithHeaders
        ) {
            override fun isEnabled(position: Int): Boolean {
                return !symbolsWithHeaders[position].startsWith("**") && position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                if (symbolsWithHeaders[position].startsWith("**") || position == 0) {
                    textView.setTextColor(Color.GRAY)
                    textView.setTypeface(null, Typeface.BOLD)
                } else {
                    textView.setTextColor(Color.BLACK)
                    textView.setTypeface(null, Typeface.NORMAL)
                }
                return view
            }
        }
        symbolsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        symbolsSpinner.adapter = symbolsAdapter

        // Configuración del Spinner de tipo de operación
        val operationTypes = arrayOf("Buy", "Sell")
        val operationTypeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            operationTypes
        )
        operationTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        operationTypeSpinner.adapter = operationTypeAdapter

        symbolsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val symbol = symbolsWithHeaders[position]
                if (!symbol.startsWith("**") && position != 0) {
                    selectedSymbol = symbol.trim().uppercase()
                    Log.d("DebugSpinner", "selectedSymbol actualizado: $selectedSymbol")
                    multiplierEditText.visibility = View.VISIBLE
                    val autoMultiplier = calcularMultiplicadorPorSimbolo(selectedSymbol)
                    multiplierEditText.setText(autoMultiplier.toString())
                } else {
                    selectedSymbol = null
                    Log.d("DebugSpinner", "Símbolo no seleccionado o inválido.")
                    multiplierEditText.visibility = View.GONE
                    multiplierEditText.setText("")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedSymbol = null
                multiplierEditText.visibility = View.GONE
                multiplierEditText.setText("")
            }
        }


// Configuración del botón para añadir un símbolo personalizado
        addSymbolButton.setOnClickListener {
            val newSymbol = addSymbolEditText.text.toString().trim()
            if (newSymbol.isNotEmpty()) {
                if (customSymbolsChipGroup.childCount == 0) {
                    val chip = Chip(this).apply {
                        text = newSymbol
                        isCloseIconVisible = true
                        setOnCloseIconClickListener {
                            customSymbolsChipGroup.removeView(this)
                            symbolsSpinner.isEnabled = true
                            selectedSymbol = null
                            multiplierEditText.visibility =
                                View.GONE // Ocultar campo de multiplicador
                            multiplierEditText.setText("") // Limpiar el campo
                        }
                    }
                    customSymbolsChipGroup.addView(chip)
                    addSymbolEditText.text.clear()

                    // Deshabilitar Spinner y forzar uso de símbolo personalizado
                    symbolsSpinner.setSelection(0) // Deseleccionar cualquier símbolo estándar
                    symbolsSpinner.isEnabled = false // Desactivar el Spinner
                    selectedSymbol = newSymbol

                    // Mostrar el campo de multiplicador para el símbolo personalizado
                    multiplierEditText.visibility = View.VISIBLE
                    multiplierEditText.setText("") // Vaciar el campo para entrada manual
                } else {
                    Toast.makeText(
                        this,
                        "Solo puedes añadir un símbolo personalizado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "Introduce un símbolo válido", Toast.LENGTH_SHORT).show()
            }
        }



        entryDateButton.setOnClickListener {
            showDateTimePicker { selectedDateTime: Long ->
                entryDateTime = selectedDateTime
                entryDateButton.text = "Fecha Entrada: ${formatDate(selectedDateTime)}"
            }
        }

        exitDateButton.setOnClickListener {
            if (entryDateTime != null) {
                showDateTimePicker { selectedDateTime: Long ->
                    if (selectedDateTime >= entryDateTime!!) { // Validar que sea mayor o igual a la fecha de entrada
                        exitDateTime = selectedDateTime
                        exitDateButton.text = "Fecha Salida: ${formatDate(selectedDateTime)}"
                    } else {
                        Toast.makeText(
                            this,
                            "La fecha de salida no puede ser menor a la de entrada",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Por favor selecciona una fecha de entrada primero",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        // Actualizar las imágenes si ya hay URLs disponibles
        image01Url?.let { url ->
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(movementImageView01)
        }

        image02Url?.let { url ->
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(movementImageView02)
        }


        // Configurar el botón de subir imagen
        uploadImageButton.setOnClickListener {
            showImageManagementDialog(movementImageView01, movementImageView02)
        }

        // Configuración del Spinner de estado emocional
        val emotionalStatesAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.emotional_states,
            android.R.layout.simple_spinner_item
        )
        emotionalStatesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        emotionalStateSpinner.adapter = emotionalStatesAdapter

        // Configurar el comportamiento del Spinner de emociones
        emotionalStateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedEmotion = parent?.getItemAtPosition(position).toString()
                when (selectedEmotion) {
                    "Psico+" -> {
                        setEmotionSpinnerOptions(emotionSpinner, emotionsPsicoPlus)
                    }

                    "Psico-" -> {
                        setEmotionSpinnerOptions(emotionSpinner, emotionsPsicoMinus)
                    }

                    else -> {
                        selectedEmotion = "Selecciona un estado emocional"
                        clearEmotionSpinner(emotionSpinner) // Limpiamos si no selecciona válido
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedEmotion = "Selecciona un estado emocional"
                clearEmotionSpinner(emotionSpinner) // Limpiamos si no selecciona nada
            }

        }


        // Cargar estrategias del usuario y sus favoritas
        loadStrategies(userId) { strategies ->
            val strategyTitles = strategies.map { it.title }.toMutableList()
            strategyTitles.add(0, "Selecciona una Estrategia")

            val strategyAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                strategyTitles
            )
            strategyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            strategySpinner.adapter = strategyAdapter

            strategySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position != 0) { // Asume que la posición 0 es "Selecciona una Estrategia"
                        selectedStrategy = strategies[position - 1]
                        Log.d("StrategyDebug", "Estrategia seleccionada: ${selectedStrategy?.id}")
                    } else {
                        selectedStrategy = null
                        Log.d("StrategyDebug", "No se seleccionó ninguna estrategia")
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedStrategy = null
                }
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Registrar", null) // Evitamos el cierre automático aquí
            .create()

// Sobrescribimos el comportamiento del botón "Registrar"
        dialog.setOnShowListener {
            val registerButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            registerButton.setOnClickListener {
                val lotes = lotesEditText.text.toString().toDoubleOrNull()
                val entryPrice =
                    entryPriceEditText.text.toString().replace(",", ".").toDoubleOrNull()
                val exitPrice = exitPriceEditText.text.toString().replace(",", ".").toDoubleOrNull()
                val swap = swapEditText.text.toString().toDoubleOrNull() ?: 0.0
                val commission = commissionEditText.text.toString().toDoubleOrNull() ?: 0.0
                val multiplierText = multiplierEditText.text.toString().trim()
                val multiplier = multiplierText.toDoubleOrNull()
                val operationType = operationTypeSpinner.selectedItem.toString()

                // Determinar el símbolo final
                val finalSymbol = if (!symbolsSpinner.isEnabled) {
                    // Si es símbolo personalizado, tomarlo del Chip
                    if (customSymbolsChipGroup.childCount > 0) {
                        val customChip = customSymbolsChipGroup.getChildAt(0) as Chip
                        customChip.text.toString()
                    } else {
                        null
                    }
                } else {
                    // Si es símbolo estándar, tomarlo del Spinner
                    selectedSymbol
                }

// Validar el multiplicador para símbolos personalizados
                if (!symbolsSpinner.isEnabled && (multiplier == null || multiplier <= 0)) {
                    Toast.makeText(
                        this,
                        "Por favor, introduce un multiplicador válido para el símbolo personalizado.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

// Validar los campos obligatorios
                if (finalSymbol == null) {
                    Toast.makeText(
                        this,
                        "Por favor selecciona o añade un símbolo.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (lotes == null || entryPrice == null || exitPrice == null) {
                    Toast.makeText(
                        this,
                        "Por favor, introduce valores válidos para lotes y precios.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (entryDateTime == null) {
                    Toast.makeText(this, "La fecha de entrada es obligatoria.", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                if (exitDateTime == null) {
                    Toast.makeText(this, "La fecha de salida es obligatoria.", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                if (exitDateTime!! < entryDateTime!!) {
                    Toast.makeText(
                        this,
                        "La fecha de salida no puede ser anterior a la de entrada.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (selectedEmotion == "Selecciona un estado emocional" || selectedEmotionDetail == null) {
                    Toast.makeText(
                        this,
                        "Por favor selecciona un estado emocional válido.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener // Salimos sin cerrar el diálogo
                }

                if (selectedEmotionDetail == "Selecciona una emoción") {
                    Toast.makeText(
                        this,
                        "Por favor selecciona una emoción válida.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener // Salimos sin cerrar el diálogo
                }

                dialog.setOnShowListener {
                    val registerButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    registerButton.setOnClickListener {
                        val operationType = operationTypeSpinner.selectedItem?.toString()
                        if (operationType.isNullOrEmpty()) {
                            Toast.makeText(
                                this,
                                "Selecciona un tipo de operación válido.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }
                    }
                }

                // Determinar si es una operación de compra o venta
                val esCompra = operationType == "Buy"

                // Calcular el multiplicador: automático o manual
                val multiplicador = if (customSymbolsChipGroup.childCount > 0) {
                    multiplierEditText.text.toString().toDoubleOrNull() ?: 1.0
                } else {
                    calcularMultiplicadorPorSimbolo(finalSymbol)
                }

                // Calcular el beneficio
                val profit = calcularBeneficioPorSimbolo(
                    precioEntrada = entryPrice,
                    precioSalida = exitPrice,
                    lotes = lotes,
                    swap = swap,
                    comision = commission,
                    simbolo = finalSymbol,
                    multiplicadorManual = multiplicador, // Aquí pasamos el multiplicador correcto
                    esCompra = esCompra
                )


                // Creamos el objeto Movement
                val movement = Movement(
                    id = UUID.randomUUID().toString(),
                    accountId = accountId,
                    symbol = finalSymbol ?: "Símbolo no especificado",
                    lotes = lotes,
                    type = operationType,
                    entryPrice = entryPrice,
                    exitPrice = exitPrice,
                    entryTime = entryDateTime ?: 0L,
                    exitTime = exitDateTime ?: 0L,
                    swap = swap,
                    commission = commission,
                    profit = profit,
                    createdAt = System.currentTimeMillis(),
                    strategyId = selectedStrategy?.id,
                    emotionalState = formatEmotionalState(selectedEmotion),
                    emotion = selectedEmotionDetail,
                    tradingStyle = determineTradingStyle(entryDateTime ?: 0L, exitDateTime ?: 0L),
                    comments = commentsEditText.text.toString(),
                    photos = listOf(image01Url ?: "", image02Url ?: "")
                )

                // Guardamos el movimiento y cerramos el diálogo si todo está correcto
                saveMovement(movement, accountId, selectedStrategy?.id)
                dialog.dismiss()
            }
        }

        dialog.show()
    }


    private fun saveMovement(movement: Movement, accountId: String, strategyId: String?) {
        val db = Firebase.firestore

        val movementData = hashMapOf(
            "id" to movement.id,
            "accountId" to accountId,
            "symbol" to movement.symbol,
            "lotes" to movement.lotes,
            "type" to movement.type,
            "entryPrice" to movement.entryPrice,
            "exitPrice" to movement.exitPrice,
            "entryTime" to movement.entryTime,
            "exitTime" to movement.exitTime,
            "swap" to movement.swap,
            "commission" to movement.commission,
            "profit" to movement.profit,
            "createdAt" to movement.createdAt,
            "strategyId" to strategyId,
            "emotionalState" to movement.emotionalState,
            "emotion" to movement.emotion,
            "tradingStyle" to movement.tradingStyle,
            "comments" to movement.comments,
            "photos" to movement.photos
        )

        db.collection("accounts")
            .document(accountId)
            .collection("movements")
            .document(movement.id)
            .set(movementData)
            .addOnSuccessListener {
                db.collection("accounts").document(accountId)
                    .get()
                    .addOnSuccessListener { document ->
                        val currentBalance = document.getDouble("balance") ?: 0.0
                        val updatedBalance = currentBalance + (movement.profit ?: 0.0)

                        // 🔹 Actualizar balance en Firestore
                        updateAccountBalance(accountId, updatedBalance)

                        // 🔹 Actualizar balance en la UI **sin recargar**
                        runOnUiThread {
                            findViewById<EditText>(R.id.balanceEditText).setText(
                                "%.2f".format(
                                    updatedBalance
                                )
                            )
                        }
                    }

                val mediaPlayer = MediaPlayer.create(this, R.raw.cash)
                mediaPlayer.start()

                movementsList.add(movement)
                movementsAdapter.notifyItemInserted(movementsList.size - 1)

                updateMovementsCount() // 🔹 Asegurar que el contador se actualiza inmediatamente en UI
                calculateMetrics()

                updateAccountWithMovement(accountId, movement.id)
                strategyId?.let {
                    saveAndLinkMovementToStrategy(strategyId, movement)
                }

                updateUserDynamicImages(movement)

                val intent = Intent()
                intent.putExtra("updateRequired", true)
                setResult(RESULT_OK, intent)

                Toast.makeText(this, "Movimiento guardado exitosamente.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al guardar el movimiento: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun updateAccountWithMovement(accountId: String, movementId: String) {
        val db = Firebase.firestore

        db.collection("accounts")
            .document(accountId)
            .update("movements", FieldValue.arrayUnion(movementId)) // Añadir el movimiento al array
            .addOnSuccessListener {
                Toast.makeText(this, "Cuenta actualizada correctamente.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al actualizar la cuenta: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun showDateTimePicker(callback: (Long) -> Unit) {
        val calendar = Calendar.getInstance()

        // Crear el DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Crear un TimePickerDialog personalizado con segundos
                val timePickerView =
                    layoutInflater.inflate(R.layout.dialog_time_picker_with_seconds, null)
                val hourPicker = timePickerView.findViewById<NumberPicker>(R.id.hourPicker)
                val minutePicker = timePickerView.findViewById<NumberPicker>(R.id.minutePicker)
                val secondPicker = timePickerView.findViewById<NumberPicker>(R.id.secondPicker)

                // Configurar los NumberPickers
                hourPicker.minValue = 0
                hourPicker.maxValue = 23
                hourPicker.value = calendar.get(Calendar.HOUR_OF_DAY)

                minutePicker.minValue = 0
                minutePicker.maxValue = 59
                minutePicker.value = calendar.get(Calendar.MINUTE)

                secondPicker.minValue = 0
                secondPicker.maxValue = 59
                secondPicker.value = calendar.get(Calendar.SECOND)

                // Crear un diálogo personalizado para elegir la hora y los segundos
                AlertDialog.Builder(this)
                    .setView(timePickerView)
                    .setPositiveButton("Aceptar") { _, _ ->
                        calendar.set(
                            year,
                            month,
                            dayOfMonth,
                            hourPicker.value,
                            minutePicker.value,
                            secondPicker.value
                        )
                        callback(calendar.timeInMillis)
                    }
                    .setNegativeButton("Cancelar", null)
                    .create()
                    .show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }


    private fun formatDate(timestamp: Long): String {
        val dateFormat =
            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date(timestamp))
    }

    // Método auxiliar para crear un chip reutilizable
    private fun createChip(text: String, isRemovable: Boolean = false, onClick: () -> Unit): Chip {
        return Chip(this).apply {
            this.text = text
            isCheckable = true
            isCloseIconVisible = isRemovable
            setOnClickListener { onClick() }
        }
    }

    private fun loadStrategies(userId: String, callback: (List<Strategy>) -> Unit) {
        val db = Firebase.firestore
        val strategies = mutableListOf<Strategy>()

        // Consultar estrategias creadas por el usuario
        val userStrategiesQuery = db.collection("strategies").whereEqualTo("createdBy", userId)

        // Consultar estrategias favoritas del usuario
        val favoriteStrategiesQuery =
            db.collection("strategies").whereArrayContains("favoritedBy", userId)

        userStrategiesQuery.get().addOnSuccessListener { userStrategiesSnapshot ->
            for (doc in userStrategiesSnapshot) {
                val strategy = doc.toObject(Strategy::class.java)
                    .copy(id = doc.id) // Añade el ID del documento
                strategies.add(strategy)
            }

            favoriteStrategiesQuery.get().addOnSuccessListener { favoriteStrategiesSnapshot ->
                for (doc in favoriteStrategiesSnapshot) {
                    val strategy = doc.toObject(Strategy::class.java)
                        .copy(id = doc.id) // Añade el ID del documento
                    // Añadir las estrategias favoritas que no están duplicadas
                    if (strategies.none { it.id == strategy.id }) { // Evitar duplicados
                        strategies.add(strategy)
                    }
                }

                callback(strategies) // Llamar al callback con la lista de estrategias
            }.addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al cargar estrategias favoritas: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al cargar estrategias: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }


    // Función para actualizar dinámicamente las opciones del Spinner de emociones
    private fun setEmotionSpinnerOptions(spinner: Spinner, options: List<String>) {
        val optionsWithDefault = mutableListOf("Selecciona una emoción") // Opción predeterminada
        optionsWithDefault.addAll(options) // Añadir las emociones específicas

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            optionsWithDefault
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Configurar el listener para capturar la emoción seleccionada

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedEmotionDetail = if (position > 0) {
                    optionsWithDefault[position] // Capturamos la emoción seleccionada
                } else {
                    null // Si selecciona "Selecciona una emoción"
                }

                // Si hay una emoción válida seleccionada
                selectedEmotionDetail?.let { emotion ->
                    // Incrementa el contador de la emoción seleccionada
                    emotionCounts[emotion] = (emotionCounts[emotion] ?: 0) + 1
                    updateEmotionChart(emotionCounts)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedEmotionDetail = null // Reiniciar emoción al no seleccionar nada
            }
        }
    }


    // Función para limpiar las opciones del Spinner
    private fun clearEmotionSpinner(spinner: Spinner) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Selecciona una emoción") // Mostrar solo la opción predeterminada
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Configurar el listener para manejar la selección vacía
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedEmotionDetail = null // Reiniciar emoción al limpiar opciones
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedEmotionDetail = null
            }
        }
    }

    // Crear el diálogo para seleccionar imágenes
    private fun showImageManagementDialog(imageView01: ImageView, imageView02: ImageView) {
        val options = arrayOf("Imagen 01", "Imagen 02")
        AlertDialog.Builder(this)
            .setTitle("Seleccionar imagen a gestionar")
            .setItems(options) { _, which ->
                val selectedImageView = if (which == 0) imageView01 else imageView02
                openImagePicker(selectedImageView)
            }
            .show()
    }


    //Permitir al usuario seleccionar una imagen
    private fun openImagePicker(targetImageView: ImageView) {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)

        // Guardamos la referencia del ImageView seleccionado para usarla después
        this.selectedImageView = targetImageView
    }


    // Gestionar el resultado de la selección de imágenes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                uploadImageToFirebase(imageUri) { uploadedUrl ->
                    if (uploadedUrl != null) {
                        selectedImageView?.let { imageView ->
                            Glide.with(this)
                                .load(uploadedUrl)
                                .placeholder(R.drawable.ic_placeholder)
                                .error(R.drawable.ic_placeholder)
                                .into(imageView)

                            // Actualizamos la URL según el ImageView
                            if (imageView.id == R.id.MovementeImageView01) {
                                image01Url = uploadedUrl
                            } else if (imageView.id == R.id.MovementeImageView02) {
                                image02Url = uploadedUrl
                            }
                        }
                        Toast.makeText(this, "Imagen subida exitosamente", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun uploadImageToFirebase(imageUri: Uri, callback: (String?) -> Unit) {
        val storageRef =
            FirebaseStorage.getInstance().reference.child("images/${System.currentTimeMillis()}.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString())
                }.addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Error al obtener la URL de la imagen",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al subir la imagen: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                callback(null)
            }
    }

    private fun saveAndLinkMovementToStrategy(strategyId: String, movement: Movement) {
        val db = Firebase.firestore

        // Actualiza el array `movements` directamente
        db.collection("strategies")
            .document(strategyId)
            .update("movements", FieldValue.arrayUnion(movement.id))
            .addOnSuccessListener {
                Log.d("MovementDebug", "ID del movimiento añadido al array de la estrategia.")

                // Guarda los detalles del movimiento en la subcolección
                db.collection("strategies")
                    .document(strategyId)
                    .collection("movements")
                    .document(movement.id)
                    .set(movement)
                    .addOnSuccessListener {
                        val intent = Intent()
                        intent.putExtra("updateRequired", true)
                        Log.d(
                            "MovementDebug",
                            "Movimiento guardado correctamente en la subcolección."
                        )
                        Toast.makeText(
                            this,
                            "Movimiento guardado y asociado correctamente a la estrategia.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreError", "Error al guardar en subcolección: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error al actualizar el array: ${e.message}")
            }
    }

    fun calcularMultiplicadorPorSimbolo(simbolo: String?): Double {
        val normalizedSymbol = simbolo?.trim()?.uppercase()
        val multiplicador = when {
            normalizedSymbol == null -> 1.0
            normalizedSymbol.startsWith("XAU") -> 100.0
            normalizedSymbol.startsWith("XAG") -> 5000.0
            normalizedSymbol.startsWith("USD/JPY") -> 100000.0
            normalizedSymbol.startsWith("USD/CHF") -> 100000.0
            normalizedSymbol.startsWith("USD/CAD") -> 100000.0
            normalizedSymbol.startsWith("USOIL") -> 100.0
            normalizedSymbol.endsWith(".CASH") || normalizedSymbol.startsWith("US") -> 1.0
            normalizedSymbol in (symbolGroups["Forex"] ?: emptyList()) -> 100000.0
            normalizedSymbol in (symbolGroups["Exotics"] ?: emptyList()) -> 100000.0
            normalizedSymbol in (symbolGroups["Crypto"] ?: emptyList()) -> 1.0
            normalizedSymbol in (symbolGroups["Commodities"] ?: emptyList()) -> 1.0
            normalizedSymbol in (symbolGroups["Equities"] ?: emptyList()) -> 1.0
            else -> 1.0
        }
        Log.d("DebugSimbolo", "Multiplicador calculado para $normalizedSymbol: $multiplicador")
        return multiplicador
    }


    fun calcularBeneficioPorSimbolo(
        precioEntrada: Double,
        precioSalida: Double,
        lotes: Double,
        swap: Double,
        comision: Double,
        simbolo: String?,
        multiplicadorManual: Double?,
        esCompra: Boolean
    ): Double {
        // Determinar el multiplicador correcto
        val multiplicador = multiplicadorManual ?: calcularMultiplicadorPorSimbolo(simbolo).also {
            Log.d("MultiplicadorDebug", "Multiplicador aplicado: $it")
        }

        // Calcular la diferencia de precio dependiendo del tipo de operación (compra/venta)
        val diferenciaPrecio = if (esCompra) {
            precioSalida - precioEntrada
        } else {
            precioEntrada - precioSalida
        }

        // Log para verificar los valores del cálculo
        Log.d(
            "DebugBeneficio",
            "Diferencia: $diferenciaPrecio, Lotes: $lotes, Multiplicador: $multiplicador, Comisión: $comision, Swap: $swap"
        )

        // Calcular el beneficio total considerando lotes y tamaño de contrato
        val beneficio = (diferenciaPrecio * lotes * multiplicador) - comision - swap

        // Redondear el beneficio a 2 decimales antes de retornarlo
        return BigDecimal(beneficio).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    // Confirmar eliminación del movimiento
    override fun confirmDeleteMovement(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar movimiento")
            .setMessage("¿Estás seguro de que deseas eliminar este movimiento?")
            .setPositiveButton("Sí") { _, _ ->
                deleteMovement(position)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteMovement(position: Int) {
        val movement = movementsList[position]
        val db = FirebaseFirestore.getInstance()
        val accountRef = db.collection("accounts").document(movement.accountId)
        val movementRef = accountRef.collection("movements").document(movement.id)

        // 🔹 Paso 1: Obtener el balance actual antes de eliminar el movimiento
        accountRef.get().addOnSuccessListener { document ->
            val currentBalance = document.getDouble("balance") ?: 0.0
            val updatedBalance =
                currentBalance - (movement.profit ?: 0.0) // 🔹 Restar el profit eliminado

            // 🔹 Paso 2: Eliminar el movimiento de Firestore
            movementRef.delete().addOnSuccessListener {
                accountRef.update("movements", FieldValue.arrayRemove(movement.id))
                    .addOnSuccessListener {
                        // 🔹 Paso 3: Actualizar el balance en Firestore
                        accountRef.update("balance", updatedBalance)
                            .addOnSuccessListener {
                                // 🔹 Actualizar UI después de la eliminación
                                movementsList.removeAt(position)
                                movementsAdapter.notifyItemRemoved(position)
                                updateMovementsCount()
                                calculateMetrics() // Recalcular métricas con balance corregido
                                Toast.makeText(
                                    this,
                                    "Movimiento eliminado y balance actualizado.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
            }
        }
    }


    private fun deleteMovementFromStrategy(
        strategyId: String,
        movementId: String,
        onComplete: () -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val strategyRef = db.collection("strategies").document(strategyId)

        // Eliminar el movimiento del array de movimientos de la estrategia
        strategyRef.update("movements", FieldValue.arrayRemove(movementId))
            .addOnSuccessListener {
                // Eliminar el movimiento de la subcolección "movements" dentro de la estrategia
                strategyRef.collection("movements").document(movementId).delete()
                    .addOnSuccessListener {
                        Log.d(
                            "DeleteMovement",
                            "Movimiento eliminado correctamente de la estrategia."
                        )
                        onComplete()
                    }
                    .addOnFailureListener { e ->
                        Log.e(
                            "DeleteMovement",
                            "Error al eliminar de la subcolección: ${e.message}"
                        )
                        Toast.makeText(
                            this,
                            "Error al eliminar de la estrategia: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("DeleteMovement", "Error al actualizar la estrategia: ${e.message}")
                Toast.makeText(
                    this,
                    "Error al actualizar la estrategia: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    // Mostrar un diálogo para ajustar el beneficio
    override fun showAdjustProfitDialog(position: Int) { // Agrega "override"
        val movement = movementsList[position]
        val dialogView = layoutInflater.inflate(R.layout.dialog_adjust_profit, null)
        val profitEditText = dialogView.findViewById<EditText>(R.id.profitEditText)
        profitEditText.setText(movement.profit?.toString() ?: "")

        AlertDialog.Builder(this)
            .setTitle("Ajustar beneficio")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val newProfit = profitEditText.text.toString().toDoubleOrNull()
                if (newProfit != null) {
                    updateMovementProfit(position, newProfit)
                } else {
                    Toast.makeText(this, "Por favor, introduce un valor válido", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Actualizar el beneficio del movimiento
    private fun updateMovementProfit(position: Int, newProfit: Double) {
        val movement = movementsList[position]
        val db = FirebaseFirestore.getInstance()
        val accountRef = db.collection("accounts").document(movement.accountId)
        val movementRef = accountRef.collection("movements").document(movement.id)

        // 🔹 Paso 1: Obtener el balance actual de la cuenta
        accountRef.get().addOnSuccessListener { document ->
            val currentBalance = document.getDouble("balance") ?: 0.0

            // 🔹 Paso 2: Calcular la diferencia de beneficio
            val oldProfit = movement.profit ?: 0.0
            val profitDifference = newProfit - oldProfit
            val updatedBalance = currentBalance + profitDifference // 🔹 Ajustar el balance

            // 🔹 Paso 3: Actualizar Firestore con el nuevo beneficio y el balance
            db.runBatch { batch ->
                batch.update(movementRef, "profit", newProfit) // Actualizar el movimiento
                batch.update(
                    accountRef,
                    "balance",
                    updatedBalance
                ) // Actualizar el balance de la cuenta
            }.addOnSuccessListener {
                // 🔹 Paso 4: Actualizar la lista local y la UI
                movementsList[position] = movement.copy(profit = newProfit)
                movementsAdapter.notifyItemChanged(position)

                findViewById<EditText>(R.id.balanceEditText).setText("%.2f".format(updatedBalance))
                calculateMetrics() // 🔹 Recalcular métricas
                Toast.makeText(this, "Beneficio y balance actualizados", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al obtener el balance: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun updateMovementsCount() {
        val accountId = movementsList.firstOrNull()?.accountId ?: return
        val db = FirebaseFirestore.getInstance()
        val accountRef = db.collection("accounts").document(accountId)

        // 🔹 Actualizar Firestore con el nuevo número de movimientos
        accountRef.update("movementsCount", movementsList.size)
            .addOnSuccessListener {
                Log.d("Firebase", "Movements count updated: ${movementsList.size}")
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al actualizar contador: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        // 🔹 También actualizar el TextView de la UI inmediatamente
        val movementsCountTextView = findViewById<TextView>(R.id.accountMovementsCountTextView)
        movementsCountTextView.text = "Movimientos: ${movementsList.size}"
    }


    private fun calculateMetrics() {
        val db = FirebaseFirestore.getInstance()
        val accountId = movementsList.firstOrNull()?.accountId ?: return

        val accountRef = db.collection("accounts").document(accountId)
        accountRef.get().addOnSuccessListener { document ->
            val updatedBalance = document.getDouble("balance") ?: 0.0
            val accountCurrency = document.getString("currency") ?: ""

            var totalSwap = 0.0
            var totalCommission = 0.0
            var totalGrossProfit = 0.0
            var totalNetProfit = 0.0

            movementsList.forEach { movement ->
                val profit = movement.profit ?: 0.0
                totalGrossProfit += profit
                totalSwap += movement.swap ?: 0.0
                totalCommission += movement.commission ?: 0.0
                totalNetProfit += profit
            }

            // 🔹 Actualizar balance en la UI inmediatamente
            runOnUiThread {
                updateMetricsUI(
                    grossProfit = totalGrossProfit,
                    swap = totalSwap,
                    netProfit = totalNetProfit,
                    commission = totalCommission,
                    accountCurrency = accountCurrency,
                    accountBalance = updatedBalance
                )
            }
        }
    }



    private fun updateMetricsUI(
        grossProfit: Double,
        swap: Double,
        netProfit: Double,
        commission: Double,
        accountBalance: Double,
        accountCurrency: String
    ) {
        findViewById<EditText>(R.id.TotalswapEditText).setText(String.format("%.2f", swap))
        findViewById<EditText>(R.id.TotalcommissionEditText).setText(
            String.format(
                "%.2f",
                commission
            )
        )
        findViewById<EditText>(R.id.grossProfitEditText).setText(String.format("%.2f", grossProfit))

        // 🔹 Solo mostrar el balance, sin recalcularlo manualmente
        findViewById<EditText>(R.id.balanceEditText).setText(
            "%.2f %s".format(accountBalance, accountCurrency)
        )
    }


    private fun setupECharts(rootView: View, dataX: List<String>, dataY: List<Double>) {
        val webView = findViewById<WebView>(R.id.chartWebViewTest)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("file:///android_asset/echarts.html")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Formatear datos para enviar al gráfico
                val jsonX = dataX.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
                val jsonY = dataY.joinToString(prefix = "[", postfix = "]")

                // Ejecutar JavaScript en WebView
                val jsCode = "updateChart($jsonX, $jsonY);"
                webView.evaluateJavascript(jsCode, null)
            }
        }
    }



    // Método para generar datos del gráfico
    // Genera los datos para el gráfico de Movimientos vs Balance
    private fun generateChartData(
        movements: List<Movement>,
        initialDeposit: Double
    ): Pair<List<String>, List<Double>> {
        val dataX = mutableListOf<String>() // Eje X: Número de movimientos
        val dataY = mutableListOf<Double>() // Eje Y: Balance acumulado

        var currentBalance = initialDeposit
        dataX.add("Depósito") // 🔹 Primera entrada del gráfico
        dataY.add(initialDeposit) // 🔹 El balance comienza en el depósito inicial

        movements.forEachIndexed { index, movement ->
            currentBalance += movement.profit ?: 0.0 // 🔹 Sumar o restar la ganancia/pérdida
            dataX.add("Movimiento ${index + 1}")
            dataY.add(currentBalance)
        }

        return Pair(dataX, dataY)
    }


    private fun setupEmotionChart(rootView: View, emotionData: Map<String, Int>) {
        val webView = rootView.findViewById<WebView>(R.id.emotionsChartWebView)

        // Configuración del WebView
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("file:///android_asset/emotions_chart.html") // Archivo HTML de ECharts

        // Ajustar dimensiones
        val layoutParams = webView.layoutParams
        layoutParams.height = 800
        webView.layoutParams = layoutParams

        // Generar los datos de emociones
        val pieData = emotionData.entries.joinToString(prefix = "[", postfix = "]") { entry ->
            "{value: ${entry.value}, name: '${entry.key}'}"
        }

        // Cargar los datos en el gráfico
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webView.evaluateJavascript("updatePieChart($pieData);", null)
            }
        }
    }


    private fun updateEmotionChart(emotionData: Map<String, Int>) {
        val webView = findViewById<WebView>(R.id.emotionsChartWebView)

        if (webView == null) {
            Log.e("EmotionChart", "El WebView no está inicializado.")
            return
        }

        // Convierte el mapa de datos en un formato JSON para el gráfico
        val pieData = emotionData.entries.joinToString(prefix = "[", postfix = "]") { entry ->
            "{value: ${entry.value}, name: '${entry.key}'}"
        }

        // Asegúrate de que el WebView haya terminado de cargar
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Ejecutar JavaScript para actualizar el gráfico
                webView.evaluateJavascript("updatePieChart($pieData);", null)
            }
        }

        if (webView.url == null) {
            // Si aún no se ha cargado el HTML, cárgalo aquí
            webView.loadUrl("file:///android_asset/emotions_chart.html")
        } else {
            // Si ya está cargado, actualiza directamente
            webView.evaluateJavascript("updatePieChart($pieData);", null)
        }
    }

    override fun showAdjustCommissionDialog(position: Int) {
        val movement = movementsList[position]
        val dialogView = layoutInflater.inflate(R.layout.dialog_adjust_profit, null)
        val editText = dialogView.findViewById<EditText>(R.id.profitEditText)
        editText.hint = "Introduce la nueva comisión"
        editText.setText(movement.commission?.toString() ?: "")

        AlertDialog.Builder(this)
            .setTitle("Ajustar comisión")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val newCommission = editText.text.toString().toDoubleOrNull()
                if (newCommission != null) {
                    updateMovementCommission(position, newCommission)
                } else {
                    Toast.makeText(this, "Por favor, introduce un valor válido", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateMovementCommission(position: Int, newCommission: Double) {
        val movement = movementsList[position]
        val updatedProfit = calcularBeneficioPorSimbolo(
            precioEntrada = movement.entryPrice,
            precioSalida = movement.exitPrice,
            lotes = movement.lotes,
            swap = movement.swap ?: 0.0, // Usa el swap actual
            comision = newCommission,   // Usa la nueva comisión
            simbolo = movement.symbol,
            multiplicadorManual = calcularMultiplicadorPorSimbolo(movement.symbol),
            esCompra = movement.type == "Buy"
        )

        FirebaseFirestore.getInstance()
            .collection("accounts")
            .document(movement.accountId)
            .collection("movements")
            .document(movement.id)
            .update("commission", newCommission, "profit", updatedProfit)
            .addOnSuccessListener {
                movementsList[position] =
                    movement.copy(commission = newCommission, profit = updatedProfit)
                movementsAdapter.notifyItemChanged(position)
                Toast.makeText(this, "Comisión y beneficio actualizados", Toast.LENGTH_SHORT).show()

                // Recalcular métricas y actualizar la interfaz
                calculateMetrics()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun showAdjustSwapDialog(position: Int) {
        val movement = movementsList[position]
        val dialogView = layoutInflater.inflate(R.layout.dialog_adjust_profit, null)
        val editText = dialogView.findViewById<EditText>(R.id.profitEditText)
        editText.hint = "Introduce el nuevo swap"
        editText.setText(movement.swap?.toString() ?: "")

        AlertDialog.Builder(this)
            .setTitle("Ajustar swap")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val newSwap = editText.text.toString().toDoubleOrNull()
                if (newSwap != null) {
                    updateMovementSwap(position, newSwap)
                } else {
                    Toast.makeText(this, "Por favor, introduce un valor válido", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateMovementSwap(position: Int, newSwap: Double) {
        val movement = movementsList[position]
        val updatedProfit = calcularBeneficioPorSimbolo(
            precioEntrada = movement.entryPrice,
            precioSalida = movement.exitPrice,
            lotes = movement.lotes,
            swap = newSwap,            // Usa el nuevo swap
            comision = movement.commission ?: 0.0, // Usa la comisión actual
            simbolo = movement.symbol,
            multiplicadorManual = calcularMultiplicadorPorSimbolo(movement.symbol),
            esCompra = movement.type == "Buy"
        )

        FirebaseFirestore.getInstance()
            .collection("accounts")
            .document(movement.accountId)
            .collection("movements")
            .document(movement.id)
            .update("swap", newSwap, "profit", updatedProfit)
            .addOnSuccessListener {
                movementsList[position] = movement.copy(swap = newSwap, profit = updatedProfit)
                movementsAdapter.notifyItemChanged(position)
                Toast.makeText(this, "Swap y beneficio actualizados", Toast.LENGTH_SHORT).show()

                // Recalcular métricas y actualizar la interfaz
                calculateMetrics()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserDynamicImages(movement: Movement) {
        val db = Firebase.firestore
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Log para verificar los datos
        Log.d("UpdateDebug", "Datos enviados a Firebase:")
        Log.d("UpdateDebug", "EmotionalState: ${movement.emotionalState}")
        Log.d("UpdateDebug", "Emotion: ${movement.emotion}")
        Log.d("UpdateDebug", "TradingStyle: ${movement.tradingStyle}")

        // Datos a actualizar
        val updatedData = mapOf(
            "psico" to (movement.emotionalState ?: "Desconocido"),
            "emotion" to (movement.emotion ?: "Sin emoción"),
            "trading_style" to (movement.tradingStyle ?: "No definido")
        )

        db.collection("users")
            .document(userId)
            .update(updatedData)
            .addOnSuccessListener {
                Log.d("DynamicImages", "Datos del usuario actualizados correctamente.")
            }
            .addOnFailureListener { e ->
                Log.e("DynamicImages", "Error al actualizar datos del usuario: ${e.message}")
            }
    }

    private fun determineTradingStyle(entryTime: Long, exitTime: Long?): String {
        if (exitTime == null) return "Desconocido" // Si no hay tiempo de salida, no se puede calcular

        val durationInMinutes = (exitTime - entryTime) / (1000 * 60) // Diferencia en minutos

        return when {
            durationInMinutes < 10 -> "Scalping"
            durationInMinutes < 1440 -> "Day Trading" // Cambiamos "Intradia" por "Day Trading"
            else -> "Swing Trading"
        }
    }

    private fun formatEmotionalState(state: String?): String {
        return when (state?.lowercase()) {
            "psico+" -> "Psico +"
            "psico-" -> "Psico -"
            else -> state ?: "Desconocido"
        }
    }

    private fun updateAccountBalance(accountId: String, newBalance: Double) {
        val db = FirebaseFirestore.getInstance()
        db.collection("accounts").document(accountId)
            .update("balance", newBalance)
            .addOnSuccessListener {
                Log.d("UpdateBalance", "Balance actualizado en Firestore: $newBalance")

                // 🔹 Actualizar UI sin necesidad de recargar la actividad
                runOnUiThread {
                    findViewById<EditText>(R.id.balanceEditText).setText("%.2f".format(newBalance))
                }
            }
            .addOnFailureListener { e ->
                Log.e("UpdateBalance", "Error al actualizar balance: ${e.message}")
            }
    }
    private fun setupSuccessRatioChart(rootView: View) {
        val webView = rootView.findViewById<WebView>(R.id.successRatioChartWebView)

        webView.settings.javaScriptEnabled = true
        webView.loadUrl("file:///android_asset/success_ratio_chart.html") // ✅ Archivo correcto

        val totalTrades = movementsList.size
        val successfulTrades = movementsList.count { it.profit ?: 0.0 > 0 }
        val successRate = if (totalTrades > 0) (successfulTrades.toDouble() / totalTrades) * 100 else 0.0

        Log.d("SuccessChart", "Total operaciones: $totalTrades, Exitosas: $successfulTrades, Ratio: $successRate")

        // Esperar a que el HTML se cargue antes de llamar a la función de actualización
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("SuccessChart", "ECharts HTML cargado correctamente, actualizando gráfico...")

                // Llama a la función JavaScript del HTML con el porcentaje de éxito
                val jsCode = "updateSuccessRate($successRate);"
                webView.evaluateJavascript(jsCode, null)
            }
        }
    }
}
