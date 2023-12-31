package data.local.admin

import data.local.user.UsersTable
import domain.model.Admin
import domain.model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object AdminsTable : Table("admins") {
    val id = integer("id").autoIncrement()
    val telegramChatId = long("telegram_chat_id")

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id, name = "admin_id")

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

    fun removeAdmin(telegramChatId: Long): Boolean = transaction {
        // Returns true on successful deletion or false, if there is no such telegramChatId
        return@transaction table.deleteWhere { AdminsTable.telegramChatId.eq(telegramChatId) } > 0
    }

    fun getAllAdmins(): List<Admin> = transaction {
        table.join(UsersTable, JoinType.LEFT, telegramChatId, UsersTable.telegramChatId).selectAll()
            .map {
                User(
                    telegramChatId = it[telegramChatId],
                    name = it[UsersTable.name] ?: "",
                    username = it[UsersTable.username]
                )
            }
    }
}

