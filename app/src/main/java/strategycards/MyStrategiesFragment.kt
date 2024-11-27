package com.example.mindtrade

import adapters.SimpleStrategyAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindtrade.model.Strategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyStrategiesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null
    private var userAlias: String = "Anónimo" // Alias por defecto

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_my_strategies, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewMyStrategies)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return view
        }

        loadUserAliasAndStrategies()
        return view
    }

    // 1. Cargar el alias del usuario y luego las estrategias
    private fun loadUserAliasAndStrategies() {
        // Obtener alias del usuario desde Firestore
        db.collection("users").document(userId!!)
            .get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    userAlias = userDocument.getString("alias") ?: "Anónimo"
                }

                // Cargar estrategias después de obtener el alias
                loadUserStrategies()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error al cargar alias: ${exception.message}", Toast.LENGTH_SHORT).show()

                // Cargar estrategias incluso si no se pudo obtener el alias
                loadUserStrategies()
            }
    }

    // 2. Cargar estrategias usando el alias del usuario logueado
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
                        author = userAlias, // Aquí usamos SIEMPRE el alias del usuario actual
                        avatarName = document.getString("avatarName"),
                        avatarUrl = document.getString("avatarUrl"),
                        rating = document.getDouble("rating") ?: 0.0,
                        createdBy = document.getString("createdBy") ?: "",
                        indicators = (document.get("indicators") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        timeframes = (document.get("timeframes") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        tradingStyles = (document.get("tradingStyles") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    )
                }

                // Configurar el adaptador con las estrategias
                recyclerView.adapter = SimpleStrategyAdapter(strategies, requireActivity())
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error al cargar estrategias: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

