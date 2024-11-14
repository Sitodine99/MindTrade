package com.example.mindtrade

import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.mindtrade.databinding.ActivityPsicoNegativeBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class PsicoNegativeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPsicoNegativeBinding
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
        binding = ActivityPsicoNegativeBinding.inflate(layoutInflater)
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

        binding.ansiedad.setOnClickListener { selectEmotion(binding.ansiedad, "Ansiedad") }
        binding.impaciencia.setOnClickListener { selectEmotion(binding.impaciencia, "Impaciencia") }
        binding.descontrol.setOnClickListener { selectEmotion(binding.descontrol, "Descontrol") }
        binding.avaricia.setOnClickListener { selectEmotion(binding.avaricia, "Avaricia") }
        binding.insatisfaccion.setOnClickListener { selectEmotion(binding.insatisfaccion, "Insatisfacción") }
        binding.rabia.setOnClickListener { selectEmotion(binding.rabia, "Rabia") }
        binding.verguenza.setOnClickListener { selectEmotion(binding.verguenza, "Vergüenza") }
        binding.confusion.setOnClickListener { selectEmotion(binding.confusion, "Confusión") }
        binding.miedo.setOnClickListener { selectEmotion(binding.miedo, "Miedo") }
        binding.fatalismo.setOnClickListener { selectEmotion(binding.fatalismo, "Fatalismo") }
        binding.frustracion.setOnClickListener { selectEmotion(binding.frustracion, "Frustración") }
        binding.ineficacia.setOnClickListener { selectEmotion(binding.ineficacia, "Ineficacia") }
    }

    private fun selectEmotion(cardView: CardView, emotion: String) {
        selectedCard?.setCardBackgroundColor(ContextCompat.getColor(this, R.color.black))
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.highlight_green))
        selectedCard = cardView
        playSelectionSound()
        saveEmotion(emotion)
    }

    private fun playSelectionSound() {
        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    }

    private fun saveEmotion(emotion: String) {
        if (userId != null) {
            val emotionData = mapOf(
                "emotion" to emotion,
                "registration_progress" to "psico_negative"
            )

            db.collection("users").document(userId!!).set(emotionData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Sentimiento guardado", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, AvatarSelectionActivity::class.java)
                    startActivity(intent)
                    // Eliminamos `finish()` para permitir retroceder a esta actividad
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar el sentimiento: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_SHORT).show()
        }
    }
}
