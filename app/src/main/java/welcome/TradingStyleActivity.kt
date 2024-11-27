package welcome

import android.media.SoundPool
import android.content.Intent
import android.media.AudioAttributes
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.mindtrade.R
import com.example.mindtrade.finishWithFade
import com.example.mindtrade.startActivityWithFade
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class TradingStyleActivity : AppCompatActivity() {

    private lateinit var cardScalping: CardView
    private lateinit var cardDayTrading: CardView
    private lateinit var cardSwingTrading: CardView
    private var selectedCard: CardView? = null
    private var selectedTradingStyle: String = ""
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null

    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0

    private fun getUserIdFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("MindTradePrefs", MODE_PRIVATE)
        return sharedPreferences.getString("USER_ID", null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trading_style)

        userId = getUserIdFromPreferences()
        if (userId == null) {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_LONG).show()
            finishWithFade()  // Termina con la animación si falta el ID de usuario
            return
        }

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

        cardScalping = findViewById(R.id.cardScalping)
        cardDayTrading = findViewById(R.id.cardDayTrading)
        cardSwingTrading = findViewById(R.id.cardSwingTrading)

        cardScalping.setOnClickListener { selectCard(cardScalping, "Scalping") }
        cardDayTrading.setOnClickListener { selectCard(cardDayTrading, "Day Trading") }
        cardSwingTrading.setOnClickListener { selectCard(cardSwingTrading, "Swing Trading") }

        // Configurar el comportamiento personalizado para el botón de atrás
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishWithFade()  // Llama a la función de transición personalizada
            }
        })
    }

    private fun selectCard(cardView: CardView, style: String) {
        selectedCard?.setCardBackgroundColor(ContextCompat.getColor(this, R.color.black))
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.highlight_green))

        selectedTradingStyle = style
        selectedCard = cardView
        playSelectionSound()
        saveTradingStyleAndProceed()
    }

    private fun playSelectionSound() {
        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    }

    private fun saveTradingStyleAndProceed() {
        if (userId != null) {
            val userData = mapOf(
                "trading_style" to selectedTradingStyle,
                "registration_progress" to "trading_style"
            )
            db.collection("users").document(userId!!).set(userData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Estilo de trading guardado", Toast.LENGTH_SHORT).show()
                    val emotionsIntent = Intent(this, EmotionsActivity::class.java)
                    startActivityWithFade(emotionsIntent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar el estilo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_SHORT).show()
        }
    }
}
