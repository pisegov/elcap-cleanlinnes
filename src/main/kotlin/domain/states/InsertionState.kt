package domain.states

sealed interface InsertionState {
    data object Success : InsertionState
    data object Duplicate : InsertionState
    data object Error : InsertionState
}