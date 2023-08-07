package handlers.user

import dev.inmo.tgbotapi.extensions.api.forwardMessage
import dev.inmo.tgbotapi.extensions.api.send.withTypingAction
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.shortcuts.executeUnsafe
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.PossiblyForwardedMessage
import dev.inmo.tgbotapi.types.message.content.PhotoContent
import dev.inmo.tgbotapi.utils.extensions.threadIdOrNull
import domain.ReceiversRepository

class UserAcitionsController(
    private val behaviourContext: BehaviourContext,
    private val receiversRepository: ReceiversRepository,
) {
    suspend fun handleCallWithPhoto(receivedMessage: CommonMessage<PhotoContent>) {
        println(receivedMessage.messageId)

        val receivers = receiversRepository.getReceiversList()
        receivers.forEach { chat ->
            behaviourContext.withTypingAction(receivedMessage.chat) {
                if (receivedMessage.forwardable) {
                    val message: PossiblyForwardedMessage = receivedMessage
                    message.apply {
                        val new = forwardMessage(
                            ChatId(chat.telegramChatId),
                            receivedMessage,
                            threadId = receivedMessage.threadIdOrNull
                        )
                        println(new)
                    }
                } else {
                    executeUnsafe(
                        receivedMessage.content.createResend(
                            ChatId(chat.telegramChatId),
                            messageThreadId = receivedMessage.threadIdOrNull,
                            allowSendingWithoutReply = false
                        )
                    ) { errorsList ->
                        errorsList.forEach { println(it) }
                    }
                }
            }
        }
    }

}