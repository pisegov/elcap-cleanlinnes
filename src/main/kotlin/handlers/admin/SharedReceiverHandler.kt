package handlers.admin

import dev.inmo.tgbotapi.types.request.ChatShared
import dev.inmo.tgbotapi.types.request.ChatSharedRequest
import dev.inmo.tgbotapi.types.request.UserShared
import domain.states.InsertionState
import util.removedDoubleSpaces
import javax.inject.Inject

class SharedReceiverHandler @Inject constructor() {
    fun getReplyString(insertionState: InsertionState, chatTitle: String, chatEvent: ChatSharedRequest): String {
        val replyString = StringBuilder()
        when (insertionState) {
            is InsertionState.Success -> {
                replyString.append("Получатель $chatTitle сохранён :)\n")
                replyString.append(receiverReply(chatEvent, chatTitle.isNotEmpty()))
            }

            is InsertionState.Duplicate -> {
                replyString.append("Получатель $chatTitle уже добавлен в систему\n")
                replyString.append(receiverReply(chatEvent, chatTitle.isNotEmpty()))
            }

            is InsertionState.Error -> {
                replyString.append("Что-то пошло не так, бот не смог добавить получателя в систему :(")
            }
        }

        return replyString.removedDoubleSpaces()
    }

    private fun receiverReply(chatEvent: ChatSharedRequest, chatIsActivated: Boolean): String {
        return when (chatEvent) {
            is UserShared -> {
                when {
                    chatIsActivated -> {
                        "Бот готов пересылать пользователю запросы"
                    }

                    else -> {
                        "Пользователь ещё не активировал чат с ботом\nКак только он это сделает, бот сможет пересылать запросы"
                    }
                }
            }

            is ChatShared -> {
                when {
                    chatIsActivated -> {
                        "Бот готов пересылать запросы в этот чат"
                    }

                    else -> {
                        "Бот ещё не добавлен в этот чат\nБот сможет пересылать запросы, как только будет добавлен в чат"
                    }
                }
            }

            else -> {
                ""
            }
        }
    }
}