package handlers.admin

import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitChatSharedRequestEventsMessages
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitUserSharedEventsMessages
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.chat.ExtendedPrivateChat
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.message.abstracts.ChatEventMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.request.ChatSharedRequest
import dev.inmo.tgbotapi.types.request.UserShared
import domain.AdminsRepository
import domain.ReceiversRepository
import domain.model.Admin
import domain.model.Receiver
import domain.states.InsertionState
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import util.KeyboardBuilder
import javax.inject.Inject

class AdminActionsController @Inject constructor(
    private val behaviourContext: BehaviourContext,
    private val receiversRepository: ReceiversRepository,
    private val adminsRepository: AdminsRepository,
    private val sharedAdminHandler: SharedAdminHandler,
    private val sharedReceiverHandler: SharedReceiverHandler,
) {
    suspend fun showReceivers(message: CommonMessage<TextContent>) = withAdminCheck {
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

    suspend fun addReceiver(receivedMessage: CommonMessage<TextContent>) = withAdminCheck {
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

    suspend fun addAdmin(receivedMessage: CommonMessage<TextContent>) = withAdminCheck {
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

    suspend fun showAllAdmins(message: CommonMessage<TextContent>) = withAdminCheck {
        val replyString = StringBuilder()

        val adminsList = adminsRepository.getAdminsList()
        val activeAdmins = adminsList.filter { it.fullName.isNotEmpty() }
        val notActiveAdmins = adminsList.filter { it.fullName.isEmpty() }

        if (activeAdmins.isNotEmpty()) {
            replyString.append("Активные администраторы:\n\n")
            activeAdmins.forEach { admin: Admin ->
                replyString.append("${admin.fullName}\nTelegram chat id: ${admin.telegramChatId}\n\n")
            }
        }
        if (notActiveAdmins.isNotEmpty()) {
            replyString.append("\nНеактивные администраторы:\n\n")
            notActiveAdmins.forEach { admin: Admin ->
                replyString.append("Telegram chat id: ${admin.telegramChatId}\n")
            }
        }
        behaviourContext.reply(
            message,
            replyString.toString(),
        )
    }

    private suspend fun <T> withAdminCheck(block: suspend () -> T): T {
        // Do admin check
        // throw exception if it's not succeeded
        return block.invoke()
    }

    private suspend fun handleSharedChat(receivedMessage: ChatEventMessage<out ChatSharedRequest>) {
        val chatEvent = receivedMessage.chatEvent
        val chatIdentifier = chatEvent.chatId as ChatId

        behaviourContext.launch {
            val insertionState = receiversRepository.addReceiver(chatIdentifier.chatId)
            val chatTitle = getChatTitle(chatIdentifier)
            val replyString = sharedReceiverHandler.getReplyString(insertionState, chatTitle, chatEvent)

            behaviourContext.reply(
                receivedMessage,
                replyString,
                replyMarkup = ReplyKeyboardRemove()
            )
        }
    }

    private fun handleSharedAdmin(eventMessage: ChatEventMessage<UserShared>) {
        val chatIdentifier: UserId = eventMessage.chatEvent.userId
        val userId: Long = chatIdentifier.chatId
        behaviourContext.launch {
            val insertionState: InsertionState = adminsRepository.addAdmin(userId)
            val chatTitle: String = getChatTitle(chatIdentifier)
            val replyString: String = sharedAdminHandler.getReplyString(insertionState, chatTitle)

            behaviourContext.reply(
                eventMessage,
                replyString,
                replyMarkup = ReplyKeyboardRemove()
            )
        }
    }

    private suspend fun getChatTitle(chatIdentifier: ChatId): String {
        return runCatchingSafely {
            val chat = behaviourContext.getChat(chatIdentifier)
            when (chat) {
                is ExtendedPrivateChat -> {
                    "${chat.firstName} ${chat.lastName}".trim()
                }

                is GroupChat -> {
                    chat.title
                }

                else -> {
                    ""
                }
            }
        }.getOrDefault("")
    }
}