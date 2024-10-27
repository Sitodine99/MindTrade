package com.example.mindtrade

import YourAdapter
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Lista de cuentas de ejemplo
        val accountsList = listOf("Cuenta 1", "Cuenta 2", "Cuenta 3", "Cuenta 4", "Cuenta 5")

        // Configurar el RecyclerView para desplazamiento horizontal
        val accountsRecyclerView = findViewById<RecyclerView>(R.id.accountsRecyclerView)
        accountsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        accountsRecyclerView.adapter = YourAdapter(accountsList)

        // Configuración de insets para manejar el padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
