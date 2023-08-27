package handlers.admin

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitAnyContentMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitUserSharedEventsMessages
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.message.abstracts.ChatEventMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.request.UserShared
import domain.AdminsRepository
import domain.model.Admin
import domain.states.InsertionState
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import states.BotState
import util.KeyboardBuilder
import util.ResourceProvider
import util.getChatTitle
import javax.inject.Inject

class AdminActionsController @Inject constructor(
    private val behaviourContext: BehaviourContextWithFSM<BotState>,
    private val adminsRepository: AdminsRepository,
    private val sharedAdminHandler: SharedAdminHandler,
    private val permissionsChecker: PermissionsChecker,
) {
    suspend fun addAdmin(receivedMessage: CommonMessage<TextContent>) = withAdminCheck(receivedMessage.chat.id) {
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

    suspend fun showAllAdmins(message: CommonMessage<TextContent>) = withAdminCheck(message.chat.id) {
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

    suspend fun showRemoveAdminKeyboard(receivedMessage: CommonMessage<TextContent>) =
        withAdminCheck(receivedMessage.chat.id) {
            val adminsList = adminsRepository.getAdminsList()

            with(behaviourContext) {
                reply(
                    receivedMessage,
                    "Воспользуйтесь клавиатурой, выбрать администратора:",
                    replyMarkup = KeyboardBuilder.adminsToRemoveKeyboard(adminsList),
                )

                startChain(BotState.ExpectSharedAdminToDelete(receivedMessage.chat.id, receivedMessage))
            }
        }

    suspend fun handleSharedAdminIdToDelete(state: BotState.ExpectSharedAdminToDelete): BotState {
        with(behaviourContext) {
            val contentMessage = waitAnyContentMessage().filter { message ->
                message.sameThread(state.sourceMessage)
            }.first()

            return try {
                // throws cast exception
                val content = contentMessage.content as TextContent

                val id: Long? = parseTelegramChatId(content.text)
                when {
                    // Handle cancellation
                    content.text == ResourceProvider.CANCEL_STRING -> {
                        BotState.StopState(state.context)
                    }

                    id != null -> {
                        adminsRepository.removeAdmin(id)
                        BotState.CorrectInputSharedAdminToDelete(
                            state.context,
                            deletedAdminFullName = content.text.substringBefore("[").trimEnd()
                        )
                    }

                    else -> {
                        throw Exception("Message has no valid chat id")
                    }
                }
            } catch (e: Exception) {
                // Handle wrong input
                BotState.WrongInputSharedAdminToDelete(state.context, sourceMessage = state.sourceMessage)
            }
        }
    }

    private suspend fun <T> withAdminCheck(chatIdentifier: IdChatIdentifier, block: suspend () -> T) {
        try {
            permissionsChecker.checkPermissions(chatIdentifier.chatId, block)
        } catch (e: Exception) {
            behaviourContext.startChain(BotState.PermissionsDeniedState(chatIdentifier))
        }
    }

    private fun parseTelegramChatId(string: String): Long? {
        return string.substringAfter("[id:").substringBefore("]").toLongOrNull()
    }

    private fun handleSharedAdmin(eventMessage: ChatEventMessage<UserShared>) {
        val chatIdentifier: UserId = eventMessage.chatEvent.userId
        val userId: Long = chatIdentifier.chatId
        behaviourContext.launch {
            val insertionState: InsertionState = adminsRepository.addAdmin(userId)
            val chatTitle: String = chatIdentifier.getChatTitle(behaviourContext)
            val replyString: String = sharedAdminHandler.getReplyString(insertionState, chatTitle)

            behaviourContext.reply(
                eventMessage,
                replyString,
                replyMarkup = ReplyKeyboardRemove()
            )
        }
    }
}