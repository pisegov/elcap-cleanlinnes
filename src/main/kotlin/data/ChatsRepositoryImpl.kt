package data

import data.local.ChatsDataSource
import domain.ChatsRepository
import domain.model.Chat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatsRepositoryImpl @Inject constructor(private val dataSource: ChatsDataSource) : ChatsRepository {
    override suspend fun addChat(chat: Chat) = withContext(Dispatchers.IO) {
        dataSource.addChat(chat)
    }

    override suspend fun removeChat(telegramChatId: Long) = withContext(Dispatchers.IO) {
        dataSource.removeChat(telegramChatId)
    }

    override suspend fun getChats(): List<Chat> = withContext(Dispatchers.IO) {
        return@withContext dataSource.getChats()
    }
}