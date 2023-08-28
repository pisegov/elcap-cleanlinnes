package handlers.user

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.DefaultBehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.*
import dev.inmo.tgbotapi.extensions.utils.userSharedOrNull
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.chat.PrivateChat
import handlers.ActionHandlers
import states.BotState
import javax.inject.Inject

class UserActionHandlers @Inject constructor(
    private val behaviourContext: DefaultBehaviourContextWithFSM<BotState>,
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

            onCommand("cancel", initialFilter = { it.chat is PrivateChat }) { message ->
                BotState.StopState(message.chat.id)
            }

            strictlyOn<BotState.StopState> {
                send(it.context, replyMarkup = ReplyKeyboardRemove()) { +"Действие отменено" }

                // Return initial state
                null
            }

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