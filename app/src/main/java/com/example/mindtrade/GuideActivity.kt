package com.example.mindtrade

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Typeface

class GuideActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        supportActionBar?.title = "Guía de uso"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val guideTextView: TextView = findViewById(R.id.guideTextView)

        // Texto estructurado con secciones clave
        val guideText = """
            📌 Bienvenido a Mind Trade
            
            🔹 Crear una cuenta simulada  
              
            1. Ve al módulo de cuentas en el dashboard.  
            2. Pulsa el botón + para crear una nueva cuenta.  
            3. Introduce los siguientes datos:  
               • Nombre de la cuenta  
               • Balance inicial  
               • Divisa de la cuenta (USD, EUR, GBP)  
               • Objetivo de beneficio  
               • Pérdida máxima diaria  
            4. Guarda la cuenta y empieza a registrar operaciones.  
            
            🔹 Cómo registrar una estrategia  
              
            1. Pulsa el botón + en el módulo de estrategias.  
            2. Introduce los datos principales:  
               • Título  
               • Condiciones de entrada y salida  
               • Indicadores, símbolos y temporalidades  
               • Opcional: Agrega imágenes y código algorítmico  
            3. Guarda la estrategia para compartirla con la comunidad.  
            
            🔹 Registro de operaciones en cuentas simuladas  
             
            Para registrar una operación debes haber creado una estrategia o haber añadido una estrategia de la comunidad a "Mis Favoritas".  
             
            1. Pulsa en una cuenta y selecciona Registrar movimiento.  
            2. Introduce los datos de la operación:  
               • Activo operado  
               • Cantidad de lotes  
               • Tipo de operación (Compra/Venta)  
               • Precio de entrada y salida  
               • Comisión y swap  
               • Estrategia utilizada  
               • Estado emocional y emoción específica  
            3. Guarda el movimiento y visualiza su impacto en la cuenta.  
            
            🔹 Búsqueda y consulta de estrategias  
            
            • Explora estrategias recientes desde el módulo de estrategias en el dashboard.  
            • Usa la función de búsqueda avanzada para filtrar por nombre, estilo de trading, indicadores, etc.  
            • Marca estrategias como favoritas para consultarlas más tarde.  
            
            🔹 Consulta de métricas y gráficos  
              
            Dentro del detalle de cada cuenta encontrarás:  
             
            • Evolución del balance con el historial de operaciones.  
            • Gráficos emocionales con el registro de emociones en tus operaciones.  
            • Ratio de éxito para analizar tu rendimiento.  
        """.trimIndent()


// Aplicar negritas a las secciones clave
        val spannableString = SpannableString(guideText)
        val boldSections = listOf(
            "📌 Bienvenido a Mind Trade",
            "🔹 Cómo registrar una estrategia",
            "🔹 Registro de operaciones en cuentas simuladas",
            "🔹 Búsqueda y consulta de estrategias",
            "🔹 Consulta de métricas y gráficos"
        )

        for (section in boldSections) {
            val start = guideText.indexOf(section)
            if (start >= 0) {
                spannableString.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    start + section.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        guideTextView.text = spannableString


        for (section in boldSections) {
            val start = guideText.indexOf(section)
            if (start >= 0) {
                spannableString.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    start + section.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        guideTextView.text = spannableString
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
