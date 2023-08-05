package domain.model

data class User(
    override val telegramChatId: Long,
    val name: String = "",
    val username: String,
) : Receiver {
    override val title: String
        get() = "$name ($username)"
}
