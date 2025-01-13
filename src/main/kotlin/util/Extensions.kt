package util

import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.chat.ExtendedPrivateChat
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.message.abstracts.Message

fun String.removedDoubleSpaces(): String {
    return this.replace("\\s{2,}".toRegex(), " ")
}

fun StringBuilder.removedDoubleSpaces(): String {
    return this.replace("\\s{2,}".toRegex(), " ")
}

suspend fun IdChatIdentifier.getChatTitle(context: BehaviourContext): String {
    return runCatchingSafely {
        val chat = context.getChat(this)
        when (chat) {
            is ExtendedPrivateChat -> {
                "${chat.firstName} ${chat.lastName}".trimEnd()
            }

            is GroupChat -> {
                chat.title
            }

            else -> {
                ""
            }
        }
    }.getOrDefault("")
}

fun ChatId(longChatId: Long): ChatId = ChatId(RawChatId(longChatId))

val Message.chatId get() = this.chat.id.chatId.long