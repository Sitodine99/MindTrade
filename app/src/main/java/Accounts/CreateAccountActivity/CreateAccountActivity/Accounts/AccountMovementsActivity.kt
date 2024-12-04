package com.example.mindtrade

import MovementsAdapter
import ScreenPagerAdapter
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.mindtrade.model.Movement
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import java.util.Calendar
import java.util.UUID

class AccountMovementsActivity : AppCompatActivity() {

    private val movementsList = mutableListOf<Movement>() // Lista de movimientos
    private lateinit var movementsAdapter: MovementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_movements)

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
                            setupMovementsView(movementsView) // Llama al método aquí
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

    private fun setupMovementsView(rootView: View) {
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.movementsRecyclerView)
        val addMovementButton = rootView.findViewById<ImageButton>(R.id.addMovementButton)


        // Inicializa el adaptador con el contexto y la lista de movimientos
        movementsAdapter = MovementsAdapter(this, movementsList)

        // Configurar el RecyclerView
        recyclerView.adapter = movementsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Agregar funcionalidad al botón (ejemplo)
        addMovementButton.setOnClickListener {
            // Lógica para añadir un nuevo movimiento
            showMovementDialog()
        }
    }


    private fun showMovementDialog() {
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
        val emotionSpinner = dialogView.findViewById<Spinner>(R.id.emotionSpinner)
        val emotionalStateSpinner = dialogView.findViewById<Spinner>(R.id.emotionalStateSpinner)
        val commentsEditText = dialogView.findViewById<EditText>(R.id.commentsEditText)

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
                "US30.cash", "SPX500.cash", "NAS100.cash",
                "GER30.cash", "FRA40.cash", "UK100.cash", "ESP35.cash",
                "JPN225.cash", "AUS200.cash"
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

        // Crear lista de símbolos con cabeceras
        val symbolsWithHeaders = mutableListOf<String>().apply {
            add("Selecciona un Activo")
            symbolGroups.forEach { (header, symbols) ->
                add("**$header**") // Header
                addAll(symbols)    // Symbols
            }
        }

        // Adaptador para el Spinner
        val spinnerAdapter = object :
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
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        symbolsSpinner.adapter = spinnerAdapter

        // Selección del Spinner
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
                    addSymbolEditText.isEnabled = false
                    addSymbolButton.isEnabled = false
                    customSymbolsChipGroup.removeAllViews()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Agregar símbolo personalizado
        addSymbolButton.setOnClickListener {
            val newSymbol = addSymbolEditText.text.toString().trim()
            if (newSymbol.isNotEmpty()) {
                if (customSymbolsChipGroup.childCount == 0) { // Permitir solo un chip
                    // Crear un chip con el nuevo símbolo
                    val chip = Chip(this).apply {
                        text = newSymbol
                        isCloseIconVisible = true
                        setOnCloseIconClickListener {
                            customSymbolsChipGroup.removeView(this)
                            // Habilitar el Spinner y los elementos de entrada al eliminar el chip
                            symbolsSpinner.isEnabled = true
                            addSymbolEditText.isEnabled = true
                            addSymbolButton.isEnabled = true
                        }
                    }
                    // Añadir el chip al ChipGroup
                    customSymbolsChipGroup.addView(chip)

                    // Limpiar el campo de texto
                    addSymbolEditText.text.clear()

                    // Deshabilitar el Spinner y los elementos de entrada ya que se ha añadido un chip
                    symbolsSpinner.isEnabled = false
                    addSymbolEditText.isEnabled = false
                    addSymbolButton.isEnabled = false
                } else {
                    Toast.makeText(this, "Solo puedes añadir un símbolo personalizado", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Mostrar un mensaje si el campo de texto está vacío
                Toast.makeText(this, "Introduce un símbolo válido", Toast.LENGTH_SHORT).show()
            }
        }




        // Configuración de fechas
        entryDateButton.setOnClickListener {
            showDateTimePicker { selectedDateTime ->
                entryDateTime = selectedDateTime
                entryDateButton.text = "Fecha Entrada: ${formatDate(selectedDateTime)}"
            }
        }
        exitDateButton.setOnClickListener {
            showDateTimePicker { selectedDateTime ->
                exitDateTime = selectedDateTime
                exitDateButton.text = "Fecha Salida: ${formatDate(selectedDateTime)}"
            }
        }

        // Mostrar diálogo
        val dialog = AlertDialog.Builder(this)
            .setTitle("Registrar Movimiento")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Registrar") { _, _ ->
                if (selectedSymbol == null && customSymbolsChipGroup.childCount == 0) {
                    Toast.makeText(
                        this,
                        "Por favor selecciona o añade un símbolo",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val finalSymbol = if (symbolsSpinner.isEnabled) selectedSymbol else {
                    val customChip = customSymbolsChipGroup.getChildAt(0) as Chip
                    customChip.text.toString()
                }

                // Verifica que finalSymbol no sea nulo
                if (finalSymbol.isNullOrBlank()) {
                    Toast.makeText(
                        this,
                        "Error: No se ha seleccionado un símbolo válido",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val entryPrice = entryPriceEditText.text.toString().toDoubleOrNull() ?: 0.0
                val exitPrice = exitPriceEditText.text.toString().toDoubleOrNull() ?: 0.0
                val swap = swapEditText.text.toString().toDoubleOrNull() ?: 0.0
                val commission = commissionEditText.text.toString().toDoubleOrNull() ?: 0.0
                val operationType = operationTypeSpinner.selectedItem.toString()
                val strategy = strategySpinner.selectedItem.toString()
                val emotion = emotionSpinner.selectedItem.toString()
                val comments = commentsEditText.text.toString()

                // Calcular beneficios y estilo de trading
                val profit = exitPrice - entryPrice - commission - swap
                val tradingStyle = if (entryDateTime != null && exitDateTime != null) {
                    val duration = (exitDateTime!! - entryDateTime!!) / (1000 * 60)
                    when {
                        duration < 10 -> "Scalping"
                        duration < 1440 -> "Intradia"
                        else -> "Swing Trading"
                    }
                } else "Desconocido"

                // Determinar estado emocional
                val positiveEmotions = listOf(
                    "Autocontrol", "Confianza", "Eficiencia", "Optimismo", "Paciencia",
                    "Realización", "Satisfacción", "Seguridad", "Sintonía", "Tranquilidad",
                    "Aceptación", "Afirmación"
                )
                val negativeEmotions = listOf(
                    "Ansiedad", "Impaciencia", "Descontrol", "Avaricia", "Insatisfacción",
                    "Rabia", "Vergüenza", "Confusión", "Miedo", "Fatalismo",
                    "Frustración", "Ineficacia"
                )
                val emotionalState = if (positiveEmotions.contains(emotion)) "Psico+" else "Psico-"

                val movement = Movement(
                    id = UUID.randomUUID().toString(),
                    accountId = "account_id",
                    symbol = finalSymbol,
                    type = operationType,
                    entryPrice = entryPrice,
                    exitPrice = exitPrice,
                    swap = swap,
                    commission = commission,
                    profit = profit,
                    strategyId = strategy,
                    emotionalState = emotionalState,
                    entryTime = entryDateTime ?: 0L,
                    exitTime = exitDateTime ?: 0L,
                    photos = listOf(),
                    tradingStyle = tradingStyle,
                    comments = comments
                )

                // Llamar al método para guardar el movimiento en Firebase
                saveMovementToFirebase(movement)

                Toast.makeText(
                    this,
                    "Movimiento registrado: $finalSymbol, Profit: $profit",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .create()
        dialog.show()
    }


        private fun saveMovementToFirebase(movement: Movement) {
        val db = Firebase.firestore // Instancia de Firestore

        val movementData = hashMapOf(
            "id" to movement.id,
            "accountId" to movement.accountId,
            "symbol" to movement.symbol,
            "type" to movement.type,
            "entryPrice" to movement.entryPrice,
            "exitPrice" to movement.exitPrice,
            "swap" to movement.swap,
            "commission" to movement.commission,
            "profit" to movement.profit,
            "strategyId" to movement.strategyId,
            "emotionalState" to movement.emotionalState,
            "entryTime" to movement.entryTime,
            "exitTime" to movement.exitTime,
            "photos" to movement.photos,
            "tradingStyle" to movement.tradingStyle,
            "comments" to movement.comments
        )

        db.collection("movements")
            .document(movement.id)
            .set(movementData)
            .addOnSuccessListener {
                updateAccountWithMovement(movement.accountId, movement.id)
                Toast.makeText(this, "Movimiento guardado exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateAccountWithMovement(accountId: String, movementId: String) {
        val db = Firebase.firestore

        // Actualizar el campo "movements" de la cuenta
        val accountRef = db.collection("accounts").document(accountId)

        accountRef.update("movements", FieldValue.arrayUnion(movementId))
            .addOnSuccessListener {
                Toast.makeText(this, "Movimiento registrado correctamente.", Toast.LENGTH_SHORT)
                    .show()
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

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val timePickerDialog = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                        callback(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()
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
}
