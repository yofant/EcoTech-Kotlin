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
