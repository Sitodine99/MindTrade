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
import com.example.mindtrade.databinding.ActivityPsicoPositiveBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class PsicoPositiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPsicoPositiveBinding
    private var userId: String? = null
    private val db = FirebaseFirestore.getInstance()

    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0
    private var selectedCard: CardView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPsicoPositiveBinding.inflate(layoutInflater)
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
        soundId = soundPool.load(this, R.raw.select_sound, 1)

        // Obtener el ID del usuario de la intent
        userId = intent.getStringExtra("USER_ID")

        Log.d("PsicoPositiveActivity", "User ID recibido: $userId")

        if (userId == null) {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Configurar la selección de emociones positivas
        binding.autocontrol.setOnClickListener { selectEmotion(binding.autocontrol, "Autocontrol") }
        binding.confianza.setOnClickListener { selectEmotion(binding.confianza, "Confianza") }
        binding.eficiencia.setOnClickListener { selectEmotion(binding.eficiencia, "Eficiencia") }
        binding.optimismo.setOnClickListener { selectEmotion(binding.optimismo, "Optimismo") }
        binding.paciencia.setOnClickListener { selectEmotion(binding.paciencia, "Paciencia") }
        binding.realizacion.setOnClickListener { selectEmotion(binding.realizacion, "Realización") }
        binding.satisfaccion.setOnClickListener { selectEmotion(binding.satisfaccion, "Satisfacción") }
        binding.seguridad.setOnClickListener { selectEmotion(binding.seguridad, "Seguridad") }
        binding.sintonia.setOnClickListener { selectEmotion(binding.sintonia, "Sintonía") }
        binding.tranquilidad.setOnClickListener { selectEmotion(binding.tranquilidad, "Tranquilidad") }
        binding.aceptacion.setOnClickListener { selectEmotion(binding.aceptacion, "Aceptación") }
        binding.afirmacion.setOnClickListener { selectEmotion(binding.afirmacion, "Afirmación") }
    }

    private fun selectEmotion(cardView: CardView, emotion: String) {
        // Cambiar el fondo de la tarjeta seleccionada
        selectedCard?.setCardBackgroundColor(ContextCompat.getColor(this, R.color.black)) // Color de las tarjetas no seleccionadas
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.highlight_green)) // Color de la tarjeta seleccionada
        selectedCard = cardView

        // Reproducir sonido de selección
        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)

        // Guardar el sentimiento en Firebase y redirigir
        saveEmotion(emotion)
    }

    private fun saveEmotion(emotion: String) {
        if (userId != null) {
            val emotionData = mapOf("emotion" to emotion)

            // Guardar el sentimiento en Firebase
            db.collection("users").document(userId!!).set(emotionData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Sentimiento guardado", Toast.LENGTH_SHORT).show()

                    // Redirigir a AvatarSelectionActivity
                    val intent = Intent(this, AvatarSelectionActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar el sentimiento: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_SHORT).show()
        }
    }
}
