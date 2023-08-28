package handlers.receiver

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitChatSharedRequestEventsMessages
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.message.abstracts.ChatEventMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.request.ChatSharedRequest
import domain.ReceiversRepository
import domain.model.Receiver
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import states.BotState
import util.KeyboardBuilder
import util.getChatTitle
import javax.inject.Inject

class ReceiverActionsController @Inject constructor(
    private val behaviourContext: BehaviourContextWithFSM<BotState>,
    private val receiversRepository: ReceiversRepository,
    private val sharedReceiverHandler: SharedReceiverHandler,
) {
    suspend fun showReceivers(message: CommonMessage<TextContent>) {
        val replyString = StringBuilder()

        val receiversList = receiversRepository.getReceiversList()
        val activeReceivers = receiversList.filter { it.title.isNotEmpty() }
        val notActiveReceivers = receiversList.filter { it.title.isEmpty() }

        if (activeReceivers.isNotEmpty()) {
            replyString.append("Активные получатели:\n\n")
            activeReceivers.forEach { receiver: Receiver ->
                replyString.append("${receiver.title}\nTelegram chat id: ${receiver.telegramChatId}\n\n")
            }
        }
        if (notActiveReceivers.isNotEmpty()) {
            replyString.append("\nНеактивные получатели:\n\n")
            notActiveReceivers.forEach { receiver: Receiver ->
                replyString.append("Telegram chat id: ${receiver.telegramChatId}\n")
            }
        }
        behaviourContext.reply(
            message,
            replyString.toString(),
        )
    }

    suspend fun addReceiver(receivedMessage: CommonMessage<TextContent>) {
        with(behaviourContext) {
            reply(
                receivedMessage,
                "Воспользуйтесь кнопкой, чтобы отправить боту пользователя или группу:",
                replyMarkup = KeyboardBuilder.shareChatKeyboard(),
            )

            val shared = waitChatSharedRequestEventsMessages().filter { message ->
                message.sameThread(receivedMessage)
            }.first()

            handleSharedChat(shared)
        }
    }

    private suspend fun handleSharedChat(receivedMessage: ChatEventMessage<out ChatSharedRequest>) {
        val chatEvent = receivedMessage.chatEvent
        val chatIdentifier = chatEvent.chatId as ChatId

        behaviourContext.launch {
            val insertionState = receiversRepository.addReceiver(chatIdentifier.chatId)
            val chatTitle = chatIdentifier.getChatTitle(behaviourContext)
            val replyString = sharedReceiverHandler.getReplyString(insertionState, chatTitle, chatEvent)

            behaviourContext.reply(
                receivedMessage,
                replyString,
                replyMarkup = ReplyKeyboardRemove()
            )
        }
    }
}