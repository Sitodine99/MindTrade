package com.example.mindtrade

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AccountMovementsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_movements)

        // Obtener los datos enviados por el intent
        val accountName = intent.getStringExtra("accountName")
        val accountBalance = intent.getDoubleExtra("accountBalance", 0.0)
        val accountId = intent.getStringExtra("accountId") // Opcional, si se envió

        // Mostrar los datos en las vistas correspondientes
        findViewById<TextView>(R.id.accountNameTextView).text = accountName
        findViewById<TextView>(R.id.accountBalanceTextView).text = "Balance: $accountBalance"
    }
}
