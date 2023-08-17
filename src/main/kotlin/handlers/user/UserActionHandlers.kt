package handlers.user

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.*
import dev.inmo.tgbotapi.extensions.utils.userSharedOrNull
import dev.inmo.tgbotapi.types.chat.PrivateChat
import handlers.ActionHandlers
import javax.inject.Inject

class UserActionHandlers @Inject constructor(
    private val behaviourContext: BehaviourContext,
    private val actionsController: UserActionsController,
) : ActionHandlers {
    override suspend fun setupHandlers() {
        behaviourContext.apply {
            onPhoto { message ->
                actionsController.handleCallWithPhoto(message)
            }

            onCommand("start", initialFilter = { it.chat is PrivateChat }) {
                actionsController.handleStartCommand(it)
            }

            onCommand("help") {
                send(it.chat, "Help message")
            }
//        onText {
//            send(it.chat, "Сорри, я не пересылаю обычный текст, введите команду /call")
//        }

            onSticker {
                reply(it, "Спасибо за стикер)")
            }

            onUserLoggedIn {
                println("User ${it.chatEvent.userSharedOrNull()} logged in")
            }

            onContentMessage {
                println(it.chat)
            }

            onEditedPhoto {
                println(it.messageId)
            }
        }
    }
}