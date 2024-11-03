package com.example.mindtrade

import YourAdapter
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Comentamos enableEdgeToEdge()
        // enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Lista de cuentas de ejemplo
        val accountsList = listOf("Cuenta 1", "Cuenta 2", "Cuenta 3", "Cuenta 4", "Cuenta 5")

        // Configurar el RecyclerView para desplazamiento horizontal
        val accountsRecyclerView = findViewById<RecyclerView>(R.id.accountsRecyclerView)
        accountsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        accountsRecyclerView.adapter = YourAdapter(accountsList)

        // Comentamos la configuración de insets para manejar el padding
        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        */

        // Configuración del botón de cierre de sesión
        val signOutButton = findViewById<Button>(R.id.button)
        signOutButton.setOnClickListener {
            // Cerrar sesión de Firebase
            FirebaseAuth.getInstance().signOut()

            // Redirigir a LoginActivity después de cerrar sesión
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finalizar MainActivity para que no esté en el historial
        }
    }
}
