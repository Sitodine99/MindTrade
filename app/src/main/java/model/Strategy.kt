package com.example.mindtrade.model

data class Comment(
    val id: String = "",
    val userId: String = "",
    val userAlias: String = "",
    val avatarUrl: String? = null, // URL del avatar
    val avatarName: String? = null, // Nombre del avatar
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val replies: List<Comment> = emptyList()
)

data class Strategy(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val author: String = "",
    val avatarName: String? = null,
    val avatarUrl: String? = null,
    val rating: Double = 0.0,
    val totalVotes: Int = 0,
    val createdBy: String = "",
    val indicators: List<String> = emptyList(),
    val timeframes: List<String> = emptyList(),
    val tradingStyles: List<String> = emptyList(),
    val symbols: List<String> = emptyList(), // Nuevo campo para los símbolos
    val algorithmCode: String = "",
    val favoritedBy: List<String> = emptyList(),
    val userRatings: Map<String, Double> = emptyMap(),
    val comments: List<Comment> = emptyList() // Campo existente
)





