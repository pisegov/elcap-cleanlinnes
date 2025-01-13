package handlers.user

import dev.inmo.tgbotapi.extensions.utils.extensions.parseCommandsWithArgs
import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.MediaGroupContent
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.content.VisualMediaGroupPartContent
import domain.ReceiversRepository
import domain.model.Admin
import domain.states.BotState
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
        forwardCallToReceivers(message)
    }

    suspend fun handleVisualMediaGroupMessage(message: CommonMessage<MediaGroupContent<VisualMediaGroupPartContent>>) {
    }

    suspend fun handleTextCall(contentMessage: CommonMessage<MessageContent>): BotState {

        val content = contentMessage.content

        return when {
            content is TextContent && content.parseCommandsWithArgs().containsKey("cancel") -> {
                BotState.StopState(contentMessage.chat.id)
            }

            else -> {
                forwardCallToReceivers(contentMessage)

                BotState.InitialState
            }
        }
    }

    suspend fun forwardCallToReceivers(receivedMessage: CommonMessage<MessageContent>) {
        val chatId = receivedMessage.chatId

        val receivers = receiversRepository.getReceiversList()
            // If someone send a message and is a receiver, they don't need to receive this message
            .filter { it.telegramChatId != chatId }

        val forwardedSuccessfully = userMessageSender.forwardMessageToEveryChat(receivers, receivedMessage)

        if (forwardedSuccessfully) {
            userMessageSender.sendMessageOnSuccessfulForward(chatId)
        } else {
            reportForwardingError(receivedMessage)
            userMessageSender.sendMessageOnUnsuccessfulForward(chatId)
        }
    }

    private suspend fun reportForwardingError(receivedMessage: CommonMessage<MessageContent>) {
        val adminsList: List<Admin> = chatInteractor.getAdminList()
        val user: PrivateChat = receivedMessage.chat as PrivateChat
        val date: DateTime = receivedMessage.date
        val offset = TimezoneOffset.local(date)
        val dateTimeTz = date.toOffset(offset)

        val message = """
                   Не было передано сообщение от посетителя!
                   Посетитель: ${user.firstName} ${user.lastName}
                   Время сообщения: ${dateTimeTz.format("yyyy-MM-dd HH:mm:ss")}
                """.trimIndent()

        userMessageSender.sendMessageToEveryChat(adminsList, messageString = message)
        userMessageSender.forwardMessageToEveryChat(adminsList, receivedMessage)
    }
}