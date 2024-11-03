package com.example.mindtrade

import android.media.SoundPool
import android.content.Intent
import android.media.AudioAttributes
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trading_style)

        userId = intent.getStringExtra("USER_ID")
        if (userId == null) {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_LONG).show()
            finish()
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
            val userData = mapOf("trading_style" to selectedTradingStyle)
            db.collection("users").document(userId!!).set(userData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Estilo de trading guardado", Toast.LENGTH_SHORT).show()


                    val emotionsIntent = Intent(this, EmotionsActivity::class.java)
                    emotionsIntent.putExtra("USER_ID", userId)
                    startActivity(emotionsIntent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar el estilo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_SHORT).show()
        }
    }
}
