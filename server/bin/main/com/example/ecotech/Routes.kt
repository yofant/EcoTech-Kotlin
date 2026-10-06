package com.example.ecotech

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.patch
import io.ktor.server.routing.delete
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction

private val ALLOWED_ROLES = listOf("Auditor", "Operador", "Tecnico", "Administrador")

private fun ApplicationCall.pathId(): Int? = parameters["id"]?.toIntOrNull()

private fun normalizeRole(role: String): String? {
    val match = ALLOWED_ROLES.firstOrNull { it.equals(role.trim(), ignoreCase = true) }
    return match
}

fun Route.apiRoutes() {
    rootRoutes()
    authRoutes()
    registroRoutes()
    usuarioRoutes()
}

private fun Route.rootRoutes() {
    get("/") {
        call.respond(
            ApiInfo(
                name = "EcoTech API",
                status = "ok",
                version = "1.0.0",
            ),
        )
    }

    get("/api/health") {
        val dbOk = runCatching { transaction { exec("SELECT 1") } }.isSuccess
        call.respond(HealthDto(status = if (dbOk) "ok" else "degraded", database = dbOk))
    }
}

private fun Route.authRoutes() {
    route("/api/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()

            when {
                request.name.isBlank() -> return@post call.error("El nombre es requerido")
                request.lastName.isBlank() -> return@post call.error("El apellido es requerido")
                request.email.isBlank() || !request.email.contains("@") ->
                    return@post call.error("Ingresa un correo electronico valido")
                request.phone.isBlank() -> return@post call.error("El telefono es requerido")
                request.password.length < 6 ->
                    return@post call.error("La contrasena debe tener al menos 6 caracteres")
                normalizeRole(request.role) == null ->
                    return@post call.error("El rol debe ser: Auditor, Operador, Tecnico o Administrador")
                UserRepository.findByEmail(request.email.trim()) != null ->
                    return@post call.conflict("El correo ya esta registrado")
            }

            val user = UserRepository.register(
                name = request.name.trim(),
                lastName = request.lastName.trim(),
                email = request.email.trim().lowercase(),
                phone = request.phone.trim(),
                role = normalizeRole(request.role)!!,
                password = request.password,
            )

            call.respond(HttpStatusCode.Created, user.toAuthResponse())
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            val user = UserRepository.findByEmailAndPassword(
                email = request.email.trim().lowercase(),
                password = request.password,
            )
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Correo, contrasena incorrectos o usuario inhabilitado"))
                return@post
            }

            call.respond(HttpStatusCode.OK, user.toAuthResponse())
        }
    }
}

private fun Route.registroRoutes() {
    route("/api/ciudades") {
        get {
            call.respond(ReferenceRepository.ciudades())
        }
        get("/{id}") {
            val id = call.pathId()
            if (id == null) return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Id invalido"))
            val dto = ReferenceRepository.ciudad(id)
            if (dto == null) call.respond(HttpStatusCode.NotFound, ErrorResponse("Ciudad no encontrada"))
            else call.respond(dto)
        }
        post {
            val request = call.receive<CiudadRequest>()
            if (request.nombre.isBlank() || request.departamento.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Nombre y departamento son requeridos"))
            }
            val dto = ReferenceRepository.createCiudad(request)
            if (dto == null) call.respond(HttpStatusCode.InternalServerError, ErrorResponse("No se pudo crear la ciudad"))
            else call.respond(HttpStatusCode.Created, dto)
        }
        put("/{id}") {
            val id = call.pathId()
            if (id == null) return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Id invalido"))
            val request = call.receive<CiudadRequest>()
            val ok = ReferenceRepository.updateCiudad(id, request)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Ciudad no encontrada"))
            else call.respond(okResponse("Ciudad actualizada"))
        }
        delete("/{id}") {
            val id = call.pathId()
            if (id == null) return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Id invalido"))
            val ok = ReferenceRepository.deleteCiudad(id)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Ciudad no encontrada"))
            else call.respond(HttpStatusCode.NoContent)
        }
    }

    route("/api/tipos-equipo") {
        get {
            call.respond(ReferenceRepository.tiposEquipo())
        }
        get("/{id}") {
            val id = call.pathId()
            if (id == null) return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Id invalido"))
            val dto = ReferenceRepository.tipoEquipo(id)
            if (dto == null) call.respond(HttpStatusCode.NotFound, ErrorResponse("Tipo de equipo no encontrado"))
            else call.respond(dto)
        }
        post {
            val request = call.receive<TipoEquipoRequest>()
            if (request.nombre.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Nombre es requerido"))
            }
            val dto = ReferenceRepository.createTipoEquipo(request)
            if (dto == null) call.respond(HttpStatusCode.InternalServerError, ErrorResponse("No se pudo crear"))
            else call.respond(HttpStatusCode.Created, dto)
        }
        put("/{id}") {
            val id = call.pathId()
            if (id == null) return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Id invalido"))
            val request = call.receive<TipoEquipoRequest>()
            val ok = ReferenceRepository.updateTipoEquipo(id, request)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Tipo de equipo no encontrado"))
            else call.respond(okResponse("Tipo de equipo actualizado"))
        }
        delete("/{id}") {
            val id = call.pathId()
            if (id == null) return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Id invalido"))
            val ok = ReferenceRepository.deleteTipoEquipo(id)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Tipo de equipo no encontrado"))
            else call.respond(HttpStatusCode.NoContent)
        }
    }

    route("/api/donantes") {
        get { call.respond(EquipoRepository.donantes()) }
        get("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Id invalido"))
            call.respondOr404(EquipoRepository.donante(id))
        }
        post {
            val request = call.receive<DonanteRequest>()
            if (request.nombre.isBlank() || request.email.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Nombre y correo son requeridos"))
            }
            val dto = EquipoRepository.createDonante(request)
            if (dto == null) call.respond(HttpStatusCode.InternalServerError, ErrorResponse("No se pudo crear"))
            else call.respond(HttpStatusCode.Created, dto)
        }
        put("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@put
            val request = call.receive<DonanteRequest>()
            val ok = EquipoRepository.updateDonante(id, request)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Donante no encontrado"))
            else call.respond(okResponse("Donante actualizado"))
        }
        delete("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@delete
            val ok = EquipoRepository.deleteDonante(id)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Donante no encontrado"))
            else call.respond(HttpStatusCode.NoContent)
        }
    }

    route("/api/equipos") {
        get {
            val estado = call.request.queryParameters["estado"]
            val equipos = EquipoRepository.equipos()
            if (estado.isNullOrBlank()) call.respond(equipos)
            else call.respond(equipos.filter { it.estadoActual.equals(estado, ignoreCase = true) })
        }
        get("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Id invalido"))
            call.respondOr404(EquipoRepository.equipo(id))
        }
        post {
            val request = call.receive<EquipoRequest>()
            if (request.tipoId == 0) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("El tipo de equipo es requerido"))
            }
            if (request.estadoActual.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("El estado actual es requerido"))
            }
            val dto = EquipoRepository.createEquipo(request)
            if (dto == null) call.respond(HttpStatusCode.InternalServerError, ErrorResponse("No se pudo crear"))
            else call.respond(HttpStatusCode.Created, dto)
        }
        put("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@put
            val request = call.receive<EquipoRequest>()
            val ok = EquipoRepository.updateEquipo(id, request)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Equipo no encontrado"))
            else call.respond(okResponse("Equipo actualizado"))
        }
        delete("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@delete
            val ok = EquipoRepository.deleteEquipo(id)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Equipo no encontrado"))
            else call.respond(HttpStatusCode.NoContent)
        }
    }

    route("/api/beneficiarios") {
        get { call.respond(BeneficiarioRepository.all()) }
        get("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Id invalido"))
            call.respondOr404(BeneficiarioRepository.findById(id))
        }
        post {
            val request = call.receive<BeneficiarioRequest>()
            when {
                request.nombre.isBlank() || request.apellido.isBlank() ->
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Nombre y apellido son requeridos"))
                request.documento.isBlank() ->
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("El documento es requerido"))
                request.email.isBlank() || !request.email.contains("@") ->
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ingresa un correo valido"))
                request.estrato != null && request.estrato !in 1..3 ->
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("El estrato debe estar entre 1 y 3"))
            }
            val dto = BeneficiarioRepository.create(request)
            if (dto == null) call.respond(HttpStatusCode.InternalServerError, ErrorResponse("No se pudo crear"))
            else call.respond(HttpStatusCode.Created, dto)
        }
        put("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@put
            val request = call.receive<BeneficiarioRequest>()
            val ok = BeneficiarioRepository.update(id, request)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Beneficiario no encontrado"))
            else call.respond(okResponse("Beneficiario actualizado"))
        }
        delete("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@delete
            val ok = BeneficiarioRepository.delete(id)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Beneficiario no encontrado"))
            else call.respond(HttpStatusCode.NoContent)
        }
    }

    gestionRoutes()
}

private fun Route.gestionRoutes() {
    route("/api/diagnosticos") {
        get { call.respond(GestionRepository.diagnosticos()) }
        get("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Id invalido"))
            call.respondOr404(GestionRepository.diagnostico(id))
        }
        post {
            val request = call.receive<DiagnosticoRequest>()
            if (request.equipoId == 0 || request.tecnicoId == 0) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Equipo y tecnico son requeridos"))
            }
            val dto = GestionRepository.createDiagnostico(request)
            if (dto == null) call.respond(HttpStatusCode.InternalServerError, ErrorResponse("No se pudo crear"))
            else call.respond(HttpStatusCode.Created, dto)
        }
        put("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@put
            val request = call.receive<DiagnosticoRequest>()
            val ok = GestionRepository.updateDiagnostico(id, request)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Diagnostico no encontrado"))
            else call.respond(okResponse("Diagnostico actualizado"))
        }
        delete("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@delete
            val ok = GestionRepository.deleteDiagnostico(id)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Diagnostico no encontrado"))
            else call.respond(HttpStatusCode.NoContent)
        }
    }

    route("/api/reparaciones") {
        get { call.respond(GestionRepository.reparaciones()) }
        get("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Id invalido"))
            call.respondOr404(GestionRepository.reparacion(id))
        }
        post {
            val request = call.receive<ReparacionRequest>()
            if (request.equipoId == 0 || request.tecnicoId == 0) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Equipo y tecnico son requeridos"))
            }
            val dto = GestionRepository.createReparacion(request)
            if (dto == null) call.respond(HttpStatusCode.InternalServerError, ErrorResponse("No se pudo crear"))
            else call.respond(HttpStatusCode.Created, dto)
        }
        put("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@put
            val request = call.receive<ReparacionRequest>()
            val ok = GestionRepository.updateReparacion(id, request)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Reparacion no encontrada"))
            else call.respond(okResponse("Reparacion actualizada"))
        }
        delete("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@delete
            val ok = GestionRepository.deleteReparacion(id)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Reparacion no encontrada"))
            else call.respond(HttpStatusCode.NoContent)
        }
    }

    route("/api/entregas") {
        get { call.respond(GestionRepository.entregas()) }
        get("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Id invalido"))
            call.respondOr404(GestionRepository.entrega(id))
        }
        post {
            val request = call.receive<EntregaRequest>()
            if (request.equipoId == 0 || request.beneficiarioId == 0 || request.usuarioId == 0) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Equipo, beneficiario y usuario son requeridos"))
            }
            val dto = GestionRepository.createEntrega(request)
            if (dto == null) call.respond(HttpStatusCode.InternalServerError, ErrorResponse("No se pudo crear"))
            else call.respond(HttpStatusCode.Created, dto)
        }
        put("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@put
            val request = call.receive<EntregaRequest>()
            val ok = GestionRepository.updateEntrega(id, request)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Entrega no encontrada"))
            else call.respond(okResponse("Entrega actualizada"))
        }
        delete("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@delete
            val ok = GestionRepository.deleteEntrega(id)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Entrega no encontrada"))
            else call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun Route.usuarioRoutes() {
    route("/api/usuarios") {
        get {
            call.respond(UserRepository.all())
        }
        get("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@get
            val user = UserRepository.findById(id)
            if (user == null) call.respond(HttpStatusCode.NotFound, ErrorResponse("Usuario no encontrado"))
            else call.respond(user.toAdminDto())
        }
        put("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@put
            val request = call.receive<UpdateUserRequest>()
            val ok = UserRepository.updateUser(id, request)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Usuario no encontrado"))
            else call.respond(okResponse("Usuario modificado con exito"))
        }
        patch("/{id}/estado") {
            val id = call.pathIdOrBadRequest() ?: return@patch
            val request = call.receive<EstadoRequest>()
            val ok = UserRepository.setActive(id, request.activo)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Usuario no encontrado"))
            else call.respond(okResponse(if (request.activo) "Usuario activado" else "Usuario inhabilitado"))
        }
        delete("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@delete
            val ok = UserRepository.delete(id)
            if (!ok) call.respond(HttpStatusCode.NotFound, ErrorResponse("Usuario no encontrado"))
            else call.respond(okResponse("Usuario eliminado"))
        }
    }

    route("/api/auditoria") {
        get { call.respond(Audit.all()) }
    }

    route("/api/stats") {
        get("/resumen") { call.respond(StatsRepository.resumen()) }
        get("/actividad-mensual") { call.respond(StatsRepository.actividadMensual()) }
    }
}

fun User.toAuthResponse() = AuthResponse(
    token = "token-$id::$role",
    user = UserResponse(
        id = id,
        name = name,
        lastName = lastName,
        email = email,
        phone = phone,
        role = role,
    ),
)

fun User.toAdminDto() = AdminUserDto(
    usuarioId = id,
    nombre = name,
    apellido = lastName,
    email = email,
    telefono = phone,
    rol = role,
    activo = active,
    fechaRegistro = registrationDate?.formatEco(),
)

@Serializable
private data class OkResponse(val message: String)

private fun okResponse(message: String) = OkResponse(message)

private fun ApplicationCall.pathIdOrBadRequest(): Int? {
    val id = pathId()
    if (id == null) return null
    return id
}

private suspend inline fun <reified T> ApplicationCall.respondOr404(dto: T?) {
    if (dto == null) respond(HttpStatusCode.NotFound, ErrorResponse("Registro no encontrado"))
    else respond(HttpStatusCode.OK, dto)
}

private suspend fun ApplicationCall.error(message: String) {
    respond(HttpStatusCode.BadRequest, ErrorResponse(message))
}

private suspend fun ApplicationCall.conflict(message: String) {
    respond(HttpStatusCode.Conflict, ErrorResponse(message))
}

@Serializable
private data class EstadoRequest(val activo: Boolean)