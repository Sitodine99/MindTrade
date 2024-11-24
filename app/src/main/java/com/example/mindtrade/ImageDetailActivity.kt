package com.example.mindtrade

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ImageDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        val imageView: ImageView = findViewById(R.id.photoImageView)
        val textView: TextView = findViewById(R.id.photoTextView)

        // Recuperar los datos de la intención
        val imageResId = intent.getIntExtra("imageResId", 0)
        val imageName = intent.getStringExtra("imageName") ?: "Sin nombre"

        // Establecer la imagen y el texto
        imageView.setImageResource(imageResId)
        textView.text = imageName
    }

    override fun onBackPressed() {
        // Usar la transición de fade al cerrar la actividad
        finishWithFade()
    }
}
