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
            val usersList =
                table.join(UsersTable, JoinType.INNER, telegramChatId, UsersTable.telegramChatId).selectAll()
                    .toList().map {
                        User(
                            telegramChatId = it[telegramChatId],
                            name = it[UsersTable.name],
                            username = it[UsersTable.username]
                        )
                    }
            val chatsList =
                table.join(ChatsTable, JoinType.INNER, telegramChatId, ChatsTable.telegramChatId).selectAll()
                    .toList().map {
                        Chat(
                            telegramChatId = it[telegramChatId],
                            title = it[ChatsTable.title]
                        )
                    }

            (usersList + chatsList)
        }
    }

    fun removeReceiver(telegramChatId: Long) {
        transaction {
            table.deleteWhere { ReceiversTable.telegramChatId.eq(telegramChatId) }
        }
    }
}

