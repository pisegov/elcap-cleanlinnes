package data.local.db

data class UserDTO(
    val id: Int,
    val telegramChatId: Long,
    val name: String,
    val username: String?,
)
