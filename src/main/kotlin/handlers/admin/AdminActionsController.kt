package handlers.admin

import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import states.BotState
import javax.inject.Inject

class AdminActionsController @Inject constructor(
    private val behaviourContext: BehaviourContextWithFSM<BotState>,
) {
    suspend fun handlePermissionDeniedState(state: BotState.PermissionsDeniedState): BotState? {
        with(behaviourContext) {
            send(
                state.context,
                replyMarkup = ReplyKeyboardRemove()
            ) { +"Oops, у вас нет прав для выполнения этой команды" }

            // Return initial state
            return null
        }
    }
}