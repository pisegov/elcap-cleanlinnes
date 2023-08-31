package util

import domain.AdminManagedType

object ResourceProvider {
    const val CANCEL_STRING = "Отмена ❌"

    private val adminResources = mapOf(
        "use_keyboard_to_select_chat" to "Воспользуйтесь клавиатурой, чтобы выбрать администратора",
        "chat_type_title" to "Администратор",
        "chats_list_is_empty" to "Список администраторов пуст",
        "active_chats" to "Активные администраторы",
        "not_active_chats" to "Неактивные администраторы",
        "whom" to "администратора",
        "use_button_to_share_chat" to "Воспользуйтесь кнопкой, чтобы отправить боту пользователя",
        "user_is_ready" to "Пользователю доступны команды администратора",
        "user_is_not_ready" to "Пользователь ещё не активировал чат с ботом\nКак только он это сделает, ему будут доступны команды администратора",
        "welcome_message" to """
           Вас назначили администратором этого бота!
           Чтобы ознакомиться с командами администратора, введите команду /admin
           
           Закрепите сообщение с командами, чтобы не потерять :)
        """.trimIndent()
    )

    private val receiverResources = mapOf(
        "use_keyboard_to_select_chat" to "Воспользуйтесь клавиатурой, чтобы выбрать получателя",
        "chat_type_title" to "Получатель",
        "chats_list_is_empty" to "Список получателей пуст",
        "active_chats" to "Активные получатели",
        "not_active_chats" to "Неактивные получатели",
        "whom" to "получателя",
        "use_button_to_share_chat" to "Воспользуйтесь кнопкой, чтобы отправить боту пользователя или группу",
        "user_is_ready" to "Бот готов пересылать пользователю запросы",
        "user_is_not_ready" to "Пользователь ещё не активировал чат с ботом\nКак только он это сделает, бот сможет пересылать запросы",
        "welcome_message" to """
            Вас назначили получателем запросов!
            Теперь запросы от наших посетителей будут пересылаться в этот чат
        """.trimIndent()
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

    fun whom(type: AdminManagedType): String = getStringResource("whom", type)
    fun useButtonToShareChat(type: AdminManagedType): String = getStringResource("use_button_to_share_chat", type)
    fun userIsReady(type: AdminManagedType): String = getStringResource("user_is_ready", type)
    fun userIsNotReady(type: AdminManagedType): String = getStringResource("user_is_not_ready", type)

    fun welcomeMessage(type: AdminManagedType): String = getStringResource("welcome_message", type)
}