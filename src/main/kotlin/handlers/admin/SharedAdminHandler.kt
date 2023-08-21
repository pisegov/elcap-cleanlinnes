package handlers.admin

import domain.states.InsertionState
import javax.inject.Inject

class SharedAdminHandler @Inject constructor() {
    fun getReplyString(insertionState: InsertionState, chatTitle: String): String {
        val replyString = StringBuilder()
        when (insertionState) {
            is InsertionState.Success -> {
                replyString.append("Администратор $chatTitle сохранён :)\n")
                replyString.append(receiverReply(chatIsActivated = chatTitle.isNotBlank()))
            }

            is InsertionState.Duplicate -> {
                replyString.append("Администратор $chatTitle уже добавлен в систему :)\n")
                replyString.append(receiverReply(chatIsActivated = chatTitle.isNotBlank()))
            }

            is InsertionState.Error -> {
                replyString.append("Что-то пошло не так, бот не смог добавить админа в систему :(")
            }
        }

        return replyString.replace("\\s{2,}".toRegex(), " ")
    }

    private fun receiverReply(chatIsActivated: Boolean): String {
        return when {
            chatIsActivated -> {
                "Пользователю доступны команды администратора"
            }

            else -> {
                "Пользователь ещё не активировал чат с ботом\nКак только он это сделает, ему будут доступны команды администратора"
            }
        }
    }
}