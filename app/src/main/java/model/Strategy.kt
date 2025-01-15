package com.example.mindtrade.model

import model.Comment

data class Strategy(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    var author: String = "",
    val avatarName: String? = null,
    val avatarUrl: String? = null,
    val rating: Double = 0.0,
    val totalVotes: Int = 0,
    val createdBy: String = "",
    val indicators: List<String> = emptyList(),
    val timeframes: List<String> = emptyList(),
    val tradingStyles: List<String> = emptyList(),
    val symbols: List<String> = emptyList(),
    val algorithmCode: String = "",
    val favoritedBy: List<String> = emptyList(),
    val userRatings: Map<String, Double> = emptyMap(),
    val comments: List<Comment> = emptyList(),
    val entryConditionImageUrl: String? = null, // Imagen para condiciones de entrada
    val exitConditionImageUrl: String? = null, // Imagen para condiciones de salida
    val movements: List<String> = emptyList() // IDs de los movimientos asociados
)






