package data.local.user

data class UserDTO(
    val id: Int,
    val telegramChatId: Long,
    val name: String,
    val username: String?,
)
