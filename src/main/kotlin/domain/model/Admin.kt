package domain.model

interface Admin : Chat {
    override val telegramChatId: Long
    val fullName: String
}
