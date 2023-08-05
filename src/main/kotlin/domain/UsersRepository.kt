package domain

import domain.model.User

interface UsersRepository {
    fun addUser(newUser: User)
    fun removeUser(telegramChatId: Long)


}