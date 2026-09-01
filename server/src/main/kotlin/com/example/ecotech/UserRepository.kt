package com.example.ecotech

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest

data class User(
    val id: Int,
    val name: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val role: String,
    val passwordHash: String,
)

object UserRepository {

    private fun ResultRow.toUser() = User(
        id = this[Usuarios.id],
        name = this[Usuarios.name],
        lastName = this[Usuarios.lastName],
        email = this[Usuarios.email],
        phone = this[Usuarios.phone],
        role = this[Usuarios.role],
        passwordHash = this[Usuarios.passwordHash],
    )

    fun findByEmail(email: String): User? = transaction {
        Usuarios.selectAll()
            .where { Usuarios.email eq email }
            .singleOrNull()
            ?.toUser()
    }

    fun findByEmailAndPassword(email: String, password: String): User? {
        val user = findByEmail(email) ?: return null
        return if (hashPassword(password) == user.passwordHash) user else null
    }

    fun register(name: String, lastName: String, email: String, phone: String, role: String, password: String): User {
        val id = transaction {
            Usuarios.insert {
                it[Usuarios.name] = name
                it[Usuarios.lastName] = lastName
                it[Usuarios.email] = email
                it[Usuarios.phone] = phone
                it[Usuarios.role] = role
                it[Usuarios.passwordHash] = hashPassword(password)
            } get Usuarios.id
        }
        return User(
            id = id,
            name = name,
            lastName = lastName,
            email = email,
            phone = phone,
            role = role,
            passwordHash = hashPassword(password),
        )
    }

    fun hashPassword(raw: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
