package handlers.user

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.DefaultBehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.*
import dev.inmo.tgbotapi.extensions.utils.usersSharedOrNull
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.chat.PrivateChat
import domain.states.BotState
import handlers.ActionHandlers
import javax.inject.Inject

class UserActionHandlers @Inject constructor(
    private val behaviourContext: DefaultBehaviourContextWithFSM<BotState>,
    private val actionsController: UserActionsController,
) : ActionHandlers {
    override suspend fun setupHandlers() {
        behaviourContext.apply {
            onPhoto { message ->
                actionsController.forwardCallToReceivers(message)
            }

            onCommand("start", initialFilter = { it.chat is PrivateChat }) {
                actionsController.handleStartCommand(it)
                actionsController.sendHelpMessage(it)
            }

            onCommand("help") {
                actionsController.sendHelpMessage(it)
            }

            onCommand("call") { message ->
                send(
                    message.chat, """
                    Следующее сообщение мы перешлём нашим сотрудникам
                    Введите обращение целиком
                    
                    Если хотите отменить это действие, введите команду /cancel
                """.trimIndent()
                )
                startChain(BotState.ExpectTextCall(message.chat.id, message))
            }
//        onText {
//            send(it.chat, "Сорри, я не пересылаю обычный текст, введите команду /call")
//        }

            strictlyOn<BotState.InitialState> { null }

            strictlyOn<BotState.ExpectTextCall> {
                actionsController.handleTextCall(it)
            }
            strictlyOn<BotState.StopState> {
                send(it.context, replyMarkup = ReplyKeyboardRemove()) { +"Действие отменено" }

                BotState.InitialState
            }

            onSticker {
                reply(it, "Спасибо за стикер)")
            }

            onUserLoggedIn {
                println("User ${it.chatEvent.usersSharedOrNull()} logged in")
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