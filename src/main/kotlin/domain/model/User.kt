package domain.model

data class User(
    override val telegramChatId: Long,
    val name: String = "",
    val username: String? = null,
) : Receiver {
    override val title: String
        get() = username?.let { "$name ($username)" } ?: name
}
