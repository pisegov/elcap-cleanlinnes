package handlers.user

import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import util.chatId
import javax.inject.Inject

class UserCommandController @Inject constructor(
    private val chatInteractor: ChatInteractor,
    private val messageSender: UserMessageSender,
) {

    suspend fun handleStartCommand(receivedMessage: CommonMessage<TextContent>) {

        val chatId = receivedMessage.chatId
        chatInteractor.saveUser(chatId)
        messageSender.sendWelcomeMessage(chatId)

        val isAdmin = chatInteractor.checkIfAdmin(chatId)
        val isReceiver = chatInteractor.checkIfReceiver(chatId)

        if (isAdmin) {
            messageSender.sendAdminWelcomeMessage(chatId)
        }
        if (isReceiver) {
            messageSender.sendReceiverWelcomeMessage(chatId)
        }
    }

    suspend fun handleHelpCommand(receivedMessage: CommonMessage<MessageContent>) {
        messageSender.sendHelpMessage(receivedMessage.chatId)
    }
}
