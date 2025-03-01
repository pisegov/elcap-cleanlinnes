package util

import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.chat.ExtendedPrivateChat
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.chat.PrivateChat
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.MediaGroupContent
import dev.inmo.tgbotapi.types.message.content.PhotoContent
import dev.inmo.tgbotapi.types.message.content.VideoContent
import dev.inmo.tgbotapi.types.message.content.VisualMediaGroupPartContent
import dev.inmo.tgbotapi.types.message.textsources.mentionTextSource
import dev.inmo.tgbotapi.types.message.textsources.plus
import dev.inmo.tgbotapi.types.message.textsources.regularTextSource
import domain.model.User

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

fun PrivateChat.toDomainModel() = User(
    telegramChatId = id.chatId.long,
    name = "$firstName $lastName".trim(),
    username = username?.username
)

fun CommonMessage<MediaGroupContent<VisualMediaGroupPartContent>>.getContentWithUserMention(
    user: User,
): List<VisualMediaGroupPartContent> {

    val contentGroup = content.group
    val rawChatId = RawChatId(user.telegramChatId)
    val mainContent = content.mainContent as VisualMediaGroupPartContent

    val dividerTextSource = regularTextSource("\n\n")
    val messageTextSources = mentionTextSource(text = user.name, rawChatId) +
            dividerTextSource +
            mainContent.textSources

    val newGroup = buildList {

        val main = when (mainContent) {
            is PhotoContent -> mainContent.copy(textSources = messageTextSources)
            is VideoContent -> mainContent.copy(textSources = messageTextSources)
        }

        add(main)
        addAll(contentGroup.subList(1, contentGroup.size).map { it.content })
    }

    return newGroup
}