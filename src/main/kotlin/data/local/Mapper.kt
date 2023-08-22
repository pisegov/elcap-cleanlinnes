package data.local

import data.local.chat.ChatDTO
import data.local.user.UserDTO
import domain.model.Chat
import domain.model.User

fun ChatDTO.toChat(): Chat {
    return Chat(telegramChatId, title)
}

fun UserDTO.toUser(): User {
    return User(telegramChatId, name, username)
}
