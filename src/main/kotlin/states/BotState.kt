package states

import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import domain.AdminManagedType

sealed interface BotState : State {
    data class ExpectSharedChatToDelete(
        override val context: IdChatIdentifier,
        val sourceMessage: CommonMessage<TextContent>,
        val chatType: AdminManagedType,
    ) : BotState

    data class CorrectInputSharedChatToDelete(
        override val context: IdChatIdentifier,
        val deletedChatTitle: String,
        val chatType: AdminManagedType,
    ) : BotState

    data class WrongInputSharedChatToDelete(
        override val context: IdChatIdentifier,
        val sourceMessage: CommonMessage<TextContent>,
        val chatType: AdminManagedType,
        val cause: String = "",
    ) : BotState

    data class StopState(override val context: IdChatIdentifier) : BotState

    data class PermissionsDeniedState(override val context: IdChatIdentifier) : BotState
}
