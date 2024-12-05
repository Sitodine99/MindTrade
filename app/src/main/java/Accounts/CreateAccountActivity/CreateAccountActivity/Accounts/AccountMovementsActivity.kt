package com.example.mindtrade

import MovementsAdapter
import ScreenPagerAdapter
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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
import java.util.Calendar
import java.util.UUID

class AccountMovementsActivity : AppCompatActivity() {

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

        movementsAdapter = MovementsAdapter(this, movementsList)

        recyclerView.adapter = movementsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        addMovementButton.setOnClickListener {
            showMovementDialog(accountId, strategies)
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


        var entryDateTime: Long? = null
        var exitDateTime: Long? = null

        // Grupos de símbolos
        val symbolGroups = mapOf(
            "Forex" to listOf(
                "EUR/USD",
                "USD/JPY",
                "GBP/USD",
                "USD/CHF",
                "AUD/USD",
                "USD/CAD",
                "NZD/USD"
            ),
            "Exotics" to listOf("USD/SEK", "USD/NOK", "USD/ZAR", "EUR/TRY"),
            "Metals" to listOf("XAU/USD", "XAG/USD", "XPT/USD", "XPD/USD"),
            "Crypto" to listOf("BTC/USD", "ETH/USD", "LTC/USD", "XRP/USD", "ADA/USD", "DOT/USD"),
            "Cash CFD" to listOf(
                "US30.cash", "SPX500.cash", "NAS100.cash", "GER30.cash", "FRA40.cash",
                "UK100.cash", "ESP35.cash", "JPN225.cash", "AUS200.cash"
            ),
            "Commodities" to listOf(
                "SOYBEAN",
                "WHEAT",
                "CORN",
                "COFFEE",
                "COCOA",
                "USOIL",
                "NATGAS"
            ),
            "Equities" to listOf("AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "META", "NFLX", "NVDA")
        )

        val symbolsWithHeaders = mutableListOf<String>().apply {
            add("Selecciona un Activo")
            symbolGroups.forEach { (header, symbols) ->
                add("**$header**")
                addAll(symbols)
            }
        }

        // Configuración del Spinner de símbolos
        val symbolsAdapter = object :
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, symbolsWithHeaders) {
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

        var selectedSymbol: String? = null
        symbolsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val symbol = symbolsWithHeaders[position]
                if (!symbol.startsWith("**") && position != 0) {
                    selectedSymbol = symbol
                    if (customSymbolsChipGroup.childCount > 0) {
                        customSymbolsChipGroup.removeAllViews()
                    }
                } else {
                    selectedSymbol = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

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
                        }
                    }
                    customSymbolsChipGroup.addView(chip)
                    addSymbolEditText.text.clear()
                    symbolsSpinner.setSelection(0)
                    selectedSymbol = null
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
                // Validaciones
                val finalSymbol = if (symbolsSpinner.isEnabled) selectedSymbol else {
                    val customChip = customSymbolsChipGroup.getChildAt(0) as Chip
                    customChip.text.toString()
                }

                if (selectedSymbol == null && customSymbolsChipGroup.childCount == 0) {
                    Toast.makeText(
                        this,
                        "Por favor selecciona o añade un símbolo",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener // Salimos sin cerrar el diálogo
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

                val entryPrice = entryPriceEditText.text.toString().toDoubleOrNull() ?: 0.0
                val exitPrice = exitPriceEditText.text.toString().toDoubleOrNull() ?: 0.0
                val swap = swapEditText.text.toString().toDoubleOrNull() ?: 0.0
                val commission = commissionEditText.text.toString().toDoubleOrNull() ?: 0.0
                val operationType = operationTypeSpinner.selectedItem.toString()
                val profit = exitPrice - entryPrice - commission - swap

                // Creamos el objeto Movement
                val movement = Movement(
                    id = UUID.randomUUID().toString(),
                    accountId = accountId,
                    symbol = finalSymbol ?: "Símbolo no especificado",
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
            "strategyId" to strategyId,
            "emotion" to movement.emotion,
            "tradingStyle" to movement.tradingStyle,
            "comments" to movement.comments,
            "photos" to movement.photos
        )

            db.collection("accounts")
                .document(accountId)
                .collection("movements") // Subcolección de movimientos
                .document(movement.id)
                .set(movementData)
                .addOnSuccessListener {
                    // Actualizar la lista de movimientos de la cuenta
                    updateAccountWithMovement(accountId, movement.id)

                    // Si hay una estrategia asociada, guardar el movimiento allí también
                    strategyId?.let {
                        saveAndLinkMovementToStrategy(strategyId, movement)
                    }


                    Toast.makeText(this, "Movimiento guardado exitosamente.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar el movimiento: ${e.message}", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Error al actualizar la cuenta: ${e.message}", Toast.LENGTH_SHORT).show()
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
                val strategy = doc.toObject(Strategy::class.java).copy(id = doc.id) // Añade el ID del documento
                strategies.add(strategy)
            }

            favoriteStrategiesQuery.get().addOnSuccessListener { favoriteStrategiesSnapshot ->
                for (doc in favoriteStrategiesSnapshot) {
                    val strategy = doc.toObject(Strategy::class.java).copy(id = doc.id) // Añade el ID del documento
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
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
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
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
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
                        Log.d("MovementDebug", "Movimiento guardado correctamente en la subcolección.")
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

}


