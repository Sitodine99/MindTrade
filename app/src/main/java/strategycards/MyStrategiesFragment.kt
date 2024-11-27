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

        loadUserStrategies()
        return view
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
                        author = document.getString("author") ?: "Anónimo",
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
                Toast.makeText(requireContext(), "Error al cargar estrategias: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
