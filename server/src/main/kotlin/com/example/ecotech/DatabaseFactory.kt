package com.example.ecotech

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val host = System.getenv("DB_HOST") ?: "localhost"
        val port = System.getenv("DB_PORT") ?: "3306"
        val databaseName = System.getenv("DB_NAME") ?: "EcoTech"
        val user = System.getenv("DB_USER") ?: "root"
        val password = System.getenv("DB_PASSWORD") ?: ""

        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:mysql://$host:$port/$databaseName?useSSL=false&serverTimezone=UTC"
            driverClassName = "com.mysql.cj.jdbc.Driver"
            username = user
            this.password = password
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        Database.connect(HikariDataSource(config))

        transaction {
            SchemaUtils.createMissingTablesAndColumns(Usuarios)
        }
    }
}

object Usuarios : Table("Usuarios") {
    val id = integer("usuario_id").autoIncrement()
    val name = varchar("nombre", 100)
    val lastName = varchar("apellido", 100)
    val email = varchar("email", 150).uniqueIndex()
    val phone = varchar("telefono", 20)
    val role = varchar("rol", 50)
    val passwordHash = varchar("password_hash", 255)

    override val primaryKey = PrimaryKey(id)
}
