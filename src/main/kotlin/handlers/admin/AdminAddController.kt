package handlers.admin

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitUserSharedEventsMessages
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.message.abstracts.ChatEventMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.request.UserShared
import domain.AdminsRepository
import domain.states.InsertionState
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import util.KeyboardBuilder
import util.getChatTitle
import util.removedDoubleSpaces
import javax.inject.Inject

class AdminAddController @Inject constructor(
    private val behaviourContext: BehaviourContext,
    private val adminsRepository: AdminsRepository,
) {
    suspend fun addAdmin(receivedMessage: CommonMessage<TextContent>) {
        with(behaviourContext) {
            reply(
                receivedMessage,
                "Воспользуйтесь кнопкой, чтобы отправить боту пользователя:",
                replyMarkup = KeyboardBuilder.shareAdminKeyboard(),
            )

            val shared = waitUserSharedEventsMessages().filter { message ->
                message.sameThread(receivedMessage)
            }.first()

            handleSharedAdmin(shared)
        }
    }

    private fun handleSharedAdmin(eventMessage: ChatEventMessage<UserShared>) {
        val chatIdentifier: UserId = eventMessage.chatEvent.userId
        val userId: Long = chatIdentifier.chatId
        behaviourContext.launch {
            val insertionState: InsertionState = adminsRepository.addAdmin(userId)
            val chatTitle: String = chatIdentifier.getChatTitle(behaviourContext)
            val replyString: String = getReplyString(insertionState, chatTitle)

            behaviourContext.reply(
                eventMessage,
                replyString,
                replyMarkup = ReplyKeyboardRemove()
            )
        }
    }

    private fun getReplyString(insertionState: InsertionState, chatTitle: String): String {
        val replyString = StringBuilder()
        when (insertionState) {
            is InsertionState.Success -> {
                replyString.append("Администратор $chatTitle сохранён :)\n")
                replyString.append(receiverReply(chatIsActivated = chatTitle.isNotBlank()))
            }

            is InsertionState.Duplicate -> {
                replyString.append("Администратор $chatTitle уже добавлен в систему :)\n")
                replyString.append(receiverReply(chatIsActivated = chatTitle.isNotBlank()))
            }

            is InsertionState.Error -> {
                replyString.append("Что-то пошло не так, бот не смог добавить админа в систему :(")
            }
        }

        return replyString.removedDoubleSpaces()
    }

    private fun receiverReply(chatIsActivated: Boolean): String {
        return when {
            chatIsActivated -> {
                "Пользователю доступны команды администратора"
            }

            else -> {
                "Пользователь ещё не активировал чат с ботом\nКак только он это сделает, ему будут доступны команды администратора"
            }
        }
    }
}