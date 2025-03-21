package handlers.user

import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.DefaultBehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitAnyContentMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.*
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.extensions.utils.usersSharedOrNull
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.*
import domain.states.BotState
import handlers.ActionHandlers
import handlers.message_types.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UserActionHandlers @Inject constructor(
    private val commandController: UserCommandController,
    private val forwardController: ForwardController,
) : ActionHandlers {

    override suspend fun setupHandlers(behaviourContext: DefaultBehaviourContextWithFSM<BotState>) {
        behaviourContext.apply {

            onContentMessage(initialFilter = { message: CommonMessage<MessageContent> ->
                filterOutCommands(message.content)
            }) { message ->
                startChain(BotState.CommonMessageSent(
                    context = message.chat.id,
                    sourceMessage = message,
                ))
            }

            strictlyOn<BotState.CommonMessageSent> { state ->
                val message = state.sourceMessage
                val messageContent = message.content

                val forwardableMessageResult = runCatching {
                    when (messageContent) {
                        is VisualMediaGroupPartContent, is VideoNoteContent -> {
                            SingleMediaContentMessage(message)
                        }

                        is MediaGroupContent<*> -> {
                            message as CommonMessage<MediaGroupContent<VisualMediaGroupPartContent>>
                            VisualMediaGroupContentMessage(message)
                        }

                        is TextContent -> {
                            message as CommonMessage<MediaGroupContent<VisualMediaGroupPartContent>>
                            SimpleTextMessage(message)
                        }

                        else -> {
                            DefaultUnsupportedMessage(message)
                        }
                    }
                }

                forwardableMessageResult
                    .onSuccess {
                        forwardController.handleMessage(it)
                    }
                    .onFailure { throwable ->
                        forwardController.handleForwardingError(
                            message = ErrorMessage(message),
                            throwable = throwable,
                        )
                    }

                BotState.InitialState
            }

            onCommand("start", initialFilter = { it.chat is PrivateChat }) {
                commandController.handleStartCommand(it)
                commandController.handleHelpCommand(it)
            }

            onCommand("help") {
                commandController.handleHelpCommand(it)
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

            strictlyOn<BotState.InitialState> { null }

            strictlyOn<BotState.ExpectTextCall> { state ->
                val contentMessage = waitAnyContentMessage().filter { message ->
                    message.sameThread(state.sourceMessage)
                }.first()
                forwardController.handleTextCall(contentMessage)
            }

            strictlyOn<BotState.StopState> {
                send(it.context, replyMarkup = ReplyKeyboardRemove()) { +"Действие отменено" }

                BotState.InitialState
            }

            onUserLoggedIn {
                println("User ${it.chatEvent.usersSharedOrNull()} logged in")
            }

            onEditedPhoto {
                println(it.messageId)
            }
        }
    }
}