package com.example.ecotech

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val role: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class UserResponse(
    val id: Int,
    val name: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val role: String,
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse,
)

@Serializable
data class ErrorResponse(
    val error: String,
)

data class AuthResult(
    val success: Boolean,
    val user: UserResponse? = null,
    val token: String? = null,
    val error: String? = null,
) {
    companion object {
        fun success(auth: AuthResponse) = AuthResult(
            success = true,
            user = auth.user,
            token = auth.token,
        )

        fun failure(message: String) = AuthResult(
            success = false,
            error = message,
        )
    }
}
