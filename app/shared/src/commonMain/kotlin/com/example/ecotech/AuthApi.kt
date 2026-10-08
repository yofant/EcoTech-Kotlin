package com.example.ecotech

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

object AuthApi {
    /**
     * URL base configurable. Si no se especifica, se detecta automáticamente según la plataforma:
     * - Android (Emulador): http://10.0.2.2:8080/api/auth
     * - Desktop / Otros: http://localhost:8080/api/auth
     */
    var customBaseUrl: String? = null
    var currentToken: String? = null
        private set

    val baseUrl: String
        get() = customBaseUrl ?: if (getPlatform().name.startsWith("Android")) {
            "http://10.0.2.2:8080/api/auth"
        } else {
            "http://localhost:8080/api/auth"
        }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun register(
        name: String,
        lastName: String,
        email: String,
        phone: String,
        role: String,
        password: String,
    ): AuthResult {
        return try {
            val response: HttpResponse = client.post("$baseUrl/register") {
                contentType(ContentType.Application.Json)
                setBody(
                    RegisterRequest(
                        name = name,
                        lastName = lastName,
                        email = email,
                        phone = phone,
                        role = role,
                        password = password,
                    )
                )
            }
            handleResponse(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.failure("No se pudo conectar con el servidor ($baseUrl). Revisa tu conexión.")
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val response: HttpResponse = client.post("$baseUrl/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email = email, password = password))
            }
            handleResponse(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.failure("No se pudo conectar con el servidor ($baseUrl). Revisa tu conexión.")
        }
    }

    suspend fun logout() {
        val token = currentToken ?: return
        val response = client.post("$baseUrl/logout") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        check(response.status.isSuccess()) { "El servidor no pudo revocar la sesión." }
        clearSession()
    }

    private suspend fun handleResponse(response: HttpResponse): AuthResult {
        return if (response.status.isSuccess()) {
            val auth = response.body<AuthResponse>()
            currentToken = auth.token
            AuthResult.success(auth)
        } else {
            val errorBody = try {
                response.body<ErrorResponse>()
            } catch (e: Exception) {
                ErrorResponse("Error inesperado en el servidor")
            }
            AuthResult.failure(errorBody.error)
        }
    }

    fun clearSession() {
        currentToken = null
    }
}
