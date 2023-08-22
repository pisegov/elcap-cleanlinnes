package data.local.receiver

import data.local.chat.ChatsTable
import data.local.user.UsersTable
import domain.model.Chat
import domain.model.Receiver
import domain.model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object ReceiversTable : Table("receivers") {
    val id = integer("id").autoIncrement()
    val telegramChatId = long("telegram_chat_id")

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id, name = "receiver_id")

    private val table = this

    init {
        uniqueIndex(telegramChatId)
    }

    fun insert(chatId: Long) {
        transaction {
            insert {
                it[telegramChatId] = chatId
            }
        }
    }

    fun getAllReceivers(): List<Receiver> {
        return transaction {
            val chatsList =
                table.join(UsersTable, JoinType.LEFT, telegramChatId, UsersTable.telegramChatId)
                    .join(ChatsTable, JoinType.LEFT, telegramChatId, ChatsTable.telegramChatId).selectAll()
                    .map {
                        val name = it[UsersTable.name] ?: ""
                        if (name.isNotEmpty()) {
                            User(
                                telegramChatId = it[telegramChatId],
                                name = name,
                                username = it[UsersTable.username]
                            )
                        } else {
                            Chat(
                                telegramChatId = it[telegramChatId],
                                title = it[ChatsTable.title] ?: ""
                            )
                        }
                    }

            chatsList
        }
    }

    fun removeReceiver(telegramChatId: Long) {
        transaction {
            table.deleteWhere { ReceiversTable.telegramChatId.eq(telegramChatId) }
        }
    }
}