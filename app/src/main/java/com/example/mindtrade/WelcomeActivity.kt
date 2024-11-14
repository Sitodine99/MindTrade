package com.example.mindtrade

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    private fun getUserIdFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("MindTradePrefs", MODE_PRIVATE)
        return sharedPreferences.getString("USER_ID", null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val userId = getUserIdFromPreferences()
        if (userId == null) {
            Toast.makeText(this, "Error: ID de usuario no encontrado", Toast.LENGTH_LONG).show()
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
    }
}


