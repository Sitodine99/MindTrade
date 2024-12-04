package com.example.mindtrade

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AccountMovementsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_movements)

        // Datos de la cuenta recibidos como parámetros (Intent)
        val accountName = intent.getStringExtra("accountName") ?: "Nombre no disponible"
        val accountBalance = intent.getDoubleExtra("accountBalance", 0.0)
        val accountCurrency = intent.getStringExtra("accountCurrency") ?: "USD"
        val accountProfitTarget = intent.getDoubleExtra("accountProfitTarget", 0.0)
        val accountMaxDailyLoss = intent.getDoubleExtra("accountMaxDailyLoss", 0.0)
        val accountCreatedAt = intent.getLongExtra("accountCreatedAt", System.currentTimeMillis())
        val accountMovements = intent.getStringArrayListExtra("accountMovements") ?: arrayListOf()
        val accountIsActive = intent.getBooleanExtra("accountIsActive", true)
        // Formatea la fecha de creación//
        val formattedDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(accountCreatedAt))

        // Mostrar los datos en las vistas correspondientes
        findViewById<TextView>(R.id.accountNameTextView).text = accountName
        findViewById<TextView>(R.id.accountBalanceTextView).text = "Balance: $%.2f".format(accountBalance)
        findViewById<TextView>(R.id.accountCurrencyTextView).text = "Divisa: $accountCurrency"
        findViewById<TextView>(R.id.accountProfitTargetTextView).text = "Objetivo: $%.2f".format(accountProfitTarget)
        findViewById<TextView>(R.id.accountMaxDailyLossTextView).text = "Máx pérdida diaria: $%.2f".format(accountMaxDailyLoss)
        findViewById<TextView>(R.id.accountCreationDateTextView).text = "Creada el: $formattedDate"
        findViewById<TextView>(R.id.accountMovementsCountTextView).text = "Movimientos: ${accountMovements.size}"

    }
}
