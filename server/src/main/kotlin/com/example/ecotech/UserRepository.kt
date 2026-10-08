package com.example.ecotech

import org.mindrot.jbcrypt.BCrypt
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.Base64

data class User(
    val id: Int,
    val name: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val role: String,
    val active: Boolean,
    val registrationDate: LocalDateTime?,
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
        active = this[Usuarios.active],
        registrationDate = this.getOrNull(Usuarios.registrationDate),
        passwordHash = this[Usuarios.passwordHash],
    )

    private fun User.toAdminDto() = AdminUserDto(
        usuarioId = id,
        nombre = name,
        apellido = lastName,
        email = email,
        telefono = phone,
        rol = role,
        activo = active,
        fechaRegistro = registrationDate?.formatEco(),
    )

    fun findByEmail(email: String): User? = transaction {
        Usuarios.selectAll()
            .where { Usuarios.email eq email }
            .singleOrNull()
            ?.toUser()
    }

    fun findById(id: Int): User? = transaction {
        Usuarios.selectAll()
            .where { Usuarios.id eq id }
            .singleOrNull()
            ?.toUser()
    }

    fun findByEmailAndPassword(email: String, password: String): User? {
        val user = findByEmail(email) ?: return null
        val valid = when {
            user.passwordHash.startsWith("\$2") -> runCatching {
                BCrypt.checkpw(password, user.passwordHash.replaceFirst("\$2y\$", "\$2a\$"))
            }.getOrDefault(false)
            hashPassword(password) == user.passwordHash -> {
                transaction {
                    Usuarios.update({ Usuarios.id eq user.id }) {
                        it[passwordHash] = hashCredential(password)
                    }
                }
                true
            }
            else -> false
        }
        return user.takeIf { valid && it.active }
    }

    fun createSession(user: User): AuthResponse {
        val tokenBytes = ByteArray(32)
        SecureRandom().nextBytes(tokenBytes)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes)
        transaction {
            AuthSessions.insert {
                it[usuarioId] = user.id
                it[tokenHash] = hashPassword(token)
                it[expiresAt] = now().plusDays(30)
            }
        }
        return user.toAuthResponse(token)
    }

    fun findBySessionToken(token: String): User? {
        if (token.isBlank()) return null
        val tokenHash = hashPassword(token)
        val userId = transaction {
            AuthSessions.selectAll()
                .where { (AuthSessions.tokenHash eq tokenHash) and (AuthSessions.expiresAt greater now()) }
                .singleOrNull()
                ?.get(AuthSessions.usuarioId)
        } ?: return null
        return findById(userId)?.takeIf { it.active }
    }

    fun revokeSession(token: String) {
        val tokenHash = hashPassword(token)
        transaction {
            AuthSessions.deleteWhere { AuthSessions.tokenHash eq tokenHash }
        }
    }

    fun createByAdmin(
        name: String,
        lastName: String,
        email: String,
        phone: String,
        role: String,
        password: String,
    ): AdminUserDto {
        val user = register(name, lastName, email, phone, role, password)
        return user.toAdminDto()
    }

    fun register(name: String, lastName: String, email: String, phone: String, role: String, password: String): User {
        val id = transaction {
            val newId = Usuarios.insert {
                it[Usuarios.name] = name
                it[Usuarios.lastName] = lastName
                it[Usuarios.email] = email
                it[Usuarios.phone] = phone
                it[Usuarios.role] = role
                it[Usuarios.active] = true
                it[Usuarios.registrationDate] = now()
                it[Usuarios.passwordHash] = hashCredential(password)
            } get Usuarios.id
            Audit.record("Usuarios", "INSERT", newId, "Usuario registrado: $email")
            newId
        }
        return requireNotNull(findById(id)) { "No se pudo crear el usuario" }
    }

    fun all(): List<AdminUserDto> = transaction {
        Usuarios.selectAll()
            .orderBy(Usuarios.id)
            .map { it.toUser().toAdminDto() }
    }

    fun updateUser(id: Int, req: UpdateUserRequest): Boolean = transaction {
        val updated = Usuarios.update({ Usuarios.id eq id }) {
            req.nombre?.let { value -> it[Usuarios.name] = value }
            req.apellido?.let { value -> it[Usuarios.lastName] = value }
            req.email?.let { value -> it[Usuarios.email] = value }
            req.telefono?.let { value -> it[Usuarios.phone] = value }
            req.rol?.let { value -> it[Usuarios.role] = value }
            req.password?.takeIf { it.isNotBlank() }?.let { value ->
                it[Usuarios.passwordHash] = hashCredential(value)
            }
        }
        if (updated > 0) Audit.record("Usuarios", "UPDATE", id, "Usuario modificado")
        updated > 0
    }

    fun setActive(id: Int, active: Boolean): Boolean = transaction {
        val updated = Usuarios.update({ Usuarios.id eq id }) {
            it[Usuarios.active] = active
        }
        if (updated > 0) {
            Audit.record("Usuarios", if (active) "ACTIVAR" else "INHABILITAR", id, "Usuario ${if (active) "activado" else "inhabilitado"}")
        }
        updated > 0
    }

    fun delete(id: Int): Boolean = transaction {
        val deleted = Usuarios.deleteWhere { Usuarios.id eq id }
        if (deleted > 0) Audit.record("Usuarios", "DELETE", id, "Usuario eliminado")
        deleted > 0
    }

    fun hashPassword(raw: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun hashCredential(raw: String): String = BCrypt.hashpw(raw, BCrypt.gensalt(12))
}