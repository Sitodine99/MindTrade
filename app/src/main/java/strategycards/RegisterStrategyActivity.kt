package strategycards

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.bumptech.glide.Glide
import com.example.mindtrade.R
import com.example.mindtrade.databinding.ActivityRegisterStrategyBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class RegisterStrategyActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var entryConditionEditText: EditText
    private lateinit var exitConditionEditText: EditText
    private lateinit var generalConsiderationsEditText: EditText


    private lateinit var dayTradingCheckBox: CheckBox
    private lateinit var scalpingCheckBox: CheckBox
    private lateinit var swingTradingCheckBox: CheckBox
    private lateinit var chipGroup: ChipGroup
    private lateinit var addIndicatorEditText: EditText
    private lateinit var addIndicatorButton: Button
    private lateinit var predefinedIndicatorsChipGroup: ChipGroup
    private lateinit var timeFramesCheckBoxes: List<CheckBox>
    private lateinit var algorithmEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var symbolsChipGroup: ChipGroup
    private lateinit var customSymbolsChipGroup: ChipGroup
    private lateinit var allInstrumentsChipGroup: ChipGroup
    private lateinit var forexChipGroup: ChipGroup
    private lateinit var exoticsChipGroup: ChipGroup
    private lateinit var metalsChipGroup: ChipGroup
    private lateinit var cryptoChipGroup: ChipGroup
    private lateinit var cashCFDChipGroup: ChipGroup
    private lateinit var commoditiesChipGroup: ChipGroup
    private lateinit var equitiesChipGroup: ChipGroup
    private lateinit var addSymbolEditText: EditText
    private lateinit var addSymbolButton: Button

    private lateinit var entryImageView: ImageView
    private lateinit var exitImageView: ImageView


    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private var entryImageUrl: String? = null
    private var exitImageUrl: String? = null
    private var selectedImageType: String = "" // "entry" o "exit"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_strategy)
        entryImageView = findViewById(R.id.entryImageView)
        exitImageView = findViewById(R.id.exitImageView)

        // Referenciar elementos del diseño
        titleEditText = findViewById(R.id.strategyTitle)
        entryConditionEditText = findViewById(R.id.entryConditionEditText)
        exitConditionEditText = findViewById(R.id.exitConditionEditText)
        generalConsiderationsEditText = findViewById(R.id.generalConsiderationsEditText)
        dayTradingCheckBox = findViewById(R.id.tradingStyleDayTrading)
        scalpingCheckBox = findViewById(R.id.tradingStyleScalping)
        swingTradingCheckBox = findViewById(R.id.tradingStyleSwingTrading)
        chipGroup = findViewById(R.id.indicatorChipGroup)
        predefinedIndicatorsChipGroup = findViewById(R.id.predefinedIndicatorChipGroup)
        addIndicatorEditText = findViewById(R.id.addIndicatorEditText)
        addIndicatorButton = findViewById(R.id.addIndicatorButton)
        algorithmEditText = findViewById(R.id.tradingAlgorithmCode)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        symbolsChipGroup = findViewById(R.id.symbolsChipGroup)
        customSymbolsChipGroup = findViewById(R.id.customSymbolsChipGroup)
        allInstrumentsChipGroup = findViewById(R.id.allInstrumentsChipGroup)
        forexChipGroup = findViewById(R.id.forexChipGroup)
        exoticsChipGroup = findViewById(R.id.exoticsChipGroup)
        metalsChipGroup = findViewById(R.id.metalsChipGroup)
        cryptoChipGroup = findViewById(R.id.cryptoChipGroup)
        cashCFDChipGroup = findViewById(R.id.cashCFDChipGroup)
        commoditiesChipGroup = findViewById(R.id.commoditiesChipGroup)
        equitiesChipGroup = findViewById(R.id.equitiesChipGroup)
        addSymbolEditText = findViewById(R.id.addSymbolEditText)
        addSymbolButton = findViewById(R.id.addSymbolButton)

        //Botón para abrir un diálogo donde el usuario pueda elegir qué tipo de imagen gestionar:
        val uploadImageButton: ImageButton = findViewById(R.id.uploadImageButton)
        uploadImageButton.setOnClickListener {
            showImageManagementDialog()
        }


        // Cargar datos de estrategia si es modo edición
        val strategyId = intent.getStringExtra("strategyId")
        if (strategyId != null) {
            loadStrategyData(strategyId)
        }

        // Temporalidades
        timeFramesCheckBoxes = listOf(
            findViewById(R.id.timeM1),
            findViewById(R.id.timeM3),
            findViewById(R.id.timeM5),
            findViewById(R.id.timeM15),
            findViewById(R.id.timeM30),
            findViewById(R.id.timeH1),
            findViewById(R.id.timeH4),
            findViewById(R.id.timeD1),
            findViewById(R.id.timeW1),
            findViewById(R.id.timeOtras)
        )

        // Inicializar etiquetas predefinidas
        setupPredefinedIndicators()

        // Configurar botones
        setupButtons()

        // Configurar chips para símbolos
        setupPredefinedSymbols()

        // Imagen gestión
        findViewById<ImageButton>(R.id.uploadImageButton).setOnClickListener {
            showImageManagementDialog()
        }

        // Cargar datos si es edición
        intent.getStringExtra("strategyId")?.let { loadStrategyData(it) }
    }

    fun toggleGroupVisibility(view: View) {
        val chipGroup: ChipGroup? = when (view.id) {

            R.id.indicatorHeader -> findViewById(R.id.predefinedIndicatorChipGroup)
            R.id.allInstrumentsHeader -> findViewById(R.id.allInstrumentsChipGroup)
            R.id.forexHeader -> findViewById(R.id.forexChipGroup)
            R.id.exoticsHeader -> findViewById(R.id.exoticsChipGroup)
            R.id.cashCFDHeader -> findViewById(R.id.cashCFDChipGroup)
            R.id.metalsHeader -> findViewById(R.id.metalsChipGroup)
            R.id.commoditiesHeader -> findViewById(R.id.commoditiesChipGroup)
            R.id.cryptoHeader -> findViewById(R.id.cryptoChipGroup)
            R.id.equitiesHeader -> findViewById(R.id.equitiesChipGroup)

            else -> null
        }

        if (chipGroup != null) {
            if (chipGroup.visibility == View.VISIBLE) {
                chipGroup.visibility = View.GONE
                (view as TextView).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand, 0)
            } else {
                chipGroup.visibility = View.VISIBLE
                (view as TextView).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_collapse, 0)
            }
        }
    }


    private fun setupPredefinedIndicators() {
        val predefinedIndicators = listOf(
            "Sin Indicadores", "EMA21", "EMA50", "EMA200", "SMA21", "SMA50", "SMA200",
            "MACD", "RSI", "Bollinger Bands", "ADX", "Ichimoku",
            "Volume", "Fibonacci Retracements", "Pivot Points"
        )

        for (indicator in predefinedIndicators) {
            val chip = Chip(this).apply {
                text = indicator
                isCloseIconVisible = false // No mostramos el ícono de cerrar inicialmente
                setOnClickListener {
                    if (indicator == "Sin Indicadores") {
                        if (isChipSelected(chipGroup, indicator)) {
                            Toast.makeText(this@RegisterStrategyActivity, "Ya has seleccionado 'Sin Indicadores'", Toast.LENGTH_SHORT).show()
                        } else {
                            clearAllIndicators()
                            addIndicatorToChipGroup(indicator)
                        }
                    } else {
                        if (isChipSelected(chipGroup, "Sin Indicadores")) {
                            Toast.makeText(this@RegisterStrategyActivity, "Has seleccionado 'Sin Indicadores'", Toast.LENGTH_SHORT).show()
                        } else {
                            addIndicatorToChipGroup(indicator)
                        }
                    }
                }
            }
            predefinedIndicatorsChipGroup.addView(chip)
        }
    }

    private fun clearAllIndicators() {
        // Elimina todos los chips del grupo personalizado de indicadores
        chipGroup.removeAllViews()
    }


    private fun setupPredefinedSymbols() {
        // Configurar el chip "Todos los Activos"
        val allInstrumentsSymbols = listOf("Todos los Activos")
        for (symbol in allInstrumentsSymbols) {
            val chip = Chip(this).apply {
                text = symbol
                isCloseIconVisible = false // Permitimos que se elimine manualmente
                setOnClickListener {
                    // Si ya está seleccionado, mostrar mensaje
                    if (isChipSelected(symbolsChipGroup, symbol)) {
                        Toast.makeText(this@RegisterStrategyActivity, "Ya has seleccionado 'Todos los Activos'", Toast.LENGTH_SHORT).show()
                    } else {
                        // Limpiar todos los símbolos y añadir "Todos los Activos"
                        clearAllSymbols()
                        addSymbolToChipGroup(symbol)
                    }
                }
                setOnCloseIconClickListener {
                    symbolsChipGroup.removeView(this) // Eliminar manualmente el chip
                }
            }
            allInstrumentsChipGroup.addView(chip)
        }

        val predefinedSymbolsMap = mapOf(
            forexChipGroup to listOf("EUR/USD", "USD/JPY", "GBP/USD", "USD/CHF", "AUD/USD", "USD/CAD", "NZD/USD"),
            exoticsChipGroup to listOf("USD/SEK", "USD/NOK", "USD/ZAR", "EUR/TRY"),
            metalsChipGroup to listOf("XAU/USD", "XAG/USD", "XPT/USD", "XPD/USD"),
            cryptoChipGroup to listOf("BTC/USD", "ETH/USD", "LTC/USD", "XRP/USD", "ADA/USD", "DOT/USD"),
            cashCFDChipGroup to listOf(
                "US30.cash", "SPX500.cash", "NAS100.cash",
                "GER30.cash", "FRA40.cash", "UK100.cash", "ESP35.cash",
                "JPN225.cash", "AUS200.cash"
            ),
            commoditiesChipGroup to listOf("SOYBEAN", "WHEAT", "CORN", "COFFEE", "COCOA", "USOIL", "NATGAS"),
            equitiesChipGroup to listOf("AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "META", "NFLX", "NVDA")
        )

        // Configurar chips predefinidos para cada grupo
        for ((chipGroup, symbols) in predefinedSymbolsMap) {
            for (symbol in symbols) {
                val chip = Chip(this).apply {
                    text = symbol
                    isCloseIconVisible = false // Permitimos que se elimine manualmente
                    setOnClickListener {
                        // Si "Todos los Activos" está seleccionado, no se pueden añadir otros símbolos
                        if (isChipSelected(symbolsChipGroup, "Todos los Activos")) {
                            Toast.makeText(this@RegisterStrategyActivity, "Ya has seleccionado Todos los Activos", Toast.LENGTH_SHORT).show()
                        } else {
                            addSymbolToChipGroup(symbol)
                        }
                    }
                    setOnCloseIconClickListener {
                        symbolsChipGroup.removeView(this) // Eliminar manualmente el chip
                    }
                }
                chipGroup.addView(chip)
            }
        }
    }


    private fun clearAllSymbols() {
        // Eliminar todos los chips del grupo de símbolos
        symbolsChipGroup.removeAllViews()

        // Desmarcar los chips de otros grupos
        val allGroups = listOf(
            forexChipGroup, exoticsChipGroup, metalsChipGroup, cryptoChipGroup,
            cashCFDChipGroup, commoditiesChipGroup, equitiesChipGroup
        )

        for (group in allGroups) {
            group.children.filterIsInstance<Chip>().forEach { chip ->
                chip.isChecked = false
            }
        }

        // Eliminar los símbolos personalizados
        customSymbolsChipGroup.removeAllViews()
    }


    // Método general para verificar si un chip específico está seleccionado en cualquier ChipGroup
    private fun isChipSelected(chipGroup: ChipGroup, text: String): Boolean {
        return chipGroup.children
            .filterIsInstance<Chip>()
            .any { it.text.toString() == text }
    }



    private fun setupButtons() {
        // Botón para añadir indicadores al ChipGroup
        addIndicatorButton.setOnClickListener {
            val indicatorText = addIndicatorEditText.text.toString().trim()

            if (indicatorText.isBlank()) {
                Toast.makeText(this, "Introduce un indicador válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Usa la función centralizada para añadir el indicador
            addIndicatorToChipGroup(indicatorText)

            // Limpia el campo de texto solo si la operación fue exitosa
            addIndicatorEditText.text.clear()
        }


        // Botón de cancelar
        cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED) // Indica que no se hizo ningún cambio
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }


        // Botón de guardar
        saveButton.setOnClickListener {
            saveStrategyToFirestore()
        }

        // Botón para añadir símbolos al ChipGroup personalizado
        addSymbolButton.setOnClickListener {
            val symbolText = addSymbolEditText.text.toString().trim()

            if (symbolText.isBlank()) {
                Toast.makeText(this, "Introduce un símbolo válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Usa la función centralizada para añadir el símbolo
            addSymbolToChipGroup(symbolText)

            // Limpia el campo de texto
            addSymbolEditText.text.clear()
        }
    }

    private fun addSymbolToChipGroup(symbol: String) {
        // Verificar si el símbolo ya está seleccionado
        if (isChipSelected(symbolsChipGroup, symbol)) {
            Toast.makeText(this, "El símbolo ya está seleccionado", Toast.LENGTH_SHORT).show()
            return
        }

        // Si se selecciona "Todos los Activos", limpiar todo
        if (symbol == "Todos los Activos") {
            clearAllSymbols()
        } else if (isChipSelected(symbolsChipGroup, "Todos los Activos")) {
            // Si "Todos los Activos" está seleccionado, no permitir otros símbolos
            Toast.makeText(this, "No puedes seleccionar otros símbolos con 'Todos los Activos' activo", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear un nuevo chip dinámico para el símbolo
        val chip = Chip(this).apply {
            text = symbol
            isCloseIconVisible = true // Permitimos que se elimine manualmente
            setOnCloseIconClickListener {
                symbolsChipGroup.removeView(this) // Eliminar manualmente el chip
            }
        }

        symbolsChipGroup.addView(chip)
    }


    private fun addIndicatorToChipGroup(indicator: String) {
        // Verificar si el indicador ya existe
        if (isChipSelected(chipGroup, indicator)) {
            Toast.makeText(this, "El indicador ya existe", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear un nuevo chip dinámico
        val chip = Chip(this).apply {
            text = indicator
            isCloseIconVisible = true
            setOnCloseIconClickListener { chipGroup.removeView(this) }
        }

        chipGroup.addView(chip)
    }


    private fun saveStrategyToFirestore() {
        val title = titleEditText.text.toString().trim()
        val entryCondition = entryConditionEditText.text.toString().trim()
        val exitCondition = exitConditionEditText.text.toString().trim()
        val generalConsiderations = generalConsiderationsEditText.text.toString().trim()

        // Concatenar la descripción completa
        val description = """
        Condición de entrada:
        $entryCondition

        Condición de salida:
        $exitCondition

        Consideraciones generales:
        $generalConsiderations
    """.trimIndent()

        val tradingStyles = mutableListOf<String>()
        val strategyId = intent.getStringExtra("strategyId")
        val indicators = chipGroup.children
            .filterIsInstance<Chip>()
            .map { it.text.toString() }
            .distinct()
            .toList()

        if (dayTradingCheckBox.isChecked) tradingStyles.add("Day Trading")
        if (scalpingCheckBox.isChecked) tradingStyles.add("Scalping")
        if (swingTradingCheckBox.isChecked) tradingStyles.add("Swing Trading")

        // Validaciones
        if (title.isBlank()) {
            Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isBlank()) {
            Toast.makeText(this, "La descripción es obligatoria", Toast.LENGTH_SHORT).show()
            return
        }

        if (entryCondition.isBlank()) {
            Toast.makeText(this, "La condición de entrada es obligatoria", Toast.LENGTH_SHORT).show()
            return
        }

        if (exitCondition.isBlank()) {
            Toast.makeText(this, "La condición de salida es obligatoria", Toast.LENGTH_SHORT).show()
            return
        }


        if (tradingStyles.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un estilo de trading", Toast.LENGTH_SHORT).show()
            return
        }

        if (indicators.isEmpty()) {
            Toast.makeText(this, "Añade al menos un indicador", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar temporalidades
        val timeFrames = timeFramesCheckBoxes.filter { it.isChecked }.map { it.text.toString() }
        if (timeFrames.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos una temporalidad", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedSymbols = mutableListOf<String>()

        // Recoger todos los chips seleccionados en los grupos predefinidos y personalizados
        val allGroups = listOf(
            symbolsChipGroup, // Chips seleccionados de cualquier grupo
            customSymbolsChipGroup
        )

        for (group in allGroups) {
            group.children.filterIsInstance<Chip>().forEach { chip ->
                selectedSymbols.add(chip.text.toString())
            }
        }

        // Validar si hay símbolos seleccionados
        if (selectedSymbols.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un símbolo", Toast.LENGTH_SHORT).show()
            return
        }


        val algorithmCode = algorithmEditText.text.toString().trim()

        if (strategyId != null) {
            // Actualizar estrategia existente
            db.collection("strategies").document(strategyId).update(
                mapOf(
                    "title" to title,
                    "description" to description,
                    "tradingStyles" to tradingStyles,
                    "indicators" to indicators,
                    "timeframes" to timeFrames,
                    "symbols" to selectedSymbols,
                    "algorithmCode" to algorithmCode,
                    "timestamp" to System.currentTimeMillis(),
                    "entryConditionImageUrl" to (entryImageUrl ?: ""),
                    "exitConditionImageUrl" to (exitImageUrl ?: "")
                )
            ).addOnSuccessListener {
                Toast.makeText(this, "Estrategia actualizada", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Error al actualizar estrategia", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Crear nueva estrategia
            val userId = currentUser?.uid ?: return
            db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
                val alias = userDoc.getString("alias") ?: "Anónimo"
                val avatarName = userDoc.getString("avatarName") ?: "default_avatar"
                val avatarUrl = userDoc.getString("avatarUrl") ?: ""

                val strategy = hashMapOf<String, Any>(
                    "title" to title,
                    "createdBy" to userId,
                    "authorAlias" to alias,
                    "avatarName" to avatarName,
                    "avatarUrl" to avatarUrl,
                    "description" to description,
                    "tradingStyles" to tradingStyles,
                    "indicators" to indicators,
                    "timeframes" to timeFrames,
                    "symbols" to selectedSymbols,
                    "algorithmCode" to algorithmCode,
                    "favoritedBy" to emptyList<String>(),
                    "userRatings" to emptyMap<String, Double>(),
                    "rating" to 0.0,
                    "totalVotes" to 0,
                    "timestamp" to System.currentTimeMillis(),
                    "entryConditionImageUrl" to (entryImageUrl ?: ""),
                    "exitConditionImageUrl" to (exitImageUrl ?: ""),
                    "movements" to emptyList<String>()
                )

                db.collection("strategies").add(strategy).addOnSuccessListener { documentRef ->
                    Toast.makeText(this, "Estrategia guardada exitosamente", Toast.LENGTH_SHORT).show()

                    val resultIntent = Intent()
                    resultIntent.putExtra("newStrategyId", documentRef.id)
                    setResult(RESULT_OK, resultIntent)

                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al guardar estrategia", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun loadStrategyData(strategyId: String) {
        db.collection("strategies").document(strategyId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Prellenar el campo de título
                    titleEditText.setText(document.getString("title"))

                    // Cargar la imagen de entrada
                    entryImageUrl = document.getString("entryConditionImageUrl")
                    if (!entryImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(entryImageUrl)
                            .placeholder(R.drawable.ic_placeholder) // Mientras carga
                            .error(R.drawable.ic_placeholder)       // Si falla
                            .into(entryImageView)
                    } else {
                        entryImageView.setImageResource(R.drawable.ic_placeholder) // Imagen por defecto
                    }

                    // Cargar la imagen de salida
                    exitImageUrl = document.getString("exitConditionImageUrl")
                    if (!exitImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(exitImageUrl)
                            .placeholder(R.drawable.ic_placeholder) // Mientras carga
                            .error(R.drawable.ic_placeholder)       // Si falla
                            .into(exitImageView)
                    } else {
                        exitImageView.setImageResource(R.drawable.ic_placeholder) // Imagen por defecto
                    }

                    // Analizar y dividir el campo de descripción
                    val description = document.getString("description") ?: ""
                    val entryCondition = extractSection(description, "Condición de entrada:")
                    val exitCondition = extractSection(description, "Condición de salida:")
                    val generalConsiderations = extractSection(description, "Consideraciones generales:")

                    // Prellenar los campos de descripción dividida
                    entryConditionEditText.setText(entryCondition)
                    exitConditionEditText.setText(exitCondition)
                    generalConsiderationsEditText.setText(generalConsiderations)

                    // Prellenar estilos de trading
                    val tradingStyles = document.get("tradingStyles") as? List<*>
                    dayTradingCheckBox.isChecked =
                        tradingStyles?.contains("Day Trading") == true
                    scalpingCheckBox.isChecked = tradingStyles?.contains("Scalping") == true
                    swingTradingCheckBox.isChecked =
                        tradingStyles?.contains("Swing Trading") == true

                    /// Prellenar indicadores
                    val indicators = document.get("indicators") as? List<*>
                    indicators?.forEach { indicator ->
                        val indicatorText = indicator.toString()
                        val existingIndicators = chipGroup.children
                            .filterIsInstance<Chip>()
                            .map { it.text.toString() }

                        if (!existingIndicators.contains(indicatorText)) {
                            val chip = Chip(this).apply {
                                text = indicatorText
                                isCloseIconVisible = true
                                setOnCloseIconClickListener { chipGroup.removeView(this) }
                            }
                            chipGroup.addView(chip)
                        }
                    }

                    // Prellenar temporalidades
                    val timeFrames = document.get("timeframes") as? List<*>
                    timeFramesCheckBoxes.forEach { checkBox ->
                        checkBox.isChecked =
                            timeFrames?.contains(checkBox.text.toString()) == true
                    }


                    // Prellenar símbolos seleccionados
                    symbolsChipGroup.removeAllViews() // Limpiar el grupo de chips antes de agregar nuevos

                    val symbols = document.get("symbols") as? List<*>
                    symbols?.forEach { symbol ->
                        val chip = Chip(this).apply {
                            text = symbol.toString()
                            isCloseIconVisible = true
                            setOnCloseIconClickListener {
                                symbolsChipGroup.removeView(this)
                            }
                        }
                        symbolsChipGroup.addView(chip)
                    }



                    // Prellenar código de algoritmo
                    algorithmEditText.setText(document.getString("algorithmCode"))
                }
            }
    }
    // Método para extraer una sección específica del texto
    private fun extractSection(description: String, sectionHeader: String): String {
        val sectionStart = description.indexOf(sectionHeader)
        if (sectionStart == -1) return "" // Si no se encuentra la sección, devolver vacío

        // Encontrar el final de la sección actual y el inicio de la siguiente
        val sectionEnd = description.indexOf("\n\n", sectionStart + sectionHeader.length)
        return if (sectionEnd == -1) {
            // Si no hay otra sección después, devolver hasta el final del texto
            description.substring(sectionStart + sectionHeader.length).trim()
        } else {
            // Devolver solo el contenido de esta sección
            description.substring(sectionStart + sectionHeader.length, sectionEnd).trim()
        }
    }

    //Crea el Diálogo para Gestionar Imágenes
    private fun showImageManagementDialog() {
        val options = arrayOf("Imagen de Entrada", "Imagen de Salida")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccionar imagen a gestionar")
        builder.setItems(options) { _, which ->
            selectedImageType = if (which == 0) "entry" else "exit"
            openImagePicker()
        }
        builder.show()
    }

    // Permite al usuario seleccionar una imagen de la galería
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
    }

    companion object {
        private const val REQUEST_CODE_IMAGE_PICKER = 1001
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                // Subir la imagen seleccionada a Firebase Storage
                uploadImageToFirebase(imageUri) { uploadedUrl ->
                    if (uploadedUrl != null) {
                        if (selectedImageType == "entry") {
                            entryImageUrl = uploadedUrl
                            entryImageView.setImageURI(imageUri) // Mostrar la imagen seleccionada
                            Toast.makeText(
                                this,
                                "Imagen de entrada subida exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (selectedImageType == "exit") {
                            exitImageUrl = uploadedUrl
                            exitImageView.setImageURI(imageUri) // Mostrar la imagen seleccionada
                            Toast.makeText(
                                this,
                                "Imagen de salida subida exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para subir la imagen a Firebase Storage
    private fun uploadImageToFirebase(imageUri: Uri, callback: (String?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("images/${System.currentTimeMillis()}.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString()) // Devuelve la URL pública de la imagen
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al obtener la URL de la imagen", Toast.LENGTH_SHORT).show()
                    callback(null) // Devuelve null si falla al obtener la URL
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                callback(null) // Devuelve null si falla la subida
            }
    }
}
