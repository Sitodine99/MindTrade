package model

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
