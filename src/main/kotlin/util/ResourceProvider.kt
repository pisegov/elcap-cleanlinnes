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

    fun chatsListIsEmpty(type: AdminManagedType): String {
        return when (type) {
            AdminManagedType.Admin -> {
                "Список администраторов пуст"
            }

            AdminManagedType.Receiver -> {
                "Список получателей пуст"
            }
        }
    }

    fun activeChats(type: AdminManagedType): String {
        return when (type) {
            AdminManagedType.Admin -> {
                "Активные администраторы"
            }

            AdminManagedType.Receiver -> {
                "Активные получатели"
            }
        }
    }

    fun notActiveChats(type: AdminManagedType): String {
        return when (type) {
            AdminManagedType.Admin -> {
                "Неактивные администраторы"
            }

            AdminManagedType.Receiver -> {
                "Неактивные получатели"
            }
        }
    }
}