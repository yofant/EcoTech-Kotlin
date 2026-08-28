package com.example.ecotech

import java.security.MessageDigest
import java.util.UUID

data class User(
    val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
)

object UserRepository {
    private val users = mutableMapOf<String, User>()

    fun findByEmail(email: String): User? =
        users.values.find { it.email.equals(email, ignoreCase = true) }

    fun findByEmailAndPassword(email: String, password: String): User? {
        val user = findByEmail(email) ?: return null
        return if (hashPassword(password) == user.passwordHash) user else null
    }

    fun register(name: String, email: String, password: String): User {
        val id = UUID.randomUUID().toString()
        val user = User(
            id = id,
            name = name,
            email = email,
            passwordHash = hashPassword(password),
        )
        users[id] = user
        return user
    }

    fun hashPassword(raw: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
