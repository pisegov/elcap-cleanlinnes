package handlers.admin

import dev.inmo.tgbotapi.extensions.behaviour_builder.DefaultBehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.chat.PrivateChat
import handlers.ActionHandlers
import handlers.receiver.ReceiverActionsController
import states.BotState
import states.BotState.*
import javax.inject.Inject

class AdminActionHandlers @Inject constructor(
    private val behaviourContext: DefaultBehaviourContextWithFSM<BotState>,
    private val addingAdminController: AdminAddController,
    private val removingAdminController: AdminRemoveController,
    private val shortAdminActionsController: AdminShortActionsController,
    private val receiverActionsController: ReceiverActionsController,
    private val permissionsChecker: PermissionsChecker,
) : ActionHandlers {
    override suspend fun setupHandlers() {
        with(behaviourContext) {
            onCommand("add_admin", initialFilter = { it.chat is PrivateChat }) { message ->
                withAdminCheck(message.chat.id) {
                    addingAdminController.addAdmin(message)
                }
            }

            onCommand("show_admins", initialFilter = { it.chat is PrivateChat }) { message ->
                withAdminCheck(message.chat.id) {
                    shortAdminActionsController.showAllAdmins(message)
                }
            }

            onCommand("remove_admin", initialFilter = { it.chat is PrivateChat }) { message ->
                withAdminCheck(message.chat.id) {
                    removingAdminController.showRemoveAdminKeyboard(message)
                }
            }

            onCommand("show_receivers", initialFilter = { it.chat is PrivateChat }) { message ->
                withAdminCheck(message.chat.id) {
                    receiverActionsController.showReceivers(message)
                }
            }

            onCommand("add_receiver", initialFilter = { it.chat is PrivateChat }) { message ->
                withAdminCheck(message.chat.id) {
                    receiverActionsController.addReceiver(message)
                }
            }

            strictlyOn<ExpectSharedAdminToDelete> {
                removingAdminController.handleSharedAdminId(it)
            }

            strictlyOn<CorrectInputSharedAdminToDelete> {
                removingAdminController.handleCorrectInput(it)
            }

            strictlyOn<WrongInputSharedAdminToDelete> {
                removingAdminController.handleWrongInput(it)
            }

            strictlyOn<PermissionsDeniedState> {
                shortAdminActionsController.handlePermissionDeniedState(it)
            }
        }
    }

    private suspend fun <T> withAdminCheck(chatIdentifier: IdChatIdentifier, block: suspend () -> T) {
        try {
            permissionsChecker.checkPermissions(chatIdentifier.chatId, block)
        } catch (e: Exception) {
            behaviourContext.startChain(PermissionsDeniedState(chatIdentifier))
        }
    }
}

