package handlers.user

import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.chat.PrivateChat
import domain.model.User
import util.ChatId
import javax.inject.Inject

class ChatInfoRetreiver @Inject constructor(
    private val behaviourContext: BehaviourContext,
) {

    suspend fun getUserByChatId(chatId: Long): User {
        val telegramUser = behaviourContext.getChat(chatId = ChatId(chatId)) as PrivateChat
        val user = User(
            telegramChatId = telegramUser.id.chatId.long,
            name = "${telegramUser.firstName} ${telegramUser.lastName}".trim(),
            username = telegramUser.username?.username
        )
        return user
    }
}