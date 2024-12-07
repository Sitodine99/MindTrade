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
import android.util.Log
import android.view.View
import android.view.ViewGroup
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


    private val symbolGroups = mapOf(
        "Forex" to listOf(
            "EUR/USD", "USD/JPY", "GBP/USD", "USD/CHF",
            "AUD/USD", "USD/CAD", "NZD/USD"
        ).map { it.trim().uppercase() }, // Normalizar a mayúsculas
        "Exotics" to listOf("USD/SEK", "USD/NOK", "USD/ZAR", "EUR/TRY").map { it.trim().uppercase() },
        "Metals" to listOf("XAU/USD", "XAG/USD", "XPT/USD", "XPD/USD").map { it.trim().uppercase() },
        "Crypto" to listOf("BTC/USD", "ETH/USD", "LTC/USD", "XRP/USD", "ADA/USD", "DOT/USD").map { it.trim().uppercase() },
        "Cash CFD" to listOf(
            "US30.cash", "SPX500.cash", "NAS100.cash", "GER30.cash", "FRA40.cash",
            "UK100.cash", "ESP35.cash", "JPN225.cash", "AUS200.cash"
        ).map { it.trim().uppercase() },
        "Commodities" to listOf(
            "SOYBEAN", "WHEAT", "CORN", "COFFEE", "COCOA", "USOIL", "NATGAS"
        ).map { it.trim().uppercase() },
        "Equities" to listOf("AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "META", "NFLX", "NVDA").map { it.trim().uppercase() }
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
            R.layout.layout_screen_details,
            R.layout.layout_screen_movements,
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


        // Escuchar los cambios de página del ViewPager2 para actualizar las vistas
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> { // Pantalla de detalles
                        val screenDetailsView = viewPager.findViewWithTag<View>("f0")
                        screenDetailsView?.let {
                            it.findViewById<TextView>(R.id.accountNameTextView).text = accountName
                            it.findViewById<TextView>(R.id.accountBalanceTextView).text =
                                "Balance: $%.2f".format(accountBalance)
                            it.findViewById<TextView>(R.id.accountCurrencyTextView).text =
                                "Divisa: $accountCurrency"
                            it.findViewById<TextView>(R.id.accountProfitTargetTextView).text =
                                "Objetivo: $%.2f".format(accountProfitTarget)
                            it.findViewById<TextView>(R.id.accountMaxDailyLossTextView).text =
                                "Máx pérdida diaria: $%.2f".format(accountMaxDailyLoss)
                            it.findViewById<TextView>(R.id.accountCreationDateTextView).text =
                                "Creada el: $formattedDate"

                            // Inicializar el gráfico desde esta vista
                            setupLineChart(it, accountBalance)
                        }
                    }

                    1 -> { // Pantalla de movimientos
                        val screenMovementsView = viewPager.findViewWithTag<View>("f1")
                        screenMovementsView?.let { movementsView ->
                            movementsView.findViewById<TextView>(R.id.accountMovementsCountTextView).text =
                                "Movimientos: ${accountMovements.size}"
                            // Configura el RecyclerView de movimientos aquí
                            setupMovementsView(movementsView, accountId) // Llama al método aquí
                        }
                    }
                    // Maneja más pantallas si es necesario
                }
            }
        })
    }

    private fun setupLineChart(rootView: View, initialBalance: Float) {
        val lineChart = rootView.findViewById<LineChart>(R.id.balanceChart)

        // Configurar propiedades del gráfico
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.setDrawGridBackground(false)
        lineChart.axisRight.isEnabled = false // Deshabilitar eje derecho

        // Configurar el eje X
        val xAxis: XAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f // Incrementos de 1 en el eje X
        xAxis.setDrawGridLines(false)
        xAxis.axisMinimum = 0f // Inicia desde 0 en el eje X
        xAxis.labelCount = 5 // Máximo número de etiquetas visibles

        // Configurar el eje Y
        val yAxis: YAxis = lineChart.axisLeft
        yAxis.setDrawGridLines(true)
        yAxis.setDrawZeroLine(true) // Línea en el 0 del eje Y
        yAxis.axisMinimum = 0f // No permite valores negativos

        // Añadir un punto inicial al gráfico
        val initialData = mutableListOf<Entry>()
        initialData.add(Entry(0f, initialBalance)) // Número de operaciones = 0, Balance inicial

        val dataSet = LineDataSet(initialData, "Balance")
        dataSet.color = resources.getColor(R.color.turquoise_blue, theme)
        dataSet.setDrawCircles(true)
        dataSet.setDrawValues(true)

        // Añadir los datos al gráfico
        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate() // Redibujar el gráfico
    }

    private fun setupMovementsView(rootView: View, accountId: String) {
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.movementsRecyclerView)
        val addMovementButton = rootView.findViewById<ImageButton>(R.id.addMovementButton)

        // Configurar el adaptador
        movementsAdapter = MovementsAdapter(this, movementsList, this) // 'this' implementa MovementActionListener

        recyclerView.adapter = movementsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar movimientos desde Firestore
        loadMovementsFromFirestore(accountId)

        // Botón para añadir un nuevo movimiento
        addMovementButton.setOnClickListener {
            showMovementDialog(accountId, strategies)
        }
    }

    private fun loadMovementsFromFirestore(accountId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("accounts")
            .document(accountId)
            .collection("movements")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error al cargar movimientos: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Manejamos cambios en tiempo real directamente desde Firestore
                    movementsList.clear()
                    for (doc in snapshot.documents) {
                        val movement = doc.toObject(Movement::class.java)
                        if (movement != null) {
                            movementsList.add(movement)
                        }
                    }
                    movementsAdapter.notifyDataSetChanged()

                    // Actualizar contador de movimientos directamente desde aquí
                    updateMovementsCountUI()
                }
            }
    }

    // Actualizar la UI del contador de movimientos
    private fun updateMovementsCountUI() {
        val movementsCountTextView = findViewById<TextView>(R.id.accountMovementsCountTextView)
        movementsCountTextView.text = "Movimientos: ${movementsList.size}"
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
                val entryPrice = entryPriceEditText.text.toString().replace(",", ".").toDoubleOrNull()
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
                    simbolo = finalSymbol, // Pasamos el símbolo para calcular el multiplicador automático si aplica
                    multiplicadorManual = null, // Ya lo calculamos arriba
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
                    emotionalState = selectedEmotion,
                    emotion = selectedEmotionDetail,
                    tradingStyle = "Swing", // Según tu lógica
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

        // Guardar el movimiento en la colección de movimientos
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
                val mediaPlayer = MediaPlayer.create(this, R.raw.cash)
                mediaPlayer.start()

                // No añadimos manualmente el movimiento a movementsList
                Toast.makeText(this, "Movimiento guardado exitosamente.", Toast.LENGTH_SHORT).show()

                // Opcional: actualizar estrategia o cuenta
                updateAccountWithMovement(accountId, movement.id)
                strategyId?.let { saveAndLinkMovementToStrategy(strategyId, movement) }
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
        val multiplicador = if (multiplicadorManual != null && multiplicadorManual > 0) {
            multiplicadorManual
        } else {

            calcularMultiplicadorPorSimbolo(simbolo)
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
        val movement = movementsList[position] // Obtiene el movimiento a eliminar
        val db = FirebaseFirestore.getInstance()
        val accountRef = db.collection("accounts").document(movement.accountId)
        val movementRef = accountRef.collection("movements").document(movement.id)

        movementRef.delete()
            .addOnSuccessListener {
                // Eliminar el ID del movimiento del array "movements" en el documento de cuenta
                accountRef.update("movements", FieldValue.arrayRemove(movement.id))
                    .addOnSuccessListener {
                        // Actualizar la lista local y notificar al adaptador
                        movementsList.removeAt(position)
                        movementsAdapter.notifyItemRemoved(position)
                        updateMovementsCount() // Actualizar contador en la UI
                        Toast.makeText(this, "Movimiento eliminado correctamente.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al actualizar la cuenta: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar el movimiento: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Por favor, introduce un valor válido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Actualizar el beneficio del movimiento
    private fun updateMovementProfit(position: Int, newProfit: Double) {
        val movement = movementsList[position]
        FirebaseFirestore.getInstance()
            .collection("accounts")
            .document(movement.accountId)
            .collection("movements")
            .document(movement.id)
            .update("profit", newProfit)
            .addOnSuccessListener {
                movementsList[position] = movement.copy(profit = newProfit)
                movementsAdapter.notifyItemChanged(position)
                Toast.makeText(this, "Beneficio actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateMovementsCount() {
        val accountId = movementsList.firstOrNull()?.accountId ?: return
        val db = FirebaseFirestore.getInstance()
        val accountRef = db.collection("accounts").document(accountId)

        accountRef.update("movementsCount", movementsList.size)
            .addOnSuccessListener {
                Log.d("Firebase", "Movements count updated: ${movementsList.size}")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar contador: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}


