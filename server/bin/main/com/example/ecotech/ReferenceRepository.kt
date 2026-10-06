package com.example.ecotech

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction

object ReferenceRepository {

    private fun ResultRow.toCiudadDto() = CiudadDto(
        ciudadId = this[Ciudades.ciudadId],
        nombre = this[Ciudades.nombre],
        departamento = this[Ciudades.departamento],
    )

    private fun ResultRow.toTipoEquipoDto() = TipoEquipoDto(
        tipoId = this[TiposEquipo.tipoId],
        nombre = this[TiposEquipo.nombre],
        descripcion = this.getOrNull(TiposEquipo.descripcion),
    )

    // ---- Ciudades ----

    fun ciudades(): List<CiudadDto> = transaction {
        Ciudades.selectAll().orderBy(Ciudades.nombre).map { it.toCiudadDto() }
    }

    fun ciudad(id: Int): CiudadDto? = transaction {
        Ciudades.selectAll().where { Ciudades.ciudadId eq id }.singleOrNull()?.toCiudadDto()
    }

    fun createCiudad(req: CiudadRequest): CiudadDto? = transaction {
        val id = Ciudades.insert {
            it[nombre] = req.nombre
            it[departamento] = req.departamento
        } get Ciudades.ciudadId
        Audit.record("Ciudades", "INSERT", id, "Ciudad creada: ${req.nombre}")
        ciudad(id)
    }

    fun updateCiudad(id: Int, req: CiudadRequest): Boolean = transaction {
        val updated = Ciudades.update({ Ciudades.ciudadId eq id }) {
            it[nombre] = req.nombre
            it[departamento] = req.departamento
        }
        if (updated > 0) Audit.record("Ciudades", "UPDATE", id, "Ciudad actualizada")
        updated > 0
    }

    fun deleteCiudad(id: Int): Boolean = transaction {
        val deleted = Ciudades.deleteWhere { Ciudades.ciudadId eq id }
        if (deleted > 0) Audit.record("Ciudades", "DELETE", id, "Ciudad eliminada")
        deleted > 0
    }

    // ---- TiposEquipo ----

    fun tiposEquipo(): List<TipoEquipoDto> = transaction {
        TiposEquipo.selectAll().orderBy(TiposEquipo.nombre).map { it.toTipoEquipoDto() }
    }

    fun tipoEquipo(id: Int): TipoEquipoDto? = transaction {
        TiposEquipo.selectAll().where { TiposEquipo.tipoId eq id }.singleOrNull()?.toTipoEquipoDto()
    }

    fun createTipoEquipo(req: TipoEquipoRequest): TipoEquipoDto? = transaction {
        val id = TiposEquipo.insert {
            it[nombre] = req.nombre
            it[descripcion] = req.descripcion
        } get TiposEquipo.tipoId
        Audit.record("TiposEquipo", "INSERT", id, "Tipo de equipo creado: ${req.nombre}")
        tipoEquipo(id)
    }

    fun updateTipoEquipo(id: Int, req: TipoEquipoRequest): Boolean = transaction {
        val updated = TiposEquipo.update({ TiposEquipo.tipoId eq id }) {
            it[nombre] = req.nombre
            it[descripcion] = req.descripcion
        }
        if (updated > 0) Audit.record("TiposEquipo", "UPDATE", id, "Tipo de equipo actualizado")
        updated > 0
    }

    fun deleteTipoEquipo(id: Int): Boolean = transaction {
        val deleted = TiposEquipo.deleteWhere { TiposEquipo.tipoId eq id }
        if (deleted > 0) Audit.record("TiposEquipo", "DELETE", id, "Tipo de equipo eliminado")
        deleted > 0
    }
}