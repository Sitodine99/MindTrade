package welcome

import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.mindtrade.R
import com.example.mindtrade.databinding.ActivityPsicoPositiveBinding
import com.example.mindtrade.finishWithFade
import com.example.mindtrade.startActivityWithFade
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class PsicoPositiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPsicoPositiveBinding
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
        binding = ActivityPsicoPositiveBinding.inflate(layoutInflater)
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
            finishWithFade()
            return
        }

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

        // Configuración de la animación para el botón de "Atrás"
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishWithFade()  // Termina con animación de fade-out al presionar "Atrás"
            }
        })
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
                "registration_progress" to "psico_positive"
            )

            db.collection("users").document(userId!!).set(emotionData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Sentimiento guardado", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, AvatarSelectionActivity::class.java)
                    startActivityWithFade(intent)
                    // Eliminamos `finish()` para permitir retroceder a esta actividad si el usuario lo desea
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar el sentimiento: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_SHORT).show()
        }
    }
}
