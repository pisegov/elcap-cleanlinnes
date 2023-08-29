package handlers.admin

import dev.inmo.tgbotapi.extensions.behaviour_builder.DefaultBehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.chat.PrivateChat
import domain.AdminManagedType
import handlers.ActionHandlers
import handlers.chat.ChatRemoveController
import handlers.chat.ChatShowController
import handlers.receiver.ReceiverActionsController
import states.BotState
import states.BotState.*
import javax.inject.Inject

class AdminActionHandlers @Inject constructor(
    private val behaviourContext: DefaultBehaviourContextWithFSM<BotState>,
    private val addingAdminController: AdminAddController,
    private val adminActionsController: AdminActionsController,
    private val receiverActionsController: ReceiverActionsController,
    private val adminManagedChatsRemoveController: ChatRemoveController,
    private val adminManagedChatsShowController: ChatShowController,
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
                    adminManagedChatsShowController.showChatsList(message, chatType = AdminManagedType.Admin)
                }
            }

            onCommand("remove_admin", initialFilter = { it.chat is PrivateChat }) { message ->
                withAdminCheck(message.chat.id) {
                    adminManagedChatsRemoveController.showRemoveChatKeyboard(message, chatType = AdminManagedType.Admin)
                }
            }

            onCommand("show_receivers", initialFilter = { it.chat is PrivateChat }) { message ->
                withAdminCheck(message.chat.id) {
                    adminManagedChatsShowController.showChatsList(message, chatType = AdminManagedType.Receiver)
                }
            }

            onCommand("add_receiver", initialFilter = { it.chat is PrivateChat }) { message ->
                withAdminCheck(message.chat.id) {
                    receiverActionsController.addReceiver(message)
                }
            }
            onCommand("remove_receiver", initialFilter = { it.chat is PrivateChat }) { message ->
                withAdminCheck(message.chat.id) {
                    adminManagedChatsRemoveController.showRemoveChatKeyboard(
                        message,
                        chatType = AdminManagedType.Receiver
                    )
                }
            }

            strictlyOn<ExpectSharedChatToDelete> {
                adminManagedChatsRemoveController.handleSharedChatId(it)
            }

            strictlyOn<CorrectInputSharedChatToDelete> {
                adminManagedChatsRemoveController.handleCorrectInput(it)
            }

            strictlyOn<WrongInputSharedChatToDelete> {
                adminManagedChatsRemoveController.handleWrongInput(it)
            }

            strictlyOn<PermissionsDeniedState> {
                adminActionsController.handlePermissionDeniedState(it)
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

