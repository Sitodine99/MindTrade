package com.example.mindtrade

import adapters.SimpleStrategyAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.R
import com.example.mindtrade.model.Strategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyStrategiesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null
    private var userAlias: String = "Anónimo" // Alias por defecto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_strategies)

        recyclerView = findViewById(R.id.recyclerViewMyStrategies)
        recyclerView.layoutManager = LinearLayoutManager(this)

        userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadUserAliasAndStrategies()
    }

    private fun loadUserAliasAndStrategies() {
        // Obtener alias del usuario desde la colección "users"
        db.collection("users").document(userId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userAlias = document.getString("alias") ?: "Anónimo"
                }

                // Una vez obtenido el alias, cargar las estrategias
                loadUserStrategies()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar alias: ${exception.message}", Toast.LENGTH_SHORT).show()
                loadUserStrategies() // Cargar estrategias aunque falle el alias
            }
    }

    private fun loadUserStrategies() {
        db.collection("strategies")
            .whereEqualTo("createdBy", userId)
            .get()
            .addOnSuccessListener { result ->
                val strategies = result.map { document ->
                    Strategy(
                        id = document.id,
                        title = document.getString("title") ?: "Sin título",
                        description = document.getString("description") ?: "Sin descripción",
                        author = userAlias, // Usar alias del usuario
                        avatarName = document.getString("avatarName"),
                        avatarUrl = document.getString("avatarUrl"),
                        rating = document.getDouble("rating") ?: 0.0,
                        createdBy = document.getString("createdBy") ?: "",
                        indicators = (document.get("indicators") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        timeframes = (document.get("timeframes") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        tradingStyles = (document.get("tradingStyles") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    )
                }

                recyclerView.adapter = SimpleStrategyAdapter(strategies)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar estrategias: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

