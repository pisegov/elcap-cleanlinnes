package handlers.chat

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitAnyContentMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import domain.AdminManagedRepositoriesProvider
import domain.AdminManagedType
import domain.states.BotState
import domain.states.DeletionState
import handlers.admin.WrongInputException
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import util.KeyboardBuilder
import util.ResourceProvider
import util.removedDoubleSpaces
import javax.inject.Inject

class ChatRemoveController @Inject constructor(
    private val behaviourContext: BehaviourContextWithFSM<BotState>,
    private val chatsRepositoryProvider: AdminManagedRepositoriesProvider,
) {
    suspend fun showRemoveChatKeyboard(receivedMessage: CommonMessage<TextContent>, chatType: AdminManagedType) {
        val chatsRepository = chatsRepositoryProvider.provide(chatType)
        val chatsList = chatsRepository.getChatsList()

        with(behaviourContext) {
            reply(
                receivedMessage,
                ResourceProvider.useKeyboardToSelectChat(chatType),
                replyMarkup = KeyboardBuilder.chatsToRemoveKeyboard(chatsList),
            )

            startChain(BotState.ExpectSharedChatToDelete(receivedMessage.chat.id, receivedMessage, chatType))
        }
    }

    suspend fun handleSharedChatId(state: BotState.ExpectSharedChatToDelete): BotState {
        val chatsRepository = chatsRepositoryProvider.provide(state.chatType)
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
                        val deletionState = chatsRepository.removeChat(id)
                        when (deletionState) {
                            is DeletionState.Success -> {
                                BotState.CorrectInputSharedChatToDelete(
                                    state.context,
                                    deletedChatTitle = content.text.substringBefore("[").trimEnd(),
                                    chatType = state.chatType
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
                BotState.WrongInputSharedChatToDelete(
                    state.context,
                    sourceMessage = state.sourceMessage,
                    chatType = state.chatType,
                    e.message.toString()

                )
            } catch (e: Exception) {
                BotState.WrongInputSharedChatToDelete(
                    state.context,
                    sourceMessage = state.sourceMessage,
                    chatType = state.chatType,
                )
            }
        }
    }

    suspend fun handleCorrectInput(state: BotState.CorrectInputSharedChatToDelete): BotState? {
        with(behaviourContext) {
            send(
                state.context,
                replyMarkup = ReplyKeyboardRemove()
            ) { +"${ResourceProvider.chatTypeTitle(state.chatType)} ${state.deletedChatTitle} удалён".removedDoubleSpaces() }
            // Return initial state
            return null
        }
    }

    suspend fun handleWrongInput(state: BotState.WrongInputSharedChatToDelete): BotState {
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
            return BotState.ExpectSharedChatToDelete(state.context, state.sourceMessage, state.chatType)
        }
    }

    private fun parseTelegramChatId(string: String): Long? {
        return string.substringAfter("[id:").substringBefore("]").toLongOrNull()
    }
}