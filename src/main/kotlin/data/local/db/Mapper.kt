package data.local.db

import domain.model.Chat

fun ChatDTO.toChat(): Chat {
    return Chat(telegramChatId, title)
}