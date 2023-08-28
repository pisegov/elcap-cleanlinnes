package handlers.admin

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitAnyContentMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import domain.AdminsRepository
import domain.states.DeletionState
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import states.BotState
import util.KeyboardBuilder
import util.ResourceProvider
import util.removedDoubleSpaces
import javax.inject.Inject

class AdminRemoveController @Inject constructor(
    private val behaviourContext: BehaviourContextWithFSM<BotState>,
    private val adminsRepository: AdminsRepository,
) {
    suspend fun showRemoveAdminKeyboard(receivedMessage: CommonMessage<TextContent>) {
        val adminsList = adminsRepository.getAdminsList()

        with(behaviourContext) {
            reply(
                receivedMessage,
                "Воспользуйтесь клавиатурой, выбрать администратора:",
                replyMarkup = KeyboardBuilder.adminsToRemoveKeyboard(adminsList),
            )

            startChain(BotState.ExpectSharedAdminToDelete(receivedMessage.chat.id, receivedMessage))
        }
    }

    suspend fun handleSharedAdminId(state: BotState.ExpectSharedAdminToDelete): BotState {
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
                        val deletionState = adminsRepository.removeAdmin(id)
                        when (deletionState) {
                            is DeletionState.Success -> {
                                BotState.CorrectInputSharedAdminToDelete(
                                    state.context,
                                    deletedAdminFullName = content.text.substringBefore("[").trimEnd()
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
                BotState.WrongInputSharedAdminToDelete(
                    state.context,
                    sourceMessage = state.sourceMessage,
                    e.message.toString()
                )
            } catch (e: Exception) {
                BotState.WrongInputSharedAdminToDelete(
                    state.context,
                    sourceMessage = state.sourceMessage,
                )
            }

        }
    }

    suspend fun handleCorrectInput(state: BotState.CorrectInputSharedAdminToDelete): BotState? {
        with(behaviourContext) {
            send(
                state.context,
                replyMarkup = ReplyKeyboardRemove()
            ) { +"Администратор ${state.deletedAdminFullName} удалён".removedDoubleSpaces() }
            // Return initial state
            return null
        }
    }

    suspend fun handleWrongInput(state: BotState.WrongInputSharedAdminToDelete): BotState {
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
            return BotState.ExpectSharedAdminToDelete(state.context, state.sourceMessage)
        }
    }

    private fun parseTelegramChatId(string: String): Long? {
        return string.substringAfter("[id:").substringBefore("]").toLongOrNull()
    }
}