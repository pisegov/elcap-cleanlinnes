package handlers.user

import domain.AdminsRepository
import domain.ReceiversRepository
import domain.UsersRepository
import domain.model.Admin
import javax.inject.Inject

class ChatInteractor @Inject constructor(
    private val chatInfoRetreiver: ChatInfoRetreiver,
    private val receiversRepository: ReceiversRepository,
    private val usersRepository: UsersRepository,
    private val adminsRepository: AdminsRepository,
) {
    suspend fun checkIfAdmin(chatId: Long): Boolean {
        return adminsRepository.getAdminsList().map { it.telegramChatId }.contains(chatId)
    }

    suspend fun checkIfReceiver(chatId: Long): Boolean {
        return receiversRepository.getReceiversList().map { it.telegramChatId }.contains(chatId)
    }

    suspend fun saveUser(chatId: Long) {
        val user = chatInfoRetreiver.getUserByChatId(chatId)
        usersRepository.addUser(user)
    }

    suspend fun deleteUser(telegramChatId: Long) {
        usersRepository.removeUser(telegramChatId)
        receiversRepository.removeReceiver(telegramChatId)
        adminsRepository.removeAdmin(telegramChatId)
    }

    suspend fun getAdminList(): List<Admin> {
        return adminsRepository.getAdminsList()
    }
}