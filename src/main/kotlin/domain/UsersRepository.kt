package domain

import domain.model.User

interface UsersRepository {
    suspend fun addUser(newUser: User)
    suspend fun removeUser(telegramChatId: Long)
}