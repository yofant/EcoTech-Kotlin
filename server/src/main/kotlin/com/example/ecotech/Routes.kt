package com.example.ecotech

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.request.path
import io.ktor.server.request.httpMethod
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.patch
import io.ktor.server.routing.delete
import io.ktor.server.routing.intercept
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction

private val ALLOWED_ROLES = listOf("Usuario", "Vendedor", "Auditor", "Operador", "Tecnico", "Administrador")

private fun ApplicationCall.pathId(): Int? = parameters["id"]?.toIntOrNull()

private fun normalizeRole(role: String): String? {
    val match = ALLOWED_ROLES.firstOrNull { it.equals(role.trim(), ignoreCase = true) }
    return match
}

fun Route.apiRoutes() {
    intercept(ApplicationCallPipeline.Call) {
        val applicationCall = context
        val path = applicationCall.request.path()
        if (
            path == "/" ||
            path == "/api/health" ||
            path == "/api/auth/login" ||
            path == "/api/auth/register"
        ) return@intercept

        val token = applicationCall.request.headers["Authorization"]
            ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
            ?.substringAfter(' ')
            ?.trim()
        val user = token?.let(UserRepository::findBySessionToken)
        if (user == null) {
            applicationCall.respond(HttpStatusCode.Unauthorized, ErrorResponse("Inicia sesión para continuar"))
            finish()
            return@intercept
        }

        val method = applicationCall.request.httpMethod.value
        val allowedRoles = when {
            path.startsWith("/api/portal") -> setOf("Usuario", "Vendedor", "Operador")
            path == "/api/auth/logout" -> setOf("Usuario", "Vendedor", "Operador", "Tecnico", "Auditor", "Administrador")
            path.startsWith("/api/usuarios") -> setOf("Administrador")
            path.startsWith("/api/auditoria") || path.startsWith("/api/stats") -> setOf("Administrador", "Auditor")
            path.startsWith("/api/diagnosticos") || path.startsWith("/api/reparaciones") ->
                when (method) {
                    "GET" -> setOf("Administrador", "Tecnico", "Auditor")
                    "POST" -> setOf("Administrador", "Tecnico")
                    else -> setOf("Administrador")
                }
            path.startsWith("/api/entregas") -> setOf("Administrador", "Operador", "Auditor")
            path.startsWith("/api/puntos-recoleccion") && method == "GET" ->
                setOf("Usuario", "Vendedor", "Operador", "Tecnico", "Auditor", "Administrador")
            path.startsWith("/api/ciudades") || path.startsWith("/api/tipos-equipo") ->
                setOf("Usuario", "Vendedor", "Operador", "Tecnico", "Auditor", "Administrador")
            path.startsWith("/api/donantes") || path.startsWith("/api/beneficiarios") ||
                path.startsWith("/api/puntos-recoleccion") -> setOf("Administrador")
            path.startsWith("/api/equipos") ->
                if (method == "GET") {
                    setOf("Usuario", "Vendedor", "Operador", "Tecnico", "Auditor", "Administrador")
                } else {
                    setOf("Administrador")
                }
            else -> setOf("Administrador")
        }
        val ownUserRead = path == "/api/usuarios/${user.id}" && method == "GET"
        if (user.role !in allowedRoles && !ownUserRead) {
            applicationCall.respond(HttpStatusCode.Forbidden, ErrorResponse("Tu cargo no tiene permiso para esta operación"))
            finish()
            return@intercept
        }
        if (
            user.role != "Administrador" &&
            method != "GET" &&
            (path.startsWith("/api/ciudades") || path.startsWith("/api/tipos-equipo"))
        ) {
            applicationCall.respond(HttpStatusCode.Forbidden, ErrorResponse("Solo Administración puede modificar catálogos"))
            finish()
        }
    }
    rootRoutes()
    authRoutes()
    registroRoutes()
    usuarioRoutes()
    portalRoutes()
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
                normalizeRole(request.role) !in listOf("Usuario", "Vendedor") ->
                    return@post call.error("El registro público solo permite los cargos Usuario y Vendedor")
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

            call.respond(HttpStatusCode.Created, UserRepository.createSession(user))
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

            call.respond(HttpStatusCode.OK, UserRepository.createSession(user))
        }

        post("/logout") {
            val token = call.request.headers["Authorization"]
                ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
                ?.substringAfter(' ')
                ?.trim()
            if (token != null) UserRepository.revokeSession(token)
            call.respond(HttpStatusCode.NoContent)
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
            val user = currentUser(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val visible = if (user.role in setOf("Usuario", "Vendedor")) {
                equipos.filter { it.publicado && it.estadoActual.equals("Publicado", ignoreCase = true) }
            } else {
                equipos
            }
            if (estado.isNullOrBlank()) call.respond(visible)
            else call.respond(visible.filter { it.estadoActual.equals(estado, ignoreCase = true) })
        }
        get("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Id invalido"))
            val user = currentUser(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val equipment = EquipoRepository.equipo(id)
            val isPublic = equipment?.let { it.publicado && it.estadoActual.equals("Publicado", ignoreCase = true) } == true
            if (user.role in setOf("Usuario", "Vendedor") && !isPublic) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Equipo no encontrado"))
            } else {
                call.respondOr404(equipment)
            }
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
        get {
            val user = currentUser(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(GestionRepository.diagnosticos(user.id.takeIf { user.role == "Tecnico" }))
        }
        get("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Id invalido"))
            val user = currentUser(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val diagnosis = GestionRepository.diagnostico(id)
            if (user.role == "Tecnico" && diagnosis?.tecnicoId != user.id) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Diagnóstico no encontrado"))
            } else {
                call.respondOr404(diagnosis)
            }
        }
        post {
            val user = currentUser(call) ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val received = call.receive<DiagnosticoRequest>()
            val request = if (user.role == "Tecnico") received.copy(tecnicoId = user.id) else received
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
        get {
            val user = currentUser(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(GestionRepository.reparaciones(user.id.takeIf { user.role == "Tecnico" }))
        }
        get("/{id}") {
            val id = call.pathIdOrBadRequest() ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Id invalido"))
            val user = currentUser(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val repair = GestionRepository.reparacion(id)
            if (user.role == "Tecnico" && repair?.tecnicoId != user.id) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Reparación no encontrada"))
            } else {
                call.respondOr404(repair)
            }
        }
        post {
            val user = currentUser(call) ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val received = call.receive<ReparacionRequest>()
            val request = if (user.role == "Tecnico") received.copy(tecnicoId = user.id) else received
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
        post {
            val request = call.receive<CreateAdminUserRequest>()
            val normalizedRole = normalizeRole(request.rol)
            if (
                request.nombre.isBlank() || request.apellido.isBlank() ||
                request.email.isBlank() || !request.email.contains("@") ||
                request.telefono.isBlank() || request.password.length < 6 ||
                normalizedRole == null
            ) return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Completa todos los campos y usa una contraseña de al menos 6 caracteres"))
            if (UserRepository.findByEmail(request.email.trim().lowercase()) != null) {
                return@post call.respond(HttpStatusCode.Conflict, ErrorResponse("El correo ya está registrado"))
            }
            val created = UserRepository.createByAdmin(
                request.nombre.trim(),
                request.apellido.trim(),
                request.email.trim().lowercase(),
                request.telefono.trim(),
                normalizedRole,
                request.password,
            )
            call.respond(HttpStatusCode.Created, created)
        }
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
            if (request.rol != null && normalizeRole(request.rol) == null) {
                return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("El cargo no es válido"))
            }
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
            if (currentUser(call)?.id == id) {
                return@delete call.respond(HttpStatusCode.Conflict, ErrorResponse("No puedes eliminar tu propia cuenta"))
            }
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

private fun Route.portalRoutes() {
    route("/api/portal") {
        get("/initial") {
            val user = currentUser(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(PortalRepository.initial(user))
        }
        get("/messages") {
            val user = currentUser(call) ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val conversationId = call.request.queryParameters["conversacion_id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Conversación inválida"))
            val messages = PortalRepository.messages(user, conversationId)
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("No se encontró la conversación o no tienes acceso"))
            call.respond(messages)
        }
        post("/conversations") {
            val user = currentUser(call) ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request = call.receive<StartConversationRequest>()
            val id = PortalRepository.startConversation(user, request)
                ?: return@post call.respond(HttpStatusCode.UnprocessableEntity, ErrorResponse("El contacto o equipo seleccionado ya no está disponible"))
            call.respond(HttpStatusCode.Created, mapOf("conversacionId" to id))
        }
        post("/messages") {
            val user = currentUser(call) ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request = call.receive<SendMessageRequest>()
            if (request.contenido.isBlank() || request.contenido.length > 2000) {
                return@post call.respond(HttpStatusCode.UnprocessableEntity, ErrorResponse("El mensaje debe tener entre 1 y 2000 caracteres"))
            }
            if (!PortalRepository.sendMessage(user, request)) {
                return@post call.respond(HttpStatusCode.NotFound, ErrorResponse("No se encontró la conversación o no tienes acceso"))
            }
            call.respond(HttpStatusCode.Created, mapOf("enviado" to true))
        }
        post("/equipment") {
            val user = currentUser(call) ?: return@post call.respond(HttpStatusCode.Unauthorized)
            if (user.role != "Vendedor") {
                return@post call.respond(HttpStatusCode.Forbidden, ErrorResponse("Solo Vendedores pueden publicar equipos"))
            }
            val request = call.receive<PublicarEquipoRequest>()
            if (
                request.marca.isBlank() || request.modelo.isBlank() ||
                request.marca.length > 100 || request.modelo.length > 100 ||
                (request.serial?.length ?: 0) > 100 || (request.descripcion?.length ?: 0) > 200
            ) return@post call.respond(HttpStatusCode.UnprocessableEntity, ErrorResponse("Completa tipo, marca y modelo; revisa el largo de los campos"))
            val created = EquipoRepository.createEquipo(
                EquipoRequest(
                    tipoId = request.tipoId,
                    estadoIngreso = "Usado",
                    estadoActual = "Publicado",
                    marca = request.marca.trim(),
                    modelo = request.modelo.trim(),
                    serial = request.serial?.trim()?.ifBlank { null },
                    descripcion = request.descripcion?.trim(),
                    usuarioId = user.id,
                    publicado = true,
                )
            ) ?: return@post call.respond(HttpStatusCode.InternalServerError, ErrorResponse("No fue posible publicar el equipo"))
            call.respond(HttpStatusCode.Created, created)
        }
    }

    route("/api/puntos-recoleccion") {
        get {
            val includeInactive = currentUser(call)?.role == "Administrador"
            call.respond(PortalRepository.points(includeInactive))
        }
        post {
            val request = call.receive<PuntoRecoleccionRequest>()
            if (
                request.nombre.isBlank() || request.nombre.length > 120 ||
                request.direccion.isBlank() || request.direccion.length > 250 ||
                request.horario.isBlank() || request.horario.length > 150 ||
                (request.instrucciones?.length ?: 0) > 250
            ) return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Completa nombre, dirección y horario con longitudes válidas"))
            val point = PortalRepository.createPoint(request)
                ?: return@post call.respond(HttpStatusCode.InternalServerError, ErrorResponse("No se pudo crear el punto"))
            call.respond(HttpStatusCode.Created, point)
        }
        delete("/{id}") pointDelete@{
            val id = call.pathIdOrBadRequest() ?: return@pointDelete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Id inválido"))
            if (!PortalRepository.deletePoint(id)) {
                return@pointDelete call.respond(HttpStatusCode.NotFound, ErrorResponse("Punto de recolección no encontrado"))
            }
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun currentUser(call: ApplicationCall): User? {
    val token = call.request.headers["Authorization"]
        ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
        ?.substringAfter(' ')
        ?.trim()
        ?: return null
    return UserRepository.findBySessionToken(token)
}

fun User.toAuthResponse(token: String) = AuthResponse(
    token = token,
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