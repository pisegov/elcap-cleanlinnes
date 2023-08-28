package data.local.receiver

import data.local.group.GroupsTable
import data.local.user.UsersTable
import domain.model.Group
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
                    .join(GroupsTable, JoinType.LEFT, telegramChatId, GroupsTable.telegramChatId).selectAll()
                    .map {
                        val name = it[UsersTable.name] ?: ""
                        if (name.isNotEmpty()) {
                            User(
                                telegramChatId = it[telegramChatId],
                                name = name,
                                username = it[UsersTable.username]
                            )
                        } else {
                            Group(
                                telegramChatId = it[telegramChatId],
                                chatTitle = it[GroupsTable.title] ?: ""
                            )
                        }
                    }

            chatsList
        }
    }

    fun removeReceiver(telegramChatId: Long): Boolean {
        return transaction {
            table.deleteWhere { ReceiversTable.telegramChatId.eq(telegramChatId) } > 0
        }
    }
}