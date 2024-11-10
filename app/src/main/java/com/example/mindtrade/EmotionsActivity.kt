package com.example.mindtrade

import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mindtrade.databinding.ActivityEmotionsBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView

class EmotionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmotionsBinding
    private var userId: String? = null
    private val db = FirebaseFirestore.getInstance()

    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0
    private var selectedCard: CardView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Usar ViewBinding
        binding = ActivityEmotionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar SoundPool
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        // Cargar el sonido
        soundId = soundPool.load(this, R.raw.select_sound, 1)

        // Obtener el ID del usuario de la intent
        userId = intent.getStringExtra("USER_ID")

        if (userId == null) {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Configurar la selección de estado emocional usando ViewBinding
        binding.cardPositive.setOnClickListener {
            selectCard(binding.cardPositive, "Psico +")
        }

        binding.cardNegative.setOnClickListener {
            selectCard(binding.cardNegative, "Psico -")
        }
    }

    private fun selectCard(cardView: CardView, psicoState: String) {
        // Cambiar el fondo de la tarjeta seleccionada
        selectedCard?.setCardBackgroundColor(ContextCompat.getColor(this, R.color.black))
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.highlight_green))

        selectedCard = cardView

        // Reproducir sonido de selección
        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)

        // Guardar el estado emocional y redirigir
        saveEmotionalState(psicoState)
    }

    private fun saveEmotionalState(psicoState: String) {
        if (userId != null) {
            val userData = mapOf("psico" to psicoState)

            // Guardar el estado emocional en Firebase
            db.collection("users").document(userId!!).set(userData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Estado emocional guardado", Toast.LENGTH_SHORT).show()

                    // Redirigir a PsicoPositiveActivity o PsicoNegativeActivity según el estado
                    val nextActivity = if (psicoState == "Psico +") {
                        PsicoPositiveActivity::class.java
                    } else {
                        PsicoNegativeActivity::class.java
                    }
                    val intent = Intent(this, nextActivity)
                    intent.putExtra("USER_ID", userId) // Añadir el userId aquí
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar el estado emocional: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_SHORT).show()
        }
    }
}
