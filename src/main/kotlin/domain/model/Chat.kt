package domain.model

interface Chat {
    val telegramChatId: Long
    val chatTitle: String
}

typealias Receiver = Chat
