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

    private fun getUserIdFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("MindTradePrefs", MODE_PRIVATE)
        return sharedPreferences.getString("USER_ID", null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmotionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
        soundId = soundPool.load(this, R.raw.select_sound, 1)

        userId = getUserIdFromPreferences()
        if (userId == null) {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.cardPositive.setOnClickListener {
            selectCard(binding.cardPositive, "Psico +")
        }
        binding.cardNegative.setOnClickListener {
            selectCard(binding.cardNegative, "Psico -")
        }
    }

    private fun selectCard(cardView: CardView, psicoState: String) {
        selectedCard?.setCardBackgroundColor(ContextCompat.getColor(this, R.color.black))
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.highlight_green))
        selectedCard = cardView
        playSelectionSound()
        saveEmotionalState(psicoState)
    }

    private fun playSelectionSound() {
        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    }

    private fun saveEmotionalState(psicoState: String) {
        if (userId != null) {
            val userData = mapOf(
                "psico" to psicoState,
                "registration_progress" to "emotions"
            )
            db.collection("users").document(userId!!).set(userData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Estado emocional guardado", Toast.LENGTH_SHORT).show()
                    val nextActivity = if (psicoState == "Psico +") {
                        PsicoPositiveActivity::class.java
                    } else {
                        PsicoNegativeActivity::class.java
                    }
                    val intent = Intent(this, nextActivity)
                    startActivity(intent)
                    // Eliminamos `finish()` para permitir retroceder a esta actividad
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar el estado emocional: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_SHORT).show()
        }
    }
}
