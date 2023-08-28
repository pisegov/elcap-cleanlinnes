package domain.states

sealed interface DeletionState {
    data object Success : DeletionState
    data object Error : DeletionState
}