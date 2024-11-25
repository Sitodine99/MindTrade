package com.example.mindtrade

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterStrategyActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var chipGroup: ChipGroup
    private lateinit var addIndicatorEditText: EditText
    private lateinit var addIndicatorButton: Button
    private lateinit var predefinedIndicatorsChipGroup: ChipGroup
    private lateinit var timeFramesCheckBoxes: List<CheckBox>
    private lateinit var algorithmEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_strategy)

        // Referenciar elementos del diseño
        titleEditText = findViewById(R.id.strategyTitle)
        descriptionEditText = findViewById(R.id.strategyDescription)
        categorySpinner = findViewById(R.id.strategyCategory)
        chipGroup = findViewById(R.id.indicatorChipGroup)
        predefinedIndicatorsChipGroup =
            findViewById(R.id.predefinedIndicatorChipGroup) // Nuevo grupo para chips predefinidos
        addIndicatorEditText = findViewById(R.id.addIndicatorEditText)
        addIndicatorButton = findViewById(R.id.addIndicatorButton)
        algorithmEditText = findViewById(R.id.tradingAlgorithmCode)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)

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
            findViewById(R.id.timeMN)
        )

        // Inicializar etiquetas predefinidas
        setupPredefinedIndicators()

        // Configurar botones
        setupButtons()
    }

    private fun setupPredefinedIndicators() {
        val predefinedIndicators = listOf(
            "EMA", "SMA", "MACD", "RSI", "Bollinger Bands",
            "ADX", "Ichimoku", "Volume", "Fibonacci Retracements", "Pivot Points"
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
            finish() // Cierra la actividad
        }

        // Botón de guardar
        saveButton.setOnClickListener {
            saveStrategyToFirestore()
        }
    }

    // Validar si un chip con el mismo texto ya existe
    private fun isChipDuplicate(indicatorText: String): Boolean {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.text.toString().equals(indicatorText, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun saveStrategyToFirestore() {
        val title = titleEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val category = categorySpinner.selectedItem.toString()

        if (title.isBlank() || description.isBlank() || category.isBlank()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val indicators = mutableListOf<String>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            indicators.add(chip.text.toString())
        }

        val timeFrames = timeFramesCheckBoxes.filter { it.isChecked }.map { it.text.toString() }
        val algorithmCode = algorithmEditText.text.toString().trim()

        // Consulta para obtener alias y avatar
        val userId = currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtén los datos del usuario actual
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    val alias = userDoc.getString("alias") ?: "Anónimo"
                    val avatarName = userDoc.getString("avatarName") ?: "default_avatar"

                    // Estructura del documento de estrategia
                    val strategy = hashMapOf(
                        "title" to title,
                        "description" to description,
                        "tradingStyle" to category,
                        "indicators" to indicators,
                        "timeframes" to timeFrames,
                        "algorithmCode" to algorithmCode,
                        "createdBy" to userId, // ID del creador
                        "authorAlias" to alias, // Alias del creador
                        "avatarName" to avatarName, // Avatar del creador
                        "timestamp" to System.currentTimeMillis()
                    )

                    // Guarda la estrategia en Firestore
                    db.collection("strategies")
                        .add(strategy)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Estrategia guardada exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Error al guardar: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al cargar datos del usuario: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}

