package com.example.mindtrade

import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class TradingStyleActivity : AppCompatActivity() {

    private lateinit var cardScalping: CardView
    private lateinit var cardDayTrading: CardView
    private lateinit var cardSwingTrading: CardView
    private var selectedCard: CardView? = null
    private var selectedTradingStyle: String = ""

    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trading_style)

        // Configurar la barra de estado en negro
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        // Ajustar márgenes de la vista principal para bordes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar SoundPool para reproducir sonidos cortos
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        // Cargar el sonido de clic (usa un archivo de sonido que tengas en res/raw)
        soundId = soundPool.load(this, R.raw.select_sound, 1)

        // Referencias de las tarjetas
        cardScalping = findViewById(R.id.cardScalping)
        cardDayTrading = findViewById(R.id.cardDayTrading)
        cardSwingTrading = findViewById(R.id.cardSwingTrading)

        // Configurar listeners para seleccionar tarjetas
        cardScalping.setOnClickListener { selectCard(cardScalping, "Scalping") }
        cardDayTrading.setOnClickListener { selectCard(cardDayTrading, "Day Trading") }
        cardSwingTrading.setOnClickListener { selectCard(cardSwingTrading, "Swing Trading") }
    }

    private fun selectCard(cardView: CardView, style: String) {
        // Desmarcar la tarjeta previamente seleccionada, restaurando su color original
        selectedCard?.setCardBackgroundColor(ContextCompat.getColor(this, R.color.black))

        // Marcar la tarjeta actualmente seleccionada en verde fosforescente
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.highlight_green))

        // Guardar el estilo de trading seleccionado
        selectedTradingStyle = style

        // Reproducir sonido de selección
        playSelectionSound()

        // Ir a la pantalla de registro después de la selección
        navigateToRegisterActivity()

        // Actualizar la referencia a la tarjeta seleccionada
        selectedCard = cardView
    }

    private fun playSelectionSound() {
        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    }

    private fun navigateToRegisterActivity() {
        // Crear el Intent para abrir RegisterActivity y pasar el estilo seleccionado
        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra("TRADING_STYLE", selectedTradingStyle)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Liberar SoundPool al destruir el Activity
        soundPool.release()
    }
}
