package util

import domain.AdminManagedType

object ResourceProvider {
    const val CANCEL_STRING = "Отмена ❌"

    private val adminResources = mapOf(
        "cancel" to "Отмена ❌",
        "use_keyboard_to_select_chat" to "Воспользуйтесь клавиатурой, чтобы выбрать администратора",
        "chat_type_title" to "Администратор",
        "chats_list_is_empty" to "Список администраторов пуст",
        "active_chats" to "Активные администраторы",
        "not_active_chats" to "Неактивные администраторы",
    )

    private val receiverResources = mapOf(
        "cancel" to "Отмена ❌",
        "use_keyboard_to_select_chat" to "Воспользуйтесь клавиатурой, чтобы выбрать получателя",
        "chat_type_title" to "Получатель",
        "chats_list_is_empty" to "Список получателей пуст",
        "active_chats" to "Активные получатели",
        "not_active_chats" to "Неактивные получатели",
    )

    private fun getStringResource(title: String, chatType: AdminManagedType): String {
        return when (chatType) {
            AdminManagedType.Admin -> {
                adminResources[title]
            }

            AdminManagedType.Receiver -> {
                receiverResources[title]
            }
        } ?: ""
    }

    fun useKeyboardToSelectChat(type: AdminManagedType): String = getStringResource("use_keyboard_to_select_chat", type)

    fun chatTypeTitle(type: AdminManagedType): String = getStringResource("chat_type_title", type)

    fun chatsListIsEmpty(type: AdminManagedType): String = getStringResource("chats_list_is_empty", type)

    fun activeChats(type: AdminManagedType): String = getStringResource("active_chats", type)

    fun notActiveChats(type: AdminManagedType): String = getStringResource("not_active_chats", type)
}