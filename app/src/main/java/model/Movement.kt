package com.example.mindtrade.model

data class Movement(
    val id: String = "", // ID único del movimiento
    val accountId: String = "", // ID de la cuenta asociada
    val symbol: String = "", // Símbolo del activo
    val type: String = "Buy", // Tipo de operación (Buy/Sell)
    val entryPrice: Double = 0.0, // Precio de entrada
    val exitPrice: Double = 0.0, // Precio de salida
    val entryTime: Long, // Agregado
    val exitTime: Long?, // Agregado
    val swap: Double = 0.0, // Swap
    val commission: Double = 0.0, // Comisión
    val profit: Double? = null, // Beneficio
    val createdAt: Long = System.currentTimeMillis(), // Fecha de creación
    val strategyId: String? = null, // Estrategia asociada
    val emotionalState: String?,
    val emotion: String? = null, // Emoción asociada
    val tradingStyle: String, // Agregado
    val comments: String?,// Comentarios
    val photos: List<String>? // Agregado
)


