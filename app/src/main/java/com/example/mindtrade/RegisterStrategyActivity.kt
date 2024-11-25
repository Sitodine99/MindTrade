package com.example.mindtrade

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class RegisterStrategyActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var chipGroup: ChipGroup
    private lateinit var addIndicatorEditText: EditText
    private lateinit var addIndicatorButton: Button
    private lateinit var timeFramesCheckBoxes: List<CheckBox>
    private lateinit var algorithmEditText: EditText
    private lateinit var uploadImageButton: ImageButton
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private var selectedImageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_strategy)

        // Referenciar elementos del diseño
        titleEditText = findViewById(R.id.strategyTitle)
        descriptionEditText = findViewById(R.id.strategyDescription)
        categorySpinner = findViewById(R.id.strategyCategory)
        chipGroup = findViewById(R.id.indicatorChipGroup)
        addIndicatorEditText = findViewById(R.id.addIndicatorEditText)
        addIndicatorButton = findViewById(R.id.addIndicatorButton)
        algorithmEditText = findViewById(R.id.tradingAlgorithmCode)
        uploadImageButton = findViewById(R.id.uploadImageButton)
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

        // Configurar botones
        setupButtons()
    }

    private fun setupButtons() {
        // Botón para añadir indicadores al ChipGroup
        addIndicatorButton.setOnClickListener {
            val indicatorText = addIndicatorEditText.text.toString()
            if (indicatorText.isNotBlank()) {
                val chip = com.google.android.material.chip.Chip(this).apply {
                    text = indicatorText
                    isCloseIconVisible = true
                    setOnCloseIconClickListener { chipGroup.removeView(this) }
                }
                chipGroup.addView(chip)
                addIndicatorEditText.text.clear()
            } else {
                Toast.makeText(this, "Introduce un indicador válido", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón para seleccionar una imagen
        uploadImageButton.setOnClickListener {
            pickImageFromGallery()
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

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveStrategyToFirestore() {
        // Validar campos obligatorios
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val category = categorySpinner.selectedItem.toString()

        if (title.isBlank() || description.isBlank() || category.isBlank()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Capturar indicadores seleccionados
        val indicators = mutableListOf<String>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as com.google.android.material.chip.Chip
            indicators.add(chip.text.toString())
        }

        // Capturar temporalidades seleccionadas
        val timeFrames = timeFramesCheckBoxes.filter { it.isChecked }.map { it.text.toString() }

        // Capturar el código del algoritmo
        val algorithmCode = algorithmEditText.text.toString()

        // Subir imagen si se seleccionó
        if (selectedImageUri != null) {
            val imageRef = storage.reference.child("strategy_images/${UUID.randomUUID()}")
            val uploadTask = imageRef.putFile(selectedImageUri!!)
            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveStrategyWithImage(title, description, category, indicators, timeFrames, algorithmCode, uri.toString())
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            saveStrategyWithImage(title, description, category, indicators, timeFrames, algorithmCode, null)
        }
    }

    private fun saveStrategyWithImage(
        title: String,
        description: String,
        category: String,
        indicators: List<String>,
        timeFrames: List<String>,
        algorithmCode: String,
        imageUrl: String?
    ) {
        // Crear el objeto para guardar en Firebase
        val strategy = hashMapOf(
            "title" to title,
            "description" to description,
            "tradingStyle" to category,
            "indicators" to indicators,
            "timeframes" to timeFrames,
            "algorithmCode" to algorithmCode,
            "imageUrl" to imageUrl, // URL de la imagen o null si no hay imagen
            "createdBy" to currentUser?.uid,
            "timestamp" to System.currentTimeMillis()
        )

        // Guardar en Firestore
        db.collection("strategies")
            .add(strategy)
            .addOnSuccessListener {
                Toast.makeText(this, "Estrategia guardada exitosamente", Toast.LENGTH_SHORT).show()
                finish() // Cierra la actividad después de guardar
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
