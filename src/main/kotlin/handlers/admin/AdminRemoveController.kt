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
                        adminsRepository.removeAdmin(id)
                        BotState.CorrectInputSharedAdminToDelete(
                            state.context,
                            deletedAdminFullName = content.text.substringBefore("[").trimEnd()
                        )
                    }

                    else -> {
                        throw Exception("Message has no valid chat id")
                    }
                }
            } catch (e: Exception) {
                // Handle wrong input
                BotState.WrongInputSharedAdminToDelete(state.context, sourceMessage = state.sourceMessage)
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
                        Администратор не удалён
                        Воспользуйтесь выпадающей клавиатурой
                        Или отмените действие командой /cancel
                    """.trimIndent()
            }
            // Return expecting state
            return BotState.ExpectSharedAdminToDelete(state.context, state.sourceMessage)
        }
    }

    private fun parseTelegramChatId(string: String): Long? {
        return string.substringAfter("[id:").substringBefore("]").toLongOrNull()
    }
}