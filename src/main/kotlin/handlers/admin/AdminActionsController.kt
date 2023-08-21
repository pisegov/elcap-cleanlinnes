package handlers.admin

import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitChatSharedRequestEventsMessages
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitUserSharedEventsMessages
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.requestGroupButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.requestUserButton
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardMarkup
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.chat.ExtendedPrivateChat
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.message.abstracts.ChatEventMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.request.ChatSharedRequest
import dev.inmo.tgbotapi.types.request.RequestId
import dev.inmo.tgbotapi.types.request.UserShared
import dev.inmo.tgbotapi.utils.row
import domain.AdminsRepository
import domain.ReceiversRepository
import domain.states.InsertionState
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class AdminActionsController @Inject constructor(
    private val behaviourContext: BehaviourContext,
    private val receiversRepository: ReceiversRepository,
    private val adminsRepository: AdminsRepository,
    private val sharedAdminHandler: SharedAdminHandler,
    private val sharedReceiverHandler: SharedReceiverHandler,
) {
    private val shareChatKeyboard: ReplyKeyboardMarkup by lazy {
        val requestIdUserAny = RequestId(2)
        val requestIdGroup = RequestId(12)
        replyKeyboard(
            resizeKeyboard = true,
            oneTimeKeyboard = true,
        ) {
            row {
                requestUserButton("Добавить пользователя ", requestIdUserAny)
                requestGroupButton("Добавить группу", requestIdGroup)
            }
        }
    }
    private val shareAdminKeyboard: ReplyKeyboardMarkup by lazy {
        val requestIdUserAny = RequestId(2)
        replyKeyboard(
            resizeKeyboard = true,
            oneTimeKeyboard = true,
        ) {
            row {
                requestUserButton("Добавить пользователя ", requestIdUserAny)
            }
        }
    }

    suspend fun showReceivers(message: CommonMessage<TextContent>) = withAdminCheck {
        behaviourContext.reply(
            message,
            receiversRepository.getReceiversList().toString(),
        )
    }

    suspend fun addReceiver(receivedMessage: CommonMessage<TextContent>) = withAdminCheck {
        with(behaviourContext) {
            reply(
                receivedMessage,
                "Here possible requests buttons:",
                replyMarkup = shareChatKeyboard,
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
                replyMarkup = shareAdminKeyboard,
            )

            val shared = waitUserSharedEventsMessages().filter { message ->
                message.sameThread(receivedMessage)
            }.first()

            handleSharedAdmin(shared)
        }
    }

    suspend fun showAllAdmins(message: CommonMessage<TextContent>) = withAdminCheck {
        behaviourContext.reply(
            message,
            adminsRepository.getAdminsList().toString(),
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