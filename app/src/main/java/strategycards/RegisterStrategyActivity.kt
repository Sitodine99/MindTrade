package strategycards

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mindtrade.R
import com.example.mindtrade.finishWithFade
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterStrategyActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
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

    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_strategy)

        // Referenciar elementos del diseño
        titleEditText = findViewById(R.id.strategyTitle)
        descriptionEditText = findViewById(R.id.strategyDescription)
        dayTradingCheckBox = findViewById(R.id.tradingStyleDayTrading)
        scalpingCheckBox = findViewById(R.id.tradingStyleScalping)
        swingTradingCheckBox = findViewById(R.id.tradingStyleSwingTrading)
        chipGroup = findViewById(R.id.indicatorChipGroup)
        predefinedIndicatorsChipGroup = findViewById(R.id.predefinedIndicatorChipGroup) // Nuevo grupo para chips predefinidos
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
            setResult(RESULT_CANCELED) // Indica que no se hizo ningún cambio
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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
        val tradingStyles = mutableListOf<String>()
        if (dayTradingCheckBox.isChecked) tradingStyles.add("Day Trading")
        if (scalpingCheckBox.isChecked) tradingStyles.add("Scalping")
        if (swingTradingCheckBox.isChecked) tradingStyles.add("Swing Trading")

        if (title.isBlank() || description.isBlank() || tradingStyles.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val indicators = mutableListOf<String>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            indicators.add(chip.text.toString())
        }

        val timeFrames = timeFramesCheckBoxes.filter { it.isChecked }.map { it.text.toString() }
        val algorithmCode = algorithmEditText.text.toString().trim()

        val userId = currentUser?.uid ?: return
        db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
            val alias = userDoc.getString("alias") ?: "Anónimo"
            val avatarName = userDoc.getString("avatarName") ?: "default_avatar"

            val strategy = hashMapOf(
                "title" to title,
                "description" to description,
                "tradingStyles" to tradingStyles,
                "indicators" to indicators,
                "timeframes" to timeFrames,
                "algorithmCode" to algorithmCode,
                "createdBy" to userId,
                "authorAlias" to alias,
                "avatarName" to avatarName,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("strategies").add(strategy).addOnSuccessListener { documentRef ->
                Toast.makeText(this, "Estrategia guardada exitosamente", Toast.LENGTH_SHORT).show()

                // Devolver el ID de la estrategia al MainActivity
                val resultIntent = Intent()
                resultIntent.putExtra("newStrategyId", documentRef.id)
                setResult(RESULT_OK, resultIntent)

                // Finalizar la actividad
                finish()
            }.addOnFailureListener {
           Toast.makeText(this, "Error al guardar estrategia", Toast.LENGTH_SHORT).show()
}
}.addOnFailureListener {
    Toast.makeText(this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
}


}


    override fun onBackPressed() {
    setResult(RESULT_CANCELED)
    finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

}

