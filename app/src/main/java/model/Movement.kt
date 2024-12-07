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
    val profit: Double = 0.0, // Beneficio neto
    //val grossProfit: Double = 0.0, // Beneficio bruto (nuevo campo)//
    val createdAt: Long = System.currentTimeMillis(), // Fecha de creación
    val strategyId: String? = null, // Estrategia asociada
    val emotionalState: String?,
    val emotion: String? = null, // Emoción asociada
    val tradingStyle: String, // Agregado
    val comments: String?, // Comentarios
    val photos: List<String>?, // Fotos asociadas
    val lotes: Double = 0.0 // Número de lotes por operación
) {
    // Constructor sin argumentos requerido por Firebase
    constructor() : this(
        id = "",
        accountId = "",
        symbol = "",
        type = "Buy",
        entryPrice = 0.0,
        exitPrice = 0.0,
        entryTime = 0L,
        exitTime = null,
        swap = 0.0,
        commission = 0.0,
        profit = 0.0,
        //grossProfit = 0.0, // Inicialización del nuevo campo//
        createdAt = 0L,
        strategyId = null,
        emotionalState = null,
        emotion = null,
        tradingStyle = "",
        comments = null,
        photos = emptyList(),
        lotes = 0.0
    )
}
