package handlers.admin

import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.requestGroupButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.requestUserButton
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardMarkup
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.chat.ExtendedUser
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.message.PrivateEventMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.request.ChatShared
import dev.inmo.tgbotapi.types.request.RequestId
import dev.inmo.tgbotapi.types.request.UserShared
import dev.inmo.tgbotapi.utils.RiskFeature
import dev.inmo.tgbotapi.utils.row
import domain.ReceiversRepository

class AdminActionsController(
    private val behaviourContext: BehaviourContext,
    private val receiversRepository: ReceiversRepository,
) {
    private suspend fun <T> withAdminCheck(block: suspend () -> T): T {
        // Do admin check
        // throw exception if it's not succeeded
        return block.invoke()
    }

    suspend fun showReceivers(message: CommonMessage<TextContent>) = withAdminCheck {
        behaviourContext.reply(
            message,
            receiversRepository.getReceiversList().toString(),
        )
    }

    suspend fun addReceiver(receivedMessage: CommonMessage<TextContent>) = withAdminCheck {
        val requestIdUserAny = RequestId(2)
        val requestIdGroup = RequestId(12)
        val keyboard: ReplyKeyboardMarkup = replyKeyboard(
            resizeKeyboard = true,
            oneTimeKeyboard = true,
        ) {
            row {
                requestUserButton("Добавить пользователя ", requestIdUserAny)
                requestGroupButton("Добавить группу", requestIdGroup)
            }
        }
        behaviourContext.reply(
            receivedMessage,
            "Here possible requests buttons:",
            replyMarkup = keyboard,
        )
    }

    @OptIn(RiskFeature::class)
    suspend fun handleSharedUser(receivedMessage: PrivateEventMessage<UserShared>) = withAdminCheck {
        val userId = receivedMessage.chatEvent.userId
        receiversRepository.addReceiver(userId.chatId)

        val username = runCatchingSafely {
            val user = behaviourContext.getChat(userId) as ExtendedUser
            "${user.firstName} ${user.lastName}".trim()
        }.getOrNull()

        behaviourContext.reply(
            receivedMessage,
            userReceiverReply(username),
            replyMarkup = ReplyKeyboardRemove()
        )
    }

    suspend fun handleSharedChat(receivedMessage: PrivateEventMessage<ChatShared>) = withAdminCheck {
        val chatId = receivedMessage.chatEvent.chatId
        receiversRepository.addReceiver(chatId.chatId)

        val chatTitle = runCatchingSafely {
            val chat = behaviourContext.getChat(chatId) as GroupChat
            chat.title
        }.getOrNull()

        behaviourContext.reply(
            receivedMessage,
            chatReceiverReply(chatTitle),
            replyMarkup = ReplyKeyboardRemove()
        )
    }

    private fun userReceiverReply(name: String?): String {
        name?.let {
            return "Получатель $it сохранён :)\nБот готов пересылать пользователю запросы"
        }
        return "Получатель сохранён :)\nПользователь ещё не активировал чат с ботом\nКак только он это сделает, бот сможет пересылать запросы"
    }

    private fun chatReceiverReply(title: String?): String {
        title?.let {
            return "Получатель $it сохранён :)\nБот готов пересылать запросы в этот чат"
        }
        return "Получатель сохранён :)\nБот ещё не добавлен в этот чат\nБот сможет пересылать запросы, как только будет добавлен в чат"
    }
}