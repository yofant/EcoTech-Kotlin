package com.example.ecotech

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        })
    }

    routing {
        get("/") {
            call.respondText(sayHello("Ktor"))
        }

        route("/api/auth") {
            post("/register") {
                val request = call.receive<RegisterRequest>()

                when {
                    request.name.isBlank() -> {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("El nombre es requerido"))
                        return@post
                    }
                    request.email.isBlank() || !request.email.contains("@") -> {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ingresa un correo electronico valido"))
                        return@post
                    }
                    request.password.length < 6 -> {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("La contrasena debe tener al menos 6 caracteres"))
                        return@post
                    }
                    UserRepository.findByEmail(request.email) != null -> {
                        call.respond(HttpStatusCode.Conflict, ErrorResponse("El correo ya esta registrado"))
                        return@post
                    }
                }

                val user = UserRepository.register(request.name, request.email, request.password)
                val response = AuthResponse(
                    token = "token-${user.id}",
                    user = UserResponse(id = user.id, name = user.name, email = user.email),
                )
                call.respond(HttpStatusCode.Created, response)
            }

            post("/login") {
                val request = call.receive<LoginRequest>()

                val user = UserRepository.findByEmailAndPassword(request.email, request.password)
                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Correo o contrasena incorrectos"))
                    return@post
                }

                val response = AuthResponse(
                    token = "token-${user.id}",
                    user = UserResponse(id = user.id, name = user.name, email = user.email),
                )
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}
