package com.example.ecotech

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val TIMESTAMP_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun LocalDateTime.formatEco(): String = this.format(TIMESTAMP_FORMAT)

fun String?.toLocalDateTimeOrNull(): LocalDateTime? =
    this?.let {
        runCatching { LocalDateTime.parse(it, TIMESTAMP_FORMAT) }.getOrNull()
    }

@Serializable
data class ApiInfo(
    val name: String,
    val status: String,
    val version: String,
)

@Serializable
data class HealthDto(
    val status: String,
    val database: Boolean,
)

@Serializable
data class CiudadDto(val ciudadId: Int, val nombre: String, val departamento: String)

@Serializable
data class CiudadRequest(val nombre: String, val departamento: String)

@Serializable
data class TipoEquipoDto(val tipoId: Int, val nombre: String, val descripcion: String? = null)

@Serializable
data class TipoEquipoRequest(val nombre: String, val descripcion: String? = null)

@Serializable
data class DonanteDto(
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
data class DonanteRequest(
    val tipo: String,
    val nombre: String,
    val email: String,
    val telefono: String,
    val ciudadId: Int? = null,
    val direccion: String,
)

@Serializable
data class EquipoDto(
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
)

@Serializable
data class EquipoRequest(
    val tipoId: Int,
    val donanteId: Int? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val serial: String? = null,
    val estadoIngreso: String? = null,
    val estadoActual: String,
    val descripcion: String? = null,
    val usuarioId: Int? = null,
)

@Serializable
data class BeneficiarioDto(
    val beneficiarioId: Int,
    val nombre: String,
    val apellido: String,
    val documento: String,
    val email: String,
    val telefono: String,
    val ciudadId: Int? = null,
    val ciudadNombre: String? = null,
    val direccion: String,
    val estrato: Int? = null,
    val necesidad: String? = null,
    val fechaRegistro: String? = null,
)

@Serializable
data class BeneficiarioRequest(
    val nombre: String,
    val apellido: String,
    val documento: String,
    val email: String,
    val telefono: String,
    val ciudadId: Int? = null,
    val direccion: String,
    val estrato: Int? = null,
    val necesidad: String? = null,
)

@Serializable
data class DiagnosticoDto(
    val diagnosticoId: Int,
    val equipoId: Int,
    val tecnicoId: Int,
    val descripcion: String? = null,
    val requiereReparacion: Boolean = true,
    val costoEstimado: Double? = null,
    val fecha: String? = null,
)

@Serializable
data class DiagnosticoRequest(
    val equipoId: Int,
    val tecnicoId: Int,
    val descripcion: String? = null,
    val requiereReparacion: Boolean = true,
    val costoEstimado: Double? = null,
)

@Serializable
data class ReparacionDto(
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
data class ReparacionRequest(
    val equipoId: Int,
    val tecnicoId: Int,
    val descripcion: String? = null,
    val repuestosUsados: String? = null,
    val costoReal: Double? = null,
    val estado: String? = null,
    val fechaFin: String? = null,
)

@Serializable
data class EntregaDto(
    val entregaId: Int,
    val equipoId: Int,
    val beneficiarioId: Int,
    val usuarioId: Int,
    val fechaEntrega: String? = null,
    val condiciones: String? = null,
    val observaciones: String? = null,
)

@Serializable
data class EntregaRequest(
    val equipoId: Int,
    val beneficiarioId: Int,
    val usuarioId: Int,
    val condiciones: String? = null,
    val observaciones: String? = null,
)

@Serializable
data class AdminUserDto(
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
data class UpdateUserRequest(
    val nombre: String? = null,
    val apellido: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    val rol: String? = null,
)

@Serializable
data class AuditoriaDto(
    val auditoriaId: Int,
    val tablaAfectada: String,
    val operacion: String,
    val registroId: Int? = null,
    val usuarioSql: String? = null,
    val fecha: String? = null,
    val detalle: String? = null,
)

@Serializable
data class LabelValue(val label: String, val value: Int)

@Serializable
data class ActividadMensual(
    val mes: String,
    val entregas: Int,
    val reparaciones: Int,
    val recolecciones: Int,
)

@Serializable
data class StatsDto(
    val totalUsuarios: Int,
    val totalEquipos: Int,
    val equiposPorEstado: List<LabelValue>,
    val totalBeneficiarios: Int,
    val totalDonantes: Int,
    val totalReparaciones: Int,
    val totalEntregas: Int,
    val entregasDelMes: Int,
    val recoleccionesDelMes: Int,
    val co2EstimadoKg: Double,
)