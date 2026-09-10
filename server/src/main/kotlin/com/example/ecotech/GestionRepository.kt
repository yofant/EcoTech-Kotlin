package com.example.ecotech

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction

object GestionRepository {

    // ---- Diagnosticos ----

    private fun ResultRow.toDiagnosticoDto() = DiagnosticoDto(
        diagnosticoId = this[Diagnosticos.diagnosticoId],
        equipoId = this[Diagnosticos.equipoId],
        tecnicoId = this[Diagnosticos.tecnicoId],
        descripcion = this.getOrNull(Diagnosticos.descripcion),
        requiereReparacion = this[Diagnosticos.requiereReparacion],
        costoEstimado = this.getOrNull(Diagnosticos.costoEstimado)?.toDouble(),
        fecha = this.getOrNull(Diagnosticos.fecha)?.formatEco(),
    )

    fun diagnosticos(): List<DiagnosticoDto> = transaction {
        Diagnosticos.selectAll().orderBy(Diagnosticos.diagnosticoId).map { it.toDiagnosticoDto() }
    }

    fun diagnostico(id: Int): DiagnosticoDto? = transaction {
        Diagnosticos.selectAll().where { Diagnosticos.diagnosticoId eq id }.singleOrNull()?.toDiagnosticoDto()
    }

    fun createDiagnostico(req: DiagnosticoRequest): DiagnosticoDto? = transaction {
        val id = Diagnosticos.insert {
            it[equipoId] = req.equipoId
            it[tecnicoId] = req.tecnicoId
            it[descripcion] = req.descripcion
            it[requiereReparacion] = req.requiereReparacion
            it[costoEstimado] = req.costoEstimado?.toBigDecimal()
            it[fecha] = now()
        } get Diagnosticos.diagnosticoId
        Audit.record("Diagnosticos", "INSERT", id, "Diagnóstico creado para equipo ${req.equipoId}")
        diagnostico(id)
    }

    fun updateDiagnostico(id: Int, req: DiagnosticoRequest): Boolean = transaction {
        val updated = Diagnosticos.update({ Diagnosticos.diagnosticoId eq id }) {
            it[equipoId] = req.equipoId
            it[tecnicoId] = req.tecnicoId
            if (req.descripcion != null) it[descripcion] = req.descripcion
            it[requiereReparacion] = req.requiereReparacion
            if (req.costoEstimado != null) it[costoEstimado] = req.costoEstimado.toBigDecimal()
        }
        if (updated > 0) Audit.record("Diagnosticos", "UPDATE", id, "Diagnóstico actualizado")
        updated > 0
    }

    fun deleteDiagnostico(id: Int): Boolean = transaction {
        val deleted = Diagnosticos.deleteWhere { Diagnosticos.diagnosticoId eq id }
        if (deleted > 0) Audit.record("Diagnosticos", "DELETE", id, "Diagnóstico eliminado")
        deleted > 0
    }

    // ---- Reparaciones ----

    private fun ResultRow.toReparacionDto() = ReparacionDto(
        reparacionId = this[Reparaciones.reparacionId],
        equipoId = this[Reparaciones.equipoId],
        tecnicoId = this[Reparaciones.tecnicoId],
        descripcion = this.getOrNull(Reparaciones.descripcion),
        repuestosUsados = this.getOrNull(Reparaciones.repuestosUsados),
        costoReal = this.getOrNull(Reparaciones.costoReal)?.toDouble(),
        estado = this.getOrNull(Reparaciones.estado),
        fechaInicio = this.getOrNull(Reparaciones.fechaInicio)?.formatEco(),
        fechaFin = this.getOrNull(Reparaciones.fechaFin)?.formatEco(),
    )

    fun reparaciones(): List<ReparacionDto> = transaction {
        Reparaciones.selectAll().orderBy(Reparaciones.reparacionId).map { it.toReparacionDto() }
    }

    fun reparacion(id: Int): ReparacionDto? = transaction {
        Reparaciones.selectAll().where { Reparaciones.reparacionId eq id }.singleOrNull()?.toReparacionDto()
    }

    fun createReparacion(req: ReparacionRequest): ReparacionDto? = transaction {
        val id = Reparaciones.insert {
            it[equipoId] = req.equipoId
            it[tecnicoId] = req.tecnicoId
            it[descripcion] = req.descripcion
            it[repuestosUsados] = req.repuestosUsados
            it[costoReal] = req.costoReal?.toBigDecimal()
            it[estado] = req.estado
            it[fechaInicio] = now()
            it[fechaFin] = req.fechaFin.toLocalDateTimeOrNull()
        } get Reparaciones.reparacionId
        Audit.record("Reparaciones", "INSERT", id, "Reparación iniciada")
        reparacion(id)
    }

    fun updateReparacion(id: Int, req: ReparacionRequest): Boolean = transaction {
        val updated = Reparaciones.update({ Reparaciones.reparacionId eq id }) {
            it[equipoId] = req.equipoId
            it[tecnicoId] = req.tecnicoId
            if (req.descripcion != null) it[descripcion] = req.descripcion
            if (req.repuestosUsados != null) it[repuestosUsados] = req.repuestosUsados
            if (req.costoReal != null) it[costoReal] = req.costoReal.toBigDecimal()
            if (req.estado != null) it[estado] = req.estado
            if (req.fechaFin != null) it[fechaFin] = req.fechaFin.toLocalDateTimeOrNull()
        }
        if (updated > 0) Audit.record("Reparaciones", "UPDATE", id, "Reparación actualizada")
        updated > 0
    }

    fun deleteReparacion(id: Int): Boolean = transaction {
        val deleted = Reparaciones.deleteWhere { Reparaciones.reparacionId eq id }
        if (deleted > 0) Audit.record("Reparaciones", "DELETE", id, "Reparación eliminada")
        deleted > 0
    }

    // ---- Entregas ----

    private fun ResultRow.toEntregaDto() = EntregaDto(
        entregaId = this[Entregas.entregaId],
        equipoId = this[Entregas.equipoId],
        beneficiarioId = this[Entregas.beneficiarioId],
        usuarioId = this[Entregas.usuarioId],
        fechaEntrega = this.getOrNull(Entregas.fechaEntrega)?.formatEco(),
        condiciones = this.getOrNull(Entregas.condiciones),
        observaciones = this.getOrNull(Entregas.observaciones),
    )

    fun entregas(): List<EntregaDto> = transaction {
        Entregas.selectAll().orderBy(Entregas.entregaId).map { it.toEntregaDto() }
    }

    fun entrega(id: Int): EntregaDto? = transaction {
        Entregas.selectAll().where { Entregas.entregaId eq id }.singleOrNull()?.toEntregaDto()
    }

    fun createEntrega(req: EntregaRequest): EntregaDto? = transaction {
        val id = Entregas.insert {
            it[equipoId] = req.equipoId
            it[beneficiarioId] = req.beneficiarioId
            it[usuarioId] = req.usuarioId
            it[fechaEntrega] = now()
            it[condiciones] = req.condiciones
            it[observaciones] = req.observaciones
        } get Entregas.entregaId
        Audit.record("Entregas", "INSERT", id, "Entrega registrada")
        entrega(id)
    }

    fun updateEntrega(id: Int, req: EntregaRequest): Boolean = transaction {
        val updated = Entregas.update({ Entregas.entregaId eq id }) {
            it[equipoId] = req.equipoId
            it[beneficiarioId] = req.beneficiarioId
            it[usuarioId] = req.usuarioId
            if (req.condiciones != null) it[condiciones] = req.condiciones
            if (req.observaciones != null) it[observaciones] = req.observaciones
        }
        if (updated > 0) Audit.record("Entregas", "UPDATE", id, "Entrega actualizada")
        updated > 0
    }

    fun deleteEntrega(id: Int): Boolean = transaction {
        val deleted = Entregas.deleteWhere { Entregas.entregaId eq id }
        if (deleted > 0) Audit.record("Entregas", "DELETE", id, "Entrega eliminada")
        deleted > 0
    }
}