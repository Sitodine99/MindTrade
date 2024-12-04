package com.example.mindtrade

import MovementsAdapter
import ScreenPagerAdapter
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
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

        // Llenar los grupos de chips con los símbolos predefinidos



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


        // Configuración del formulario

        val operationTypeSpinner = dialogView.findViewById<Spinner>(R.id.operationTypeSpinner)
        val entryPriceEditText = dialogView.findViewById<EditText>(R.id.entryPriceEditText)
        val exitPriceEditText = dialogView.findViewById<EditText>(R.id.exitPriceEditText)
        val swapEditText = dialogView.findViewById<EditText>(R.id.swapEditText)
        val commissionEditText = dialogView.findViewById<EditText>(R.id.commissionEditText)
        val strategySpinner = dialogView.findViewById<Spinner>(R.id.strategySpinner)
        val emotionSpinner = dialogView.findViewById<Spinner>(R.id.emotionSpinner)
        val emotionalStateSpinner = dialogView.findViewById<Spinner>(R.id.emotionalStateSpinner)
        val commentsEditText = dialogView.findViewById<EditText>(R.id.commentsEditText)
        val entryDateButton = dialogView.findViewById<Button>(R.id.entryDateButton)
        val exitDateButton = dialogView.findViewById<Button>(R.id.exitDateButton)

        var entryDateTime: Long? = null
        var exitDateTime: Long? = null

        // Configuración de selección de fecha y hora
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


        // Adaptador para el estado emocional
        val emotionalStateAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Psico+", "Psico-")
        )
        emotionalStateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        emotionalStateSpinner.adapter = emotionalStateAdapter

        // Configurar spinners
        operationTypeSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, listOf("Buy", "Sell")
        )

        // Mostrar diálogo
        val dialog = AlertDialog.Builder(this)
            .setTitle("Registrar Movimiento")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Registrar") { _, _ ->
                // Obtener los valores del formulario

                val operationType = operationTypeSpinner.selectedItem.toString()
                val entryPrice = entryPriceEditText.text.toString().toDoubleOrNull() ?: 0.0
                val exitPrice = exitPriceEditText.text.toString().toDoubleOrNull() ?: 0.0
                val swap = swapEditText.text.toString().toDoubleOrNull() ?: 0.0
                val commission = commissionEditText.text.toString().toDoubleOrNull() ?: 0.0
                val strategy = strategySpinner.selectedItem.toString()
                val emotion = emotionSpinner.selectedItem.toString()
                val comments = commentsEditText.text.toString()

                // Validaciones
                if (entryDateTime == null || exitDateTime == null) {
                    Toast.makeText(
                        this,
                        "Por favor selecciona fecha de entrada y salida",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                // Determinar estilo de trading al registrar
                val tradingStyle = if (entryDateTime != null && exitDateTime != null) {
                    val durationInMinutes = (exitDateTime!! - entryDateTime!!) / (1000 * 60)
                    when {
                        durationInMinutes < 10 -> "Scalping"
                        durationInMinutes < 1440 -> "Intradia"
                        else -> "Swing Trading"
                    }
                } else {
                    "Desconocido" // Si no hay tiempo de salida, establece un valor por defecto
                }

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

                // Calcular beneficio
                val profit =
                    if (exitPrice != null) (exitPrice - entryPrice - commission - swap) else null

                // Crear el objeto Movement
                val movementId = UUID.randomUUID().toString()
                val newMovement = Movement(
                    id = movementId,
                    accountId = "account_id", // Cambia por el ID real de la cuenta
                    //symbol = selectedSymbolChip?.text.toString(),
                    type = operationType,
                    entryPrice = entryPrice,
                    exitPrice = exitPrice,
                    swap = swap,
                    commission = commission,
                    profit = profit,
                    strategyId = strategy,
                    emotionalState = emotionalState,
                    entryTime = entryDateTime!!,
                    exitTime = exitDateTime,
                    photos = listOf(), // Inicialmente vacío
                    tradingStyle = tradingStyle,
                    comments = comments
                )

                // Guardar en Firebase
                saveMovementToFirebase(newMovement)
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
