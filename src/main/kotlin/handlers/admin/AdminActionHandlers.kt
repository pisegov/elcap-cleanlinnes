package handlers.admin

import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.DefaultBehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.chat.PrivateChat
import handlers.ActionHandlers
import states.BotState
import states.BotState.*
import util.removedDoubleSpaces
import javax.inject.Inject

class AdminActionHandlers @Inject constructor(
    private val behaviourContext: DefaultBehaviourContextWithFSM<BotState>,
    private val actionsController: AdminActionsController,
) : ActionHandlers {
    override suspend fun setupHandlers() {
        with(behaviourContext) {
            onCommand("show_receivers", initialFilter = { it.chat is PrivateChat }) { message ->
                actionsController.showReceivers(message)
            }

            onCommand("add_receiver", initialFilter = { it.chat is PrivateChat }) { message ->
                actionsController.addReceiver(message)
            }

            onCommand("add_admin", initialFilter = { it.chat is PrivateChat }) { message ->
                actionsController.addAdmin(message)
            }

            onCommand("show_admins", initialFilter = { it.chat is PrivateChat }) { message ->
                actionsController.showAllAdmins(message)
            }

            onCommand("cancel", initialFilter = { it.chat is PrivateChat }) { message ->
                StopState(message.chat.id)
            }

            onCommand("remove_admin", initialFilter = { it.chat is PrivateChat }) { message ->
                actionsController.showRemoveAdminKeyboard(message)
            }


            strictlyOn<ExpectSharedAdminToDelete> {
                actionsController.handleSharedAdminToDelete(it)
            }

            strictlyOn<CorrectInputSharedAdminToDelete> {
                send(
                    it.context,
                    replyMarkup = ReplyKeyboardRemove()
                ) { +"Администратор ${it.deletedAdminFullName} удалён".removedDoubleSpaces() }
                // Return initial state
                null
            }

            strictlyOn<WrongInputSharedAdminToDelete> {
                send(it.context) {
                    +"""
                        Неверный ввод
                        Администратор не удалён
                        Воспользуйтесь выпадающей клавиатурой
                        Или отмените действие командой /cancel
                    """.trimIndent()
                }
                // Return expecting state
                ExpectSharedAdminToDelete(it.context, it.sourceMessage)
            }

            strictlyOn<StopState> {
                send(it.context, replyMarkup = ReplyKeyboardRemove()) { +"Действие отменено" }

                // Return initial state
                null
            }
        }
    }
}

