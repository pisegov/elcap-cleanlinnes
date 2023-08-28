package domain.model

data class User(
    override val telegramChatId: Long,
    val name: String = "",
    val username: String? = null,
) : Receiver, Admin {
    override val fullName: String
        get() = username?.let { "$name ($username)" } ?: name
    override val chatTitle: String
        get() = fullName
}
