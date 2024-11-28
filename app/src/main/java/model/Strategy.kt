package com.example.mindtrade.model

data class Strategy(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val author: String = "",
    val avatarName: String? = null,
    val avatarUrl: String? = null,
    val rating: Double = 0.0,  // Rating promedio
    val totalVotes: Int = 0,  // Número total de votos
    val createdBy: String = "",
    val indicators: List<String> = emptyList(),
    val timeframes: List<String> = emptyList(),
    val tradingStyles: List<String> = emptyList(),
    val algorithmCode: String = "",
    val favoritedBy: List<String> = emptyList(),
    val votedBy: List<String> = emptyList()
)






