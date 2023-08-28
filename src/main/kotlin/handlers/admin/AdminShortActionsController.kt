package handlers.admin

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import domain.AdminsRepository
import domain.model.Admin
import states.BotState
import javax.inject.Inject

class AdminShortActionsController @Inject constructor(
    private val behaviourContext: BehaviourContextWithFSM<BotState>,
    private val adminsRepository: AdminsRepository,
) {
    suspend fun showAllAdmins(message: CommonMessage<TextContent>) {
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

    suspend fun handlePermissionDeniedState(state: BotState.PermissionsDeniedState): BotState? {
        with(behaviourContext) {
            send(
                state.context,
                replyMarkup = ReplyKeyboardRemove()
            ) { +"Oops, у вас нет прав для выполнения этой команды" }

            // Return initial state
            return null
        }
    }
}