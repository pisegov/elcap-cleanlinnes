package util

import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.requestGroupButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.requestUserButton
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardMarkup
import dev.inmo.tgbotapi.types.buttons.reply.simpleReplyButton
import dev.inmo.tgbotapi.types.request.RequestId
import dev.inmo.tgbotapi.utils.row
import domain.model.Chat

object KeyboardBuilder {
    private val requestIdUserAny = RequestId(2)
    private val requestIdGroup = RequestId(12)

    fun shareChatKeyboard(): ReplyKeyboardMarkup {
        return replyKeyboard(
            resizeKeyboard = true,
            oneTimeKeyboard = true,
        ) {
            row {
                requestUserButton("Добавить пользователя ", requestIdUserAny)
                requestGroupButton("Добавить группу", requestIdGroup)
            }
        }
    }

    fun shareAdminKeyboard(): ReplyKeyboardMarkup {
        return replyKeyboard(
            resizeKeyboard = true,
            oneTimeKeyboard = true,
        ) {
            row {
                requestUserButton("Добавить пользователя ", requestIdUserAny)
            }
        }
    }

    fun chatsToRemoveKeyboard(list: List<Chat>): ReplyKeyboardMarkup {
        return replyKeyboard(
            resizeKeyboard = true,
            oneTimeKeyboard = true,
        ) {
            add(listOf(simpleReplyButton(ResourceProvider.CANCEL_STRING)))
            list.forEach { chat ->
                val string = "${chat.chatTitle} [id:${chat.telegramChatId}]"
                row { simpleReplyButton(string) }
                add(
                    listOf(simpleReplyButton(string))
                )
            }
        }
    }
}