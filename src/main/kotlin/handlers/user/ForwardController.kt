package handlers.user

import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.tgbotapi.extensions.utils.extensions.parseCommandsWithArgs
import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.*
import domain.ReceiversRepository
import domain.model.*
import domain.states.BotState
import handlers.message_types.ExplicitCallMessage
import handlers.message_types.ForwardableMessage
import handlers.message_types.SingleMediaContentMessage
import handlers.message_types.VisualMediaGroupContentMessage
import korlibs.time.DateTime
import korlibs.time.TimezoneOffset
import util.chatId
import javax.inject.Inject

class ForwardController @Inject constructor(
    private val userMessageSender: UserMessageSender,
    private val chatInteractor: ChatInteractor,
    private val receiversRepository: ReceiversRepository,
){
    suspend fun handleVisualContentMessage(message: CommonMessage<VisualMediaGroupPartContent>) {
        forwardCallToReceivers(SingleMediaContentMessage(message))
    }

    suspend fun handleVisualMediaGroupMessage(message: CommonMessage<MediaGroupContent<VisualMediaGroupPartContent>>) {
        forwardCallToReceivers(VisualMediaGroupContentMessage(message))
    }

    suspend fun handleTextCall(contentMessage: CommonMessage<MessageContent>): BotState {

        val content = contentMessage.content

        return when {
            content is TextContent && content.parseCommandsWithArgs().containsKey("cancel") -> {
                BotState.StopState(contentMessage.chat.id)
            }

            else -> {
                forwardCallToReceivers(ExplicitCallMessage(contentMessage))

                BotState.InitialState
            }
        }
    }

    private suspend fun forwardCallToReceivers(receivedMessage: ForwardableMessage) {
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
    private suspend fun forwardMessageToEveryChat(list: List<Chat>, message: ForwardableMessage): Boolean {
        var forwardedSuccessfully = false
        list.forEach { chat ->
            runCatchingSafely {
                message.forward(chat = chat, messageSender = userMessageSender)
            }.onSuccess {
                forwardedSuccessfully = true
            }.onFailure { exception ->
                println(exception)
                chatInteractor.deleteUser(chat.telegramChatId)
            }
        }
        return forwardedSuccessfully
    }

    private suspend fun reportForwardingError(receivedMessage: ForwardableMessage) {
        val adminsList: List<Admin> = chatInteractor.getAdminList()
        val user: PrivateChat = receivedMessage.telegramMessage.chat as PrivateChat
        val date: DateTime = receivedMessage.telegramMessage.date
        val offset = TimezoneOffset.local(date)
        val dateTimeTz = date.toOffset(offset)

        val message = """
                   Не было передано сообщение от посетителя!
                   Посетитель: ${user.firstName} ${user.lastName}
                   Время сообщения: ${dateTimeTz.format("yyyy-MM-dd HH:mm:ss")}
                """.trimIndent()

        userMessageSender.sendMessageToEveryChat(adminsList, messageString = message)
        forwardMessageToEveryChat(adminsList, receivedMessage)
    }
}