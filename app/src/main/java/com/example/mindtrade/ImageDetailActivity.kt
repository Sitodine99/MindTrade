package com.example.mindtrade

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class ImageDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        val rootView: LinearLayout = findViewById(R.id.rootLayout)
        val imageView: ImageView = findViewById(R.id.photoImageView)
        val textView: TextView = findViewById(R.id.photoTextView)

        // Recuperar los datos de la intención
        val imageUrl = intent.getStringExtra("imageUrl") // URL de la imagen
        val imageResId = intent.getIntExtra("imageResId", 0) // Recurso local
        val imageName = intent.getStringExtra("imageName") ?: "Sin nombre"

        // Establecer el texto
        textView.text = imageName

        // Cargar la imagen según sea una URL o un recurso local
        if (!imageUrl.isNullOrEmpty() && imageUrl.startsWith("https://")) {
            Glide.get(this).clearMemory() // Limpia la caché de memoria (debe ejecutarse en el hilo principal)
            Thread {
                Glide.get(this).clearDiskCache() // Limpia la caché en disco (debe ejecutarse en un hilo en segundo plano)
            }.start()

            Glide.with(this)
                .load(imageUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.interrogacion_icon)
                .error(R.drawable.interrogacion_icon)
                .into(imageView)

        } else if (imageResId != 0) {
            // Cargar desde un recurso local
            imageView.setImageResource(imageResId)
        } else {
            // Imagen predeterminada
            imageView.setImageResource(R.drawable.interrogacion_icon)
        }

        // Configurar OnClickListener para cerrar la actividad al tocar la pantalla
        rootView.setOnClickListener {
            finishWithFade()
        }

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
