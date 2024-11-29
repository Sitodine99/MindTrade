package com.example.mindtrade.model

data class Comment(
    val id: String = "", // ID del comentario
    val userId: String = "", // ID del usuario que hizo el comentario
    val userAlias: String = "", // Alias del usuario
    val avatarUrl: String? = null, // URL del avatar
    val content: String = "", // Contenido del comentario
    val timestamp: Long = System.currentTimeMillis(), // Timestamp del comentario
    val replies: List<Comment> = emptyList() // Respuestas al comentario
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
    val algorithmCode: String = "",
    val favoritedBy: List<String> = emptyList(),
    val userRatings: Map<String, Double> = emptyMap(),
    val comments: List<Comment> = emptyList() // Nuevo campo
)





