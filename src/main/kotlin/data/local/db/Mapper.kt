package data.local.db

import domain.model.Chat
import domain.model.User

fun ChatDTO.toChat(): Chat {
    return Chat(telegramChatId, title)
}

fun UserDTO.toUser(): User {
    return User(telegramChatId, name, username)
}
