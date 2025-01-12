package handlers.admin

import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import domain.states.BotState
import javax.inject.Inject

class AdminActionsController @Inject constructor(
    private val behaviourContext: BehaviourContextWithFSM<BotState>,
) {
    suspend fun handlePermissionDeniedState(state: BotState.PermissionsDeniedState): BotState {
        with(behaviourContext) {
            send(
                state.context,
                replyMarkup = ReplyKeyboardRemove()
            ) { +"Oops, у вас нет прав для выполнения этой команды" }

            return BotState.InitialState
        }
    }

    suspend fun sendAdminHelpMessage(receivedMessage: CommonMessage<MessageContent>) {
        with(behaviourContext) {
            send(
                receivedMessage.chat.id,
                """
                    Команды администратора:
                    
                    /admin — вывести это сообщение
                    
                    /add_admin — добавить администратора
                    /show_admins — показать список администраторов
                    /remove_admin — удалить администратора
                    
                    /add_receiver — добавить получателя
                    /show_receivers — показать всех получателей
                    /remove_receiver — удалить получателя
               """.trimIndent()
            )
        }
    }
}