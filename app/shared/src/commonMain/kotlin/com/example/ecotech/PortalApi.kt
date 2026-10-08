package com.example.ecotech

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ApiEquipmentType(val tipoId: Int, val nombre: String, val descripcion: String? = null)

@Serializable
data class ApiEquipment(
    val equipoId: Int,
    val tipoId: Int,
    val tipoNombre: String? = null,
    val donanteId: Int? = null,
    val donanteNombre: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val serial: String? = null,
    val estadoIngreso: String? = null,
    val estadoActual: String,
    val descripcion: String? = null,
    val fechaRecepcion: String? = null,
    val usuarioId: Int? = null,
    val publicado: Boolean = false,
    val vendedorNombre: String? = null,
)

@Serializable
data class ApiPoint(
    val puntoId: Int,
    val nombre: String,
    val ciudadId: Int? = null,
    val ciudad: String? = null,
    val direccion: String,
    val horario: String,
    val instrucciones: String? = null,
    val activo: Boolean = true,
)

@Serializable
data class ApiContact(val id: Int, val nombre: String)

@Serializable
data class ApiConversation(
    val conversacionId: Int,
    val contactoId: Int,
    val contactoNombre: String,
    val tipo: String,
    val equipoId: Int? = null,
    val equipoMarca: String? = null,
    val equipoModelo: String? = null,
    val ultimoMensaje: String? = null,
    val ultimaFecha: String? = null,
    val noLeidos: Int = 0,
)

@Serializable
data class ApiMessage(
    val mensajeId: Long,
    val emisorId: Int,
    val emisorNombre: String,
    val contenido: String,
    val fechaEnvio: String,
)

@Serializable
data class ApiPortalInitial(
    val tipos: List<ApiEquipmentType> = emptyList(),
    val equipos: List<ApiEquipment> = emptyList(),
    val puntos: List<ApiPoint> = emptyList(),
    val operadores: List<ApiContact> = emptyList(),
    val conversaciones: List<ApiConversation> = emptyList(),
)

@Serializable
data class ApiUser(
    val usuarioId: Int,
    val nombre: String,
    val apellido: String,
    val email: String,
    val telefono: String,
    val rol: String,
    val activo: Boolean,
    val fechaRegistro: String? = null,
)

@Serializable
data class ApiDonor(
    val donanteId: Int,
    val tipo: String,
    val nombre: String,
    val email: String,
    val telefono: String,
    val ciudadId: Int? = null,
    val ciudadNombre: String? = null,
    val direccion: String,
    val fechaRegistro: String? = null,
)

@Serializable
data class ApiCity(val ciudadId: Int, val nombre: String, val departamento: String)

@Serializable
data class ApiLabelValue(val label: String, val value: Int)

@Serializable
data class ApiStats(
    val totalUsuarios: Int,
    val totalEquipos: Int,
    val equiposPorEstado: List<ApiLabelValue>,
    val totalBeneficiarios: Int,
    val totalDonantes: Int,
    val totalReparaciones: Int,
    val totalEntregas: Int,
    val entregasDelMes: Int,
    val recoleccionesDelMes: Int,
    val co2EstimadoKg: Double,
)

@Serializable
data class ApiAudit(
    val auditoriaId: Int,
    val tablaAfectada: String,
    val operacion: String,
    val registroId: Int? = null,
    val usuarioSql: String? = null,
    val fecha: String? = null,
    val detalle: String? = null,
    val valoresAnteriores: String? = null,
    val valoresNuevos: String? = null,
)

@Serializable
data class ApiDiagnosis(
    val diagnosticoId: Int,
    val equipoId: Int,
    val tecnicoId: Int,
    val descripcion: String? = null,
    val requiereReparacion: Boolean = true,
    val costoEstimado: Double? = null,
    val fecha: String? = null,
)

@Serializable
data class ApiRepair(
    val reparacionId: Int,
    val equipoId: Int,
    val tecnicoId: Int,
    val descripcion: String? = null,
    val repuestosUsados: String? = null,
    val costoReal: Double? = null,
    val estado: String? = null,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
)

@Serializable
data class ApiCreateUser(
    val nombre: String,
    val apellido: String,
    val email: String,
    val telefono: String,
    val rol: String,
    val password: String,
)

@Serializable
data class ApiUpdateUser(
    val nombre: String,
    val apellido: String,
    val email: String,
    val telefono: String,
    val rol: String,
    val password: String? = null,
)

@Serializable
data class ApiUpdateUserState(val activo: Boolean)

@Serializable
data class ApiDonorRequest(
    val tipo: String,
    val nombre: String,
    val email: String,
    val telefono: String,
    val ciudadId: Int? = null,
    val direccion: String,
)

@Serializable
data class ApiPointRequest(
    val nombre: String,
    val ciudadId: Int? = null,
    val direccion: String,
    val horario: String,
    val instrucciones: String? = null,
)

@Serializable
data class ApiStartConversation(val tipo: String, val contactoId: Int, val equipoId: Int? = null)

@Serializable
data class ApiSendMessage(val conversacionId: Int, val contenido: String)

@Serializable
data class ApiPublishEquipment(
    val tipoId: Int,
    val marca: String,
    val modelo: String,
    val serial: String? = null,
    val descripcion: String? = null,
)

@Serializable
data class ApiDiagnosisRequest(
    val equipoId: Int,
    val tecnicoId: Int,
    val descripcion: String,
    val requiereReparacion: Boolean,
    val costoEstimado: Double? = null,
)

@Serializable
data class ApiRepairRequest(
    val equipoId: Int,
    val tecnicoId: Int,
    val descripcion: String,
    val repuestosUsados: String? = null,
    val costoReal: Double? = null,
    val estado: String,
    val fechaFin: String? = null,
)

object PortalApi {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val root: String
        get() = AuthApi.baseUrl.substringBefore("/api/auth").trimEnd('/') + "/api"

    private fun authToken(): String =
        AuthApi.currentToken ?: error("La sesión expiró. Inicia sesión nuevamente.")

    private suspend inline fun <reified T : Any> get(path: String): T {
        val response = client.get("$root$path") {
            header(HttpHeaders.Authorization, "Bearer ${authToken()}")
        }
        return response.decodeOrThrow()
    }

    private suspend inline fun <reified B : Any, reified T : Any> post(path: String, body: B): T {
        val response = client.post("$root$path") {
            header(HttpHeaders.Authorization, "Bearer ${authToken()}")
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        return response.decodeOrThrow()
    }

    private suspend inline fun <reified B : Any, reified T : Any> put(path: String, body: B): T {
        val response = client.put("$root$path") {
            header(HttpHeaders.Authorization, "Bearer ${authToken()}")
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        return response.decodeOrThrow()
    }

    private suspend inline fun <reified B : Any, reified T : Any> patch(path: String, body: B): T {
        val response = client.patch("$root$path") {
            header(HttpHeaders.Authorization, "Bearer ${authToken()}")
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        return response.decodeOrThrow()
    }

    private suspend fun delete(path: String) {
        val response = client.delete("$root$path") {
            header(HttpHeaders.Authorization, "Bearer ${authToken()}")
        }
        response.ensureSuccess()
    }

    suspend fun loadPortal() = get<ApiPortalInitial>("/portal/initial")
    suspend fun loadUsers() = get<List<ApiUser>>("/usuarios")
    suspend fun loadDonors() = get<List<ApiDonor>>("/donantes")
    suspend fun loadCities() = get<List<ApiCity>>("/ciudades")
    suspend fun loadEquipment() = get<List<ApiEquipment>>("/equipos")
    suspend fun loadPoints() = get<List<ApiPoint>>("/puntos-recoleccion")
    suspend fun loadStats() = get<ApiStats>("/stats/resumen")
    suspend fun loadAudits() = get<List<ApiAudit>>("/auditoria")
    suspend fun loadDiagnoses() = get<List<ApiDiagnosis>>("/diagnosticos")
    suspend fun loadRepairs() = get<List<ApiRepair>>("/reparaciones")
    suspend fun loadMessages(conversationId: Int) =
        get<List<ApiMessage>>("/portal/messages?conversacion_id=$conversationId")

    suspend fun createConversation(type: String, contactId: Int, equipmentId: Int? = null) =
        post<ApiStartConversation, Map<String, Int>>(
            "/portal/conversations",
            ApiStartConversation(type, contactId, equipmentId),
        )

    suspend fun sendMessage(conversationId: Int, content: String) =
        post<ApiSendMessage, Map<String, Boolean>>("/portal/messages", ApiSendMessage(conversationId, content))

    suspend fun publishEquipment(request: ApiPublishEquipment) =
        post<ApiPublishEquipment, ApiEquipment>("/portal/equipment", request)

    suspend fun createUser(request: ApiCreateUser) = post<ApiCreateUser, ApiUser>("/usuarios", request)
    suspend fun updateUser(id: Int, request: ApiUpdateUser) = put<ApiUpdateUser, Map<String, String>>("/usuarios/$id", request)
    suspend fun setUserActive(id: Int, active: Boolean) =
        patch<ApiUpdateUserState, Map<String, String>>("/usuarios/$id/estado", ApiUpdateUserState(active))
    suspend fun deleteUser(id: Int) = delete("/usuarios/$id")

    suspend fun createDonor(request: ApiDonorRequest) = post<ApiDonorRequest, ApiDonor>("/donantes", request)
    suspend fun updateDonor(id: Int, request: ApiDonorRequest) =
        put<ApiDonorRequest, Map<String, String>>("/donantes/$id", request)
    suspend fun deleteDonor(id: Int) = delete("/donantes/$id")

    suspend fun createPoint(request: ApiPointRequest) = post<ApiPointRequest, ApiPoint>("/puntos-recoleccion", request)
    suspend fun deletePoint(id: Int) = delete("/puntos-recoleccion/$id")

    suspend fun createDiagnosis(request: ApiDiagnosisRequest) =
        post<ApiDiagnosisRequest, ApiDiagnosis>("/diagnosticos", request)
    suspend fun createRepair(request: ApiRepairRequest) =
        post<ApiRepairRequest, ApiRepair>("/reparaciones", request)
}

private suspend inline fun <reified T : Any> HttpResponse.decodeOrThrow(): T {
    ensureSuccess()
    return body()
}

private suspend fun HttpResponse.ensureSuccess() {
    if (!status.isSuccess()) {
        val error = body<ErrorResponse>()
        throw IllegalStateException(error.error)
    }
}
