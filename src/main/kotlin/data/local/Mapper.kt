package data.local

import data.local.group.GroupDTO
import data.local.user.UserDTO
import domain.model.Group
import domain.model.User

fun GroupDTO.toGroup(): Group {
    return Group(telegramChatId, title)
}

fun UserDTO.toUser(): User {
    return User(telegramChatId, name, username)
}
