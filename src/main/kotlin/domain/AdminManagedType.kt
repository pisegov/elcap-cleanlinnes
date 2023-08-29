package domain

sealed interface AdminManagedType {
    data object Admin : AdminManagedType
    data object Receiver : AdminManagedType
}