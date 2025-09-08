package handlers.user

import dev.inmo.tgbotapi.extensions.api.forwardMessage
import dev.inmo.tgbotapi.extensions.api.send.media.sendVisualMediaGroup
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.abstracts.PossiblyForwardedMessage
import dev.inmo.tgbotapi.types.message.content.MediaGroupContent
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.content.VisualMediaGroupPartContent
import dev.inmo.tgbotapi.types.message.textsources.TextSourcesList
import dev.inmo.tgbotapi.utils.extensions.threadIdOrNull
import util.ChatId
import util.runSuspendCatching
import javax.inject.Inject

class TelegramMessageSender @Inject constructor(
    private val behaviourContext: BehaviourContext,
){
    suspend fun sendMessage(chatId: Long, messageText: String): Result<ContentMessage<TextContent>> {
        return runSuspendCatching {
            behaviourContext.send(chatId = ChatId(chatId), text = messageText)
        }
    }

    suspend fun sendMessage(chatId: Long, textSources: TextSourcesList): Result<ContentMessage<TextContent>> {
        return runSuspendCatching {
            behaviourContext.send(chatId = ChatId(chatId), entities = textSources)
        }
    }

    suspend fun forwardMessage(
        chatId: Long,
        message: ContentMessage<MessageContent>,
    ): Result<PossiblyForwardedMessage> {
        return runSuspendCatching {
            behaviourContext.forwardMessage(
                toChatId = ChatId(chatId),
                message = message,
                threadId = message.threadIdOrNull,
            )
        }
    }

    suspend fun sendVisualMediaGroup(
        chatId: Long,
        media: List<VisualMediaGroupPartContent>,
    ): Result<ContentMessage<MediaGroupContent<VisualMediaGroupPartContent>>> {
        return runSuspendCatching {
            behaviourContext.sendVisualMediaGroup(chatId = ChatId(chatId), media = media)
        }
    }
}