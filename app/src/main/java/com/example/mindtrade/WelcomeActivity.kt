package com.example.mindtrade

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        //window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        //WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        val userId = intent.getStringExtra("USER_ID")

        if (userId == null) {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_LONG).show()
            // Redirige al login si no hay userId
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        findViewById<Button>(R.id.startbutton).setOnClickListener {
            val tradingStyleIntent = Intent(this, TradingStyleActivity::class.java)
            tradingStyleIntent.putExtra("USER_ID", userId)
            startActivity(tradingStyleIntent)
            finish()
        }

        //ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            //val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            //v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            //insets
        //}
    }
}

