package com.example.ecotech

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

object AuthApi {
    private const val BASE_URL = "http://localhost:8080/api/auth"

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
            val response: HttpResponse = client.post("$BASE_URL/register") {
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
            AuthResult.failure("No se pudo conectar con el servidor. Revisa tu conexión.")
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val response: HttpResponse = client.post("$BASE_URL/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email = email, password = password))
            }
            handleResponse(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.failure("No se pudo conectar con el servidor. Revisa tu conexión.")
        }
    }

    private suspend fun handleResponse(response: HttpResponse): AuthResult {
        return if (response.status.isSuccess()) {
            AuthResult.success(response.body<AuthResponse>())
        } else {
            val errorBody = try {
                response.body<ErrorResponse>()
            } catch (e: Exception) {
                ErrorResponse("Error inesperado en el servidor")
            }
            AuthResult.failure(errorBody.error)
        }
    }
}
