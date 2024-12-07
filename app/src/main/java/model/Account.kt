package com.example.mindtrade.model

data class Account(
    val id: String = "", // ID único de la cuenta
    val userId: String = "", // Usuario propietario
    val name: String = "", // Nombre de la cuenta (máximo 10 caracteres)
    val balance: Double = 0.0, // Balance inicial (no puede ser negativo)
    val currency: String = "USD", // Divisa asociada (USD, EUR, etc.)
    val profitTarget: Double? = null, // Objetivo de beneficio (opcional)
    val maxDailyLoss: Double? = null, // Pérdida diaria máxima (opcional)
    val createdAt: Long = System.currentTimeMillis(), // Fecha de creación en timestamp
    var movements: List<String> = emptyList(),
    val isActive: Boolean = true, // Indica si la cuenta está activa
    var movementsCount: Int = 0 // Campo adicional para el número de movimientos
)
