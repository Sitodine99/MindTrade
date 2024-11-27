package com.example.mindtrade

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ImageDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        val rootView: LinearLayout = findViewById(R.id.rootLayout)
        val imageView: ImageView = findViewById(R.id.photoImageView)
        val textView: TextView = findViewById(R.id.photoTextView)

        // Recuperar los datos de la intención
        val imageResId = intent.getIntExtra("imageResId", 0)
        val imageName = intent.getStringExtra("imageName") ?: "Sin nombre"

        // Establecer la imagen y el texto
        imageView.setImageResource(imageResId)
        textView.text = imageName

        // Listas de emociones negativas y positivas (basadas en emotionText del MainActivity)
        val negativeEmotionTexts = listOf(
            "Trader ansioso", "Trader impaciente", "Trader descontrolado",
            "Trader avaricioso", "Trader insatisfecho", "Trader rabioso",
            "Trader avergonzado", "Trader confundido", "Trader atemorizado",
            "Trader fatalista", "Trader frustrado", "Trader ineficiente"
        )

        val positiveEmotionTexts = listOf(
            "Trader autocontrolado", "Trader confiado", "Trader eficiente",
            "Trader optimista", "Trader paciente", "Trader realizado",
            "Trader satisfecho", "Trader seguro", "Trader en sintonía",
            "Trader tranquilo", "Trader aceptado", "Trader afirmativo"
        )

        // Cambiar el color del texto según el grupo al que pertenezca
        when (imageName) {
            "Psico +" -> textView.setTextColor(resources.getColor(R.color.highlight_green, theme))
            "Psico -" -> textView.setTextColor(resources.getColor(R.color.my_red, theme))
            in negativeEmotionTexts -> textView.setTextColor(resources.getColor(R.color.my_red, theme))
            in positiveEmotionTexts -> textView.setTextColor(resources.getColor(R.color.highlight_green, theme))
            "Day trader" -> textView.setTextColor(resources.getColor(R.color.turquoise_blue, theme))
            "Scalper" -> textView.setTextColor(resources.getColor(R.color.orange, theme))
            "Swing trader" -> textView.setTextColor(resources.getColor(R.color.blue_light, theme))
            else -> textView.setTextColor(resources.getColor(android.R.color.white, theme))
        }

        // Configurar OnClickListener para cerrar la actividad al tocar la pantalla
        rootView.setOnClickListener {
            finishWithFade()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Usar la transición de fade al cerrar la actividad
        finishWithFade()
    }
}

