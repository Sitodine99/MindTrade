package com.example.mindtrade

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mindtrade.model.Account
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var accountNameInput: EditText
    private lateinit var accountBalanceInput: EditText
    private lateinit var currencySpinner: Spinner
    private lateinit var profitTargetInput: EditText
    private lateinit var maxDailyLossInput: EditText
    private lateinit var saveAccountButton: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        // Inicializar vistas
        accountNameInput = findViewById(R.id.accountNameInput)
        accountBalanceInput = findViewById(R.id.accountBalanceInput)
        currencySpinner = findViewById(R.id.currencySpinner)
        profitTargetInput = findViewById(R.id.profitTargetInput)
        maxDailyLossInput = findViewById(R.id.maxDailyLossInput)
        saveAccountButton = findViewById(R.id.saveAccountButton)

        // Configurar el botón "Guardar Cuenta"
        saveAccountButton.setOnClickListener {
            saveAccount()
        }
    }

    private fun saveAccount() {
        // Obtener el UID del usuario autenticado
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener los valores de los campos
        val name = accountNameInput.text.toString()
        val balance = accountBalanceInput.text.toString().toDoubleOrNull() ?: 0.0
        val currency = currencySpinner.selectedItem.toString()
        val profitTarget = profitTargetInput.text.toString().toDoubleOrNull()
        val maxDailyLoss = maxDailyLossInput.text.toString().toDoubleOrNull()

        // Validar campos obligatorios
        if (name.isEmpty()) {
            Toast.makeText(this, "El nombre de la cuenta es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear el objeto `Account` con los valores proporcionados
        val newAccount = Account(
            id = db.collection("accounts").document().id, // Generar un ID único para Firestore
            userId = userId, // UID del usuario actual
            name = name, // Nombre de la cuenta
            balance = balance, // Balance inicial
            currency = currency, // Moneda seleccionada
            profitTarget = profitTarget, // Objetivo de beneficio
            maxDailyLoss = maxDailyLoss, // Pérdida diaria máxima
            createdAt = System.currentTimeMillis(), // Fecha de creación
            movements = emptyList() // Lista vacía para movimientos
        )

        // Guardar la cuenta en Firestore
        db.collection("accounts").document(newAccount.id).set(newAccount)
            .addOnSuccessListener {
                Toast.makeText(this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
                finish() // Cerrar la actividad actual
            }
            .addOnFailureListener { e ->
                // Mostrar el error en la consola y al usuario
                Log.e("FirestoreError", "Error al guardar la cuenta: ${e.message}")
                Toast.makeText(this, "Error al guardar la cuenta: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}

