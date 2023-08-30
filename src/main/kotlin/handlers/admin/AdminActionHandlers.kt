package handlers.admin

import dev.inmo.tgbotapi.extensions.behaviour_builder.DefaultBehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.chat.PrivateChat
import domain.AdminManagedType
import handlers.ActionHandlers
import handlers.chat.ChatAddController
import handlers.chat.ChatRemoveController
import handlers.chat.ChatShowController
import states.BotState
import states.BotState.*
import javax.inject.Inject

class AdminActionHandlers @Inject constructor(
    private val behaviourContext: DefaultBehaviourContextWithFSM<BotState>,
    private val adminActionsController: AdminActionsController,
    private val chatsAddController: ChatAddController,
    private val chatsShowController: ChatShowController,
    private val chatsRemoveController: ChatRemoveController,
    private val permissionsChecker: PermissionsChecker,
) : ActionHandlers {
    override suspend fun setupHandlers() {
        with(behaviourContext) {
            onCommand("add_admin", initialFilter = { it.chat is PrivateChat }) { message ->
                withAdminCheck(message.chat.id) {
                    chatsAddController.addChat(message, chatType = AdminManagedType.Admin)
                }
            }

            onCommand("show_admins", initialFilter = { it.chat is PrivateChat }) { message ->
                withAdminCheck(message.chat.id) {
                    chatsShowController.showChatsList(message, chatType = AdminManagedType.Admin)
                }
            }

            onCommand("remove_admin", initialFilter = { it.chat is PrivateChat }) { message ->
                withAdminCheck(message.chat.id) {
                    chatsRemoveController.showRemoveChatKeyboard(message, chatType = AdminManagedType.Admin)
                }
            }

            onCommand("show_receivers", initialFilter = { it.chat is PrivateChat }) { message ->
                withAdminCheck(message.chat.id) {
                    chatsShowController.showChatsList(message, chatType = AdminManagedType.Receiver)
                }
            }

            onCommand("add_receiver", initialFilter = { it.chat is PrivateChat }) { message ->
                withAdminCheck(message.chat.id) {
                    chatsAddController.addChat(message, chatType = AdminManagedType.Receiver)
                }
            }
            onCommand("remove_receiver", initialFilter = { it.chat is PrivateChat }) { message ->
                withAdminCheck(message.chat.id) {
                    chatsRemoveController.showRemoveChatKeyboard(
                        message,
                        chatType = AdminManagedType.Receiver
                    )
                }
            }

            strictlyOn<ExpectSharedChatToDelete> {
                chatsRemoveController.handleSharedChatId(it)
            }

            strictlyOn<CorrectInputSharedChatToDelete> {
                chatsRemoveController.handleCorrectInput(it)
            }

            strictlyOn<WrongInputSharedChatToDelete> {
                chatsRemoveController.handleWrongInput(it)
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

