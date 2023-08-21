package data.local.db

import domain.model.User
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

object UsersTable : Table("bot_users") {
    private val id = integer("user_id").autoIncrement()
    val telegramChatId = long("telegram_chat_id")
    val name = varchar("name", 100)
    val username = varchar("telegram_username", 100).nullable()
    override val primaryKey = PrimaryKey(id, name = "user_id")

    private val table = this

    init {
        uniqueIndex(telegramChatId)
    }

    fun insert(user: User) {
        return transaction {
            upsert {
                it[name] = user.name
                it[username] = user.username
                it[telegramChatId] = user.telegramChatId
            }
        }
    }

    fun getAllUsers(): List<UserDTO> {
        return transaction {
            val listModel = table.selectAll()

            listModel.toList().map { model ->
                UserDTO(
                    id = model[table.id],
                    telegramChatId = model[table.telegramChatId],
                    name = model[table.name],
                    username = model[table.username],
                )
            }
        }
    }

    fun removeUser(telegramChatId: Long) {
        transaction {
            table.deleteWhere { table.telegramChatId.eq(telegramChatId) }
        }
    }
}
