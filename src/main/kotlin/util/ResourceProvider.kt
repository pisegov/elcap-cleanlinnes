package util

import domain.AdminManagedType

object ResourceProvider {
    const val CANCEL_STRING = "Отмена ❌"

    fun useKeyboardToSelectChat(type: AdminManagedType): String {
        return when (type) {
            AdminManagedType.Admin -> {
                "Воспользуйтесь клавиатурой, чтобы выбрать администратора"
            }

            AdminManagedType.Receiver -> {
                "Воспользуйтесь клавиатурой, чтобы выбрать получателя"
            }
        }
    }

    fun chatTypeTitle(type: AdminManagedType): String {
        return when (type) {
            AdminManagedType.Admin -> {
                "Администратор"
            }

            AdminManagedType.Receiver -> {
                "Получатель"
            }
        }
    }
}