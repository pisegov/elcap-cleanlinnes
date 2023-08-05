package domain.model


data class Chat(
    override val telegramChatId: Long,
    override val title: String,
) : Receiver
