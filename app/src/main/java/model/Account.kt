package com.example.mindtrade.model

data class Account(
    val id: String = "", // ID de la cuenta
    val userId: String = "", // Usuario propietario
    val name: String = "", // Nombre de la cuenta
    val balance: Double = 0.0, // Balance inicial
    val currency: String = "USD", // Divisa (USD, EUR, etc.)
    val profitTarget: Double? = null, // Objetivo de beneficio (opcional)
    val maxDailyLoss: Double? = null, // Pérdida diaria máxima (opcional)
    val createdAt: Long = System.currentTimeMillis(), // Fecha de creación
    val movements: List<String> = emptyList() // IDs de los movimientos asociados
)
