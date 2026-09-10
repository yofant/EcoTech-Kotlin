package com.example.ecotech

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction

object BeneficiarioRepository {

    private fun ResultRow.toBeneficiarioDto(ciudades: Map<Int, String> = emptyMap()) = BeneficiarioDto(
        beneficiarioId = this[Beneficiarios.beneficiarioId],
        nombre = this[Beneficiarios.nombre],
        apellido = this[Beneficiarios.apellido],
        documento = this[Beneficiarios.documento],
        email = this[Beneficiarios.email],
        telefono = this[Beneficiarios.telefono],
        ciudadId = this.getOrNull(Beneficiarios.ciudadId),
        ciudadNombre = this.getOrNull(Beneficiarios.ciudadId)?.let { ciudades[it] },
        direccion = this[Beneficiarios.direccion],
        estrato = this.getOrNull(Beneficiarios.estrato),
        necesidad = this.getOrNull(Beneficiarios.necesidad),
        fechaRegistro = this.getOrNull(Beneficiarios.fechaRegistro)?.formatEco(),
    )

    fun all(): List<BeneficiarioDto> = transaction {
        val ciudades = Ciudades.selectAll().associate { it[Ciudades.ciudadId] to it[Ciudades.nombre] }
        Beneficiarios.selectAll().orderBy(Beneficiarios.apellido).map { it.toBeneficiarioDto(ciudades) }
    }

    fun findById(id: Int): BeneficiarioDto? = transaction {
        val ciudades = Ciudades.selectAll().associate { it[Ciudades.ciudadId] to it[Ciudades.nombre] }
        Beneficiarios.selectAll().where { Beneficiarios.beneficiarioId eq id }.singleOrNull()?.toBeneficiarioDto(ciudades)
    }

    fun create(req: BeneficiarioRequest): BeneficiarioDto? = transaction {
        val id = Beneficiarios.insert {
            it[nombre] = req.nombre
            it[apellido] = req.apellido
            it[documento] = req.documento
            it[email] = req.email
            it[telefono] = req.telefono
            it[ciudadId] = req.ciudadId
            it[direccion] = req.direccion
            it[estrato] = req.estrato
            it[necesidad] = req.necesidad
            it[fechaRegistro] = now()
        } get Beneficiarios.beneficiarioId
        Audit.record("Beneficiarios", "INSERT", id, "Beneficiario creado: ${req.nombre}")
        findById(id)
    }

    fun update(id: Int, req: BeneficiarioRequest): Boolean = transaction {
        val updated = Beneficiarios.update({ Beneficiarios.beneficiarioId eq id }) {
            it[nombre] = req.nombre
            it[apellido] = req.apellido
            it[documento] = req.documento
            it[email] = req.email
            it[telefono] = req.telefono
            it[ciudadId] = req.ciudadId
            it[direccion] = req.direccion
            it[estrato] = req.estrato
            it[necesidad] = req.necesidad
        }
        if (updated > 0) Audit.record("Beneficiarios", "UPDATE", id, "Beneficiario actualizado")
        updated > 0
    }

    fun delete(id: Int): Boolean = transaction {
        val deleted = Beneficiarios.deleteWhere { Beneficiarios.beneficiarioId eq id }
        if (deleted > 0) Audit.record("Beneficiarios", "DELETE", id, "Beneficiario eliminado")
        deleted > 0
    }
}