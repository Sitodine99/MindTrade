package strategycards

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.children
import com.example.mindtrade.R
import com.example.mindtrade.finishWithFade
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class RegisterStrategyActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
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

    private lateinit var strategyImageView01: ImageView
    private lateinit var strategyImageView02: ImageView


    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private var entryImageUrl: String? = null
    private var exitImageUrl: String? = null
    private var selectedImageType: String = "" // "entry" o "exit"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_strategy)
        strategyImageView01 = findViewById(R.id.strategyImageView)
        strategyImageView02 = findViewById(R.id.strategyImageView02)

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
            "EMA21",
            "EMA50",
            "EMA200",
            "SMA21",
            "SMA50",
            "SMA200",
            "MACD",
            "RSI",
            "Bollinger Bands",
            "ADX",
            "Ichimoku",
            "Volume",
            "Fibonacci Retracements",
            "Pivot Points",
        )

        for (indicator in predefinedIndicators) {
            val chip = Chip(this).apply {
                text = indicator
                isCheckable = true
                setOnClickListener {
                    if (!isChipDuplicate(indicator)) {
                        val selectedChip = Chip(this@RegisterStrategyActivity).apply {
                            text = indicator
                            isCloseIconVisible = true
                            setOnCloseIconClickListener { chipGroup.removeView(this) }
                        }
                        chipGroup.addView(selectedChip)
                    } else {
                        Toast.makeText(
                            this@RegisterStrategyActivity,
                            "El indicador ya existe",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            predefinedIndicatorsChipGroup.addView(chip)
        }
    }

    private fun setupPredefinedSymbols() {

        // Forex
        val allInstrumentsSymbols = listOf("Todos los activos")
        for (symbol in allInstrumentsSymbols) {
            val chip = Chip(this).apply {
                text = symbol
                isCheckable = true // Permite que el chip sea seleccionable
                setOnClickListener {
                    // Crear y añadir directamente al grupo personalizado sin comprobar duplicados
                    val selectedChip = Chip(this@RegisterStrategyActivity).apply {
                        text = symbol
                        isCloseIconVisible = true // Permitir eliminar el chip
                        setOnCloseIconClickListener {
                            customSymbolsChipGroup.removeView(this)
                        }
                    }
                    customSymbolsChipGroup.addView(selectedChip)
                }
            }
            allInstrumentsChipGroup.addView(chip)
        }
        // Forex
        val forexSymbols = listOf("EUR/USD", "USD/JPY", "GBP/USD", "USD/CHF", "AUD/USD", "USD/CAD", "NZD/USD")
        for (symbol in forexSymbols) {
            val chip = Chip(this).apply {
                text = symbol
                isCheckable = true // Permite que el chip sea seleccionable
                setOnClickListener {
                    // Crear y añadir directamente al grupo personalizado sin comprobar duplicados
                    val selectedChip = Chip(this@RegisterStrategyActivity).apply {
                        text = symbol
                        isCloseIconVisible = true // Permitir eliminar el chip
                        setOnCloseIconClickListener {
                            customSymbolsChipGroup.removeView(this)
                        }
                    }
                    customSymbolsChipGroup.addView(selectedChip)
                }
            }
            forexChipGroup.addView(chip)
        }

        // Exotics
        val exoticsSymbols = listOf("USD/SEK", "USD/NOK", "USD/ZAR", "EUR/TRY")
        for (symbol in exoticsSymbols) {
            val chip = Chip(this).apply {
                text = symbol
                isCheckable = true
                setOnClickListener {
                    val selectedChip = Chip(this@RegisterStrategyActivity).apply {
                        text = symbol
                        isCloseIconVisible = true
                        setOnCloseIconClickListener {
                            customSymbolsChipGroup.removeView(this)
                        }
                    }
                    customSymbolsChipGroup.addView(selectedChip)
                }
            }
            exoticsChipGroup.addView(chip)
        }

        // Metals CFD
        val metalsSymbols = listOf("XAU/USD", "XAG/USD", "XPT/USD", "XPD/USD")
        for (symbol in metalsSymbols) {
            val chip = Chip(this).apply {
                text = symbol
                isCheckable = true
                setOnClickListener {
                    val selectedChip = Chip(this@RegisterStrategyActivity).apply {
                        text = symbol
                        isCloseIconVisible = true
                        setOnCloseIconClickListener {
                            customSymbolsChipGroup.removeView(this)
                        }
                    }
                    customSymbolsChipGroup.addView(selectedChip)
                }
            }
            metalsChipGroup.addView(chip)
        }

        // Crypto CFD
        val cryptoSymbols = listOf("BTC/USD", "ETH/USD", "LTC/USD", "XRP/USD", "ADA/USD", "DOT/USD")
        for (symbol in cryptoSymbols) {
            val chip = Chip(this).apply {
                text = symbol
                isCheckable = true
                setOnClickListener {
                    val selectedChip = Chip(this@RegisterStrategyActivity).apply {
                        text = symbol
                        isCloseIconVisible = true
                        setOnCloseIconClickListener {
                            customSymbolsChipGroup.removeView(this)
                        }
                    }
                    customSymbolsChipGroup.addView(selectedChip)
                }
            }
            cryptoChipGroup.addView(chip)
        }

        // Cash CFD
        val cashCFDSymbols = listOf(
            "US30.cash", "SPX500.cash", "NAS100.cash",
            "GER30.cash", "FRA40.cash", "UK100.cash", "ESP35.cash",
            "JPN225.cash", "AUS200.cash"
        )
        for (symbol in cashCFDSymbols) {
            val chip = Chip(this).apply {
                text = symbol
                isCheckable = true
                setOnClickListener {
                    val selectedChip = Chip(this@RegisterStrategyActivity).apply {
                        text = symbol
                        isCloseIconVisible = true
                        setOnCloseIconClickListener {
                            customSymbolsChipGroup.removeView(this)
                        }
                    }
                    customSymbolsChipGroup.addView(selectedChip)
                }
            }
            cashCFDChipGroup.addView(chip)
        }

        // Commodities
        val commoditiesSymbols = listOf("SOYBEAN", "WHEAT", "CORN", "COFFEE", "COCOA", "USOIL", "NATGAS")
        for (symbol in commoditiesSymbols) {
            val chip = Chip(this).apply {
                text = symbol
                isCheckable = true
                setOnClickListener {
                    val selectedChip = Chip(this@RegisterStrategyActivity).apply {
                        text = symbol
                        isCloseIconVisible = true
                        setOnCloseIconClickListener {
                            customSymbolsChipGroup.removeView(this)
                        }
                    }
                    customSymbolsChipGroup.addView(selectedChip)
                }
            }
            commoditiesChipGroup.addView(chip)
        }

        // Equities CFD
        val equitiesSymbols = listOf("AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "META", "NFLX", "NVDA")
        for (symbol in equitiesSymbols) {
            val chip = Chip(this).apply {
                text = symbol
                isCheckable = true
                setOnClickListener {
                    val selectedChip = Chip(this@RegisterStrategyActivity).apply {
                        text = symbol
                        isCloseIconVisible = true
                        setOnCloseIconClickListener {
                            customSymbolsChipGroup.removeView(this)
                        }
                    }
                    customSymbolsChipGroup.addView(selectedChip)
                }
            }
            equitiesChipGroup.addView(chip)
        }
    }



    private fun setupButtons() {
        // Botón para añadir indicadores al ChipGroup
        addIndicatorButton.setOnClickListener {
            val indicatorText = addIndicatorEditText.text.toString().trim()

            if (indicatorText.isBlank()) {
                Toast.makeText(this, "Introduce un indicador válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Evitar duplicados
            if (isChipDuplicate(indicatorText)) {
                Toast.makeText(this, "El indicador ya existe", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Crear un nuevo chip dinámico
            val chip = Chip(this).apply {
                text = indicatorText
                isCloseIconVisible = true
                setOnCloseIconClickListener { chipGroup.removeView(this) }
            }
            chipGroup.addView(chip)
            addIndicatorEditText.text.clear() // Limpiar el campo de texto
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

        addSymbolButton.setOnClickListener {
            val symbolText = addSymbolEditText.text.toString().trim()

            if (symbolText.isBlank()) {
                Toast.makeText(this, "Introduce un símbolo válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Evitar duplicados
            if (isChipDuplicate(symbolText)) {
                Toast.makeText(this, "El símbolo ya existe", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Crear un nuevo chip dinámico
            val chip = Chip(this).apply {
                text = symbolText
                isCloseIconVisible = true
                setOnCloseIconClickListener { customSymbolsChipGroup.removeView(this) }
            }

            // Añadir el chip al grupo de símbolos personalizados
            customSymbolsChipGroup.addView(chip)

            // Limpiar el campo de texto
            addSymbolEditText.text.clear()
        }

    }



    private fun isChipDuplicate(text: String): Boolean {
        val allGroups = listOf(
            chipGroup, // Incluye el grupo de indicadores
            customSymbolsChipGroup,
            allInstrumentsChipGroup,
            forexChipGroup,
            exoticsChipGroup,
            metalsChipGroup,
            cryptoChipGroup,
            cashCFDChipGroup,
            commoditiesChipGroup,
            equitiesChipGroup
        )
        return allGroups.any { group ->
            group.children.filterIsInstance<Chip>().any { it.text.toString().equals(text, true) }
        }
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

        if (dayTradingCheckBox.isChecked) tradingStyles.add("Day Trading")
        if (scalpingCheckBox.isChecked) tradingStyles.add("Scalping")
        if (swingTradingCheckBox.isChecked) tradingStyles.add("Swing Trading")

        // Validar campos obligatorios
        if (title.isBlank()) {
            Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isBlank()) {
            Toast.makeText(this, "La descripción es obligatoria", Toast.LENGTH_SHORT).show()
            return
        }

        if (tradingStyles.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un estilo de trading", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Validar temporalidades
        val timeFrames = timeFramesCheckBoxes.filter { it.isChecked }.map { it.text.toString() }
        if (timeFrames.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos una temporalidad", Toast.LENGTH_SHORT)
                .show()
            return
        }


        // Recoger los indicadores seleccionados
        val indicators = mutableListOf<String>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            indicators.add(chip.text.toString())
        }

        // Recoger los símbolos seleccionados
        val selectedSymbols = mutableListOf<String>()
        allInstrumentsChipGroup.children.filterIsInstance<Chip>().filter { it.isChecked }
            .mapTo(selectedSymbols) { it.text.toString() }
        forexChipGroup.children.filterIsInstance<Chip>().filter { it.isChecked }
            .mapTo(selectedSymbols) { it.text.toString() }
        exoticsChipGroup.children.filterIsInstance<Chip>().filter { it.isChecked }
            .mapTo(selectedSymbols) { it.text.toString() }
        metalsChipGroup.children.filterIsInstance<Chip>().filter { it.isChecked }
            .mapTo(selectedSymbols) { it.text.toString() }
        cryptoChipGroup.children.filterIsInstance<Chip>().filter { it.isChecked }
            .mapTo(selectedSymbols) { it.text.toString() }
        commoditiesChipGroup.children.filterIsInstance<Chip>().filter { it.isChecked }
            .mapTo(selectedSymbols) { it.text.toString() }
        cashCFDChipGroup.children.filterIsInstance<Chip>().filter { it.isChecked }
            .mapTo(selectedSymbols) { it.text.toString() }
        equitiesChipGroup.children.filterIsInstance<Chip>().filter { it.isChecked }
            .mapTo(selectedSymbols) { it.text.toString() }


        // Al pasar los datos al fragmento de detalles
        val bundle = Bundle().apply {
            putString("strategyId", strategyId)
            putStringArray(
                "strategySymbols",
                selectedSymbols.toTypedArray()
            ) // Pasar los símbolos como StringArray
        }
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
                    "symbols" to selectedSymbols, // Guardar los símbolos
                    "algorithmCode" to algorithmCode,
                    "timestamp" to System.currentTimeMillis()
                )
            ).addOnSuccessListener {
                Toast.makeText(this, "Estrategia actualizada", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Error al actualizar estrategia", Toast.LENGTH_SHORT)
                    .show()
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
                    "timestamp" to System.currentTimeMillis()
                ).apply {
                    entryImageUrl?.let { put("entryConditionImageUrl", it) }
                    exitImageUrl?.let { put("exitConditionImageUrl", it) }
                }



                db.collection("strategies").add(strategy).addOnSuccessListener { documentRef ->
                    Toast.makeText(this, "Estrategia guardada exitosamente", Toast.LENGTH_SHORT)
                        .show()

                    val resultIntent = Intent()
                    resultIntent.putExtra("newStrategyId", documentRef.id)
                    setResult(RESULT_OK, resultIntent)

                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al guardar estrategia", Toast.LENGTH_SHORT)
                        .show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT)
                    .show()
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

                    entryImageUrl = document.getString("entryConditionImageUrl")
                    if (!entryImageUrl.isNullOrEmpty()) {
                        strategyImageView01.setImageURI(Uri.parse(entryImageUrl))
                    } else {
                        strategyImageView01.setImageResource(R.drawable.ic_placeholder) // Imagen por defecto
                    }

                    exitImageUrl = document.getString("exitConditionImageUrl")
                    if (!exitImageUrl.isNullOrEmpty()) {
                        strategyImageView02.setImageURI(Uri.parse(exitImageUrl))
                    } else {
                        strategyImageView02.setImageResource(R.drawable.ic_placeholder) // Imagen por defecto
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

                    // Prellenar indicadores
                    val indicators = document.get("indicators") as? List<*>
                    indicators?.forEach { indicator ->
                        val chip = Chip(this).apply {
                            text = indicator.toString()
                            isCloseIconVisible = true
                            setOnCloseIconClickListener { chipGroup.removeView(this) }
                        }
                        chipGroup.addView(chip)
                    }

                    // Prellenar temporalidades
                    val timeFrames = document.get("timeframes") as? List<*>
                    timeFramesCheckBoxes.forEach { checkBox ->
                        checkBox.isChecked =
                            timeFrames?.contains(checkBox.text.toString()) == true
                    }

                    // Prellenar símbolos seleccionados
                    val symbols = document.get("symbols") as? List<*>
                    symbols?.forEach { symbol ->
                        // Busca el chip en cada grupo de símbolos y márcalo como seleccionado si coincide
                        val chipGroups = listOf(
                            allInstrumentsChipGroup,
                            forexChipGroup,
                            exoticsChipGroup,
                            metalsChipGroup,
                            cryptoChipGroup,
                            commoditiesChipGroup,
                            cashCFDChipGroup,
                            equitiesChipGroup
                        )
                        chipGroups.forEach { group ->
                            group.children.filterIsInstance<Chip>().forEach { chip ->
                                if (chip.text.toString() == symbol.toString()) {
                                    chip.isChecked = true
                                }
                            }
                        }
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
                            strategyImageView01.setImageURI(imageUri) // Mostrar la imagen seleccionada
                            Toast.makeText(
                                this,
                                "Imagen de entrada subida exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (selectedImageType == "exit") {
                            exitImageUrl = uploadedUrl
                            strategyImageView02.setImageURI(imageUri) // Mostrar la imagen seleccionada
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


