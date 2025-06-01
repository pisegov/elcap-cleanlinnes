package handlers.user

import dev.inmo.kslog.common.LogLevel
import dev.inmo.kslog.common.log
import dev.inmo.kslog.common.logger
import dev.inmo.tgbotapi.extensions.utils.extensions.parseCommandsWithArgs
import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.*
import domain.ReceiversRepository
import domain.model.*
import domain.states.BotState
import handlers.message_types.*
import korlibs.time.DateTime
import korlibs.time.TimezoneOffset
import util.chatId
import javax.inject.Inject

class ForwardController @Inject constructor(
    private val userMessageSender: UserMessageSender,
    private val chatInteractor: ChatInteractor,
    private val receiversRepository: ReceiversRepository,
) {

    suspend fun handleMessage(message: Message) {
        when (message) {
            is SupportedMessage -> forwardCallToReceivers(message)
            is UnsupportedMessage -> handleForwardingError(message)
        }
    }

    suspend fun handleTextCall(contentMessage: CommonMessage<MessageContent>): BotState {

        val content = contentMessage.content

        return when {
            content is TextContent && content.parseCommandsWithArgs().containsKey("cancel") -> {
                BotState.StopState(contentMessage.chat.id)
            }

            else -> {
                forwardCallToReceivers(SingleMediaContentMessage(contentMessage))

                BotState.InitialState
            }
        }
    }

    suspend fun handleForwardingError(message: UnsupportedMessage, throwable: Throwable? = null) {

        val chatId = message.telegramMessage.chat.id.chatId.long
        userMessageSender.sendMessage(chatId, message.getAnswerMessageText())

        reportForwardingError(message, throwable)
    }

    private suspend fun forwardCallToReceivers(receivedMessage: SupportedMessage) {
        val chatId = receivedMessage.telegramMessage.chatId

        val receivers = receiversRepository.getReceiversList()
            // If someone send a message and is a receiver, they don't need to receive this message
            .filter { it.telegramChatId != chatId }

        val forwardedSuccessfully = forwardMessageToEveryChat(receivers, receivedMessage)

        if (forwardedSuccessfully) {
            userMessageSender.sendMessageOnSuccessfulForward(chatId)
        } else {
            reportForwardingError(receivedMessage)
            userMessageSender.sendMessageOnUnsuccessfulForward(chatId)
        }
    }

    /**
     * @return false if a receiver blocked the bot
     * and the bot can't forward the [message]
     */
    private suspend fun forwardMessageToEveryChat(list: List<Chat>, message: Message): Boolean {
        var forwardedSuccessfully = false
        list.forEach { chat ->
            message.forward(chat = chat, messageSender = userMessageSender)
                .collect { result ->
                    result.onSuccess {
                        forwardedSuccessfully = true
                    }.onFailure { exception ->
                        logger.log(LogLevel.WARNING, exception)
                    }
                }
        }
        return forwardedSuccessfully
    }

    private suspend fun reportForwardingError(receivedMessage: Message, throwable: Throwable? = null) {
        val adminsList: List<Admin> = chatInteractor.getAdminList()
        val user: PrivateChat = receivedMessage.telegramMessage.chat as PrivateChat
        val date: DateTime = receivedMessage.telegramMessage.date
        val offset = TimezoneOffset.local(date)
        val dateTimeTz = date.toOffset(offset)

        val message = """
                   Не было передано сообщение от посетителя!
                   Посетитель: ${user.firstName} ${user.lastName}
                   Время сообщения: ${dateTimeTz.format("yyyy-MM-dd HH:mm:ss")}
                   ${throwable?.let { "Ошибка: $throwable" }.orEmpty()}
                """.trimIndent()

        adminsList.forEach { userMessageSender.sendMessage(it.telegramChatId, message) }
        forwardMessageToEveryChat(adminsList, receivedMessage)
    }
}