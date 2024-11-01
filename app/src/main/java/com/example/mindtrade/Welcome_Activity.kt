package com.example.mindtrade

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.auth.FirebaseAuth

class Welcome_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        // Configurar la barra de estado en negro
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        // Cambiar los iconos de la barra de estado a blanco
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        FirebaseAuth.getInstance().signOut() // Forzar cierre de sesión
        // Verificar si el usuario está autenticado en Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Usuario autenticado, redirigir a MainActivity
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish() // Finalizar WelcomeActivity
        } else {
            // Usuario no autenticado, mostrar la pantalla de bienvenida
            initWelcomeScreen()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initWelcomeScreen() {
        // Configurar el botón "Comencemos"
        findViewById<Button>(R.id.startbutton).setOnClickListener {
            // Redirigir a la pantalla de estilo de trading
            val tradingStyleIntent = Intent(this, TradingStyleActivity::class.java)
            startActivity(tradingStyleIntent)
        }
    }
}

