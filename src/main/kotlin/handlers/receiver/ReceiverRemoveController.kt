package handlers.receiver

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitAnyContentMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import domain.ReceiversRepository
import domain.states.DeletionState
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import states.BotState
import util.KeyboardBuilder
import util.ResourceProvider
import util.removedDoubleSpaces
import javax.inject.Inject
import handlers.admin.WrongInputException

class ReceiverRemoveController @Inject constructor(
    private val behaviourContext: BehaviourContextWithFSM<BotState>,
    private val receiversRepository: ReceiversRepository,
) {
    suspend fun showRemoveReceiverKeyboard(receivedMessage: CommonMessage<TextContent>) {
        val receiversList = receiversRepository.getReceiversList()

        with(behaviourContext) {
            reply(
                receivedMessage,
                "Воспользуйтесь клавиатурой, выбрать получателя:",
                replyMarkup = KeyboardBuilder.chatsToRemoveKeyboard(receiversList),
            )

            startChain(BotState.ExpectSharedReceiverToDelete(receivedMessage.chat.id, receivedMessage))
        }
    }

    suspend fun handleSharedReceiverId(state: BotState.ExpectSharedReceiverToDelete): BotState {
        with(behaviourContext) {
            val contentMessage = waitAnyContentMessage().filter { message ->
                message.sameThread(state.sourceMessage)
            }.first()

            return try {
                // throws cast exception
                val content = contentMessage.content as TextContent

                val id: Long? = parseTelegramChatId(content.text)
                when {
                    // Handle cancellation
                    content.text == ResourceProvider.CANCEL_STRING -> {
                        BotState.StopState(state.context)
                    }

                    id != null -> {
                        val deletionState = receiversRepository.removeReceiver(id)
                        when (deletionState) {
                            is DeletionState.Success -> {
                                BotState.CorrectInputSharedReceiverToDelete(
                                    state.context,
                                    deletedReceiverChatTitle = content.text.substringBefore("[").trimEnd()
                                )
                            }

                            is DeletionState.Error -> {
                                throw WrongInputException("Нет такого id")
                            }
                        }
                    }

                    else -> {
                        throw WrongInputException("В сообщении нет подходящего id")
                    }
                }
            } catch (e: WrongInputException) {
                // Handle wrong input
                BotState.WrongInputSharedReceiverToDelete(
                    state.context,
                    sourceMessage = state.sourceMessage,
                    e.message.toString()
                )
            } catch (e: Exception) {
                BotState.WrongInputSharedReceiverToDelete(
                    state.context,
                    sourceMessage = state.sourceMessage,
                )
            }
        }
    }

    suspend fun handleCorrectInput(state: BotState.CorrectInputSharedReceiverToDelete): BotState? {
        with(behaviourContext) {
            send(
                state.context,
                replyMarkup = ReplyKeyboardRemove()
            ) { +"Получатель ${state.deletedReceiverChatTitle} удалён".removedDoubleSpaces() }
            // Return initial state
            return null
        }
    }

    suspend fun handleWrongInput(state: BotState.WrongInputSharedReceiverToDelete): BotState {
        with(behaviourContext) {
            send(state.context) {
                +"""
                        Неверный ввод
                        ${state.cause}
                        
                        Воспользуйтесь выпадающей клавиатурой
                        Или отмените действие командой /cancel
                    """.trimIndent()
                    // If it's a cast exception, it removes the blank string
                    .replace("\n\n\n".toRegex(), "\n\n")
            }
            // Return expecting state
            return BotState.ExpectSharedReceiverToDelete(state.context, state.sourceMessage)
        }
    }

    private fun parseTelegramChatId(string: String): Long? {
        return string.substringAfter("[id:").substringBefore("]").toLongOrNull()
    }
}