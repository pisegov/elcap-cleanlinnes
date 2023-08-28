package domain.model


data class Group(
    override val telegramChatId: Long,
    override val chatTitle: String = "",
) : Receiver
