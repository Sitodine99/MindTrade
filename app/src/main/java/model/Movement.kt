package com.example.mindtrade.model

data class Movement(
    val id: String = "", // ID del movimiento
    val accountId: String = "", // ID de la cuenta asociada
    val userId: String = "", // Usuario que registra el movimiento
    val alias: String = "", // Alias del usuario que registra
    val symbol: String = "", // Activo (e.g., EUR/USD)
    val type: String = "buy", // Tipo de operación: buy o sell
    val entryPrice: Double = 0.0, // Precio de entrada
    val exitPrice: Double? = null, // Precio de salida (opcional)
    val entryTime: Long = System.currentTimeMillis(), // Fecha y hora de entrada
    val exitTime: Long? = null, // Fecha y hora de salida (opcional)
    val commission: Double = 0.0, // Comisión
    val swap: Double = 0.0, // Swap
    val strategyId: String? = null, // ID de la estrategia utilizada (puede ser "ninguna")
    val profit: Double? = null // Beneficio de la operación (opcional)
)
