package com.example.ecotech

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction

object EquipoRepository {

    private fun ResultRow.toDonanteDto(ciudades: Map<Int, String> = emptyMap()) = DonanteDto(
        donanteId = this[Donantes.donanteId],
        tipo = this[Donantes.tipo],
        nombre = this[Donantes.nombre],
        email = this[Donantes.email],
        telefono = this[Donantes.telefono],
        ciudadId = this.getOrNull(Donantes.ciudadId),
        ciudadNombre = this.getOrNull(Donantes.ciudadId)?.let { ciudades[it] },
        direccion = this[Donantes.direccion],
        fechaRegistro = this.getOrNull(Donantes.fechaRegistro)?.formatEco(),
    )

    private fun ResultRow.toEquipoDto(tipos: Map<Int, String>, donantes: Map<Int, String>) = EquipoDto(
        equipoId = this[Equipos.equipoId],
        tipoId = this[Equipos.tipoId],
        tipoNombre = tipos[this[Equipos.tipoId]],
        donanteId = this.getOrNull(Equipos.donanteId),
        donanteNombre = this.getOrNull(Equipos.donanteId)?.let { donantes[it] },
        marca = this.getOrNull(Equipos.marca),
        modelo = this.getOrNull(Equipos.modelo),
        serial = this.getOrNull(Equipos.serial),
        estadoIngreso = this.getOrNull(Equipos.estadoIngreso),
        estadoActual = this[Equipos.estadoActual],
        descripcion = this.getOrNull(Equipos.descripcion),
        fechaRecepcion = this.getOrNull(Equipos.fechaRecepcion)?.formatEco(),
        usuarioId = this.getOrNull(Equipos.usuarioId),
    )

    // ---- Donantes ----

    fun donantes(): List<DonanteDto> = transaction {
        val ciudades = Ciudades.selectAll().associate { it[Ciudades.ciudadId] to it[Ciudades.nombre] }
        Donantes.selectAll().orderBy(Donantes.nombre).map { it.toDonanteDto(ciudades) }
    }

    fun donante(id: Int): DonanteDto? = transaction {
        val ciudades = Ciudades.selectAll().associate { it[Ciudades.ciudadId] to it[Ciudades.nombre] }
        Donantes.selectAll().where { Donantes.donanteId eq id }.singleOrNull()?.toDonanteDto(ciudades)
    }

    fun createDonante(req: DonanteRequest): DonanteDto? = transaction {
        val id = Donantes.insert {
            it[tipo] = req.tipo
            it[nombre] = req.nombre
            it[email] = req.email
            it[telefono] = req.telefono
            it[ciudadId] = req.ciudadId
            it[direccion] = req.direccion
            it[fechaRegistro] = now()
        } get Donantes.donanteId
        Audit.record("Donantes", "INSERT", id, "Donante creado: ${req.nombre}")
        donante(id)
    }

    fun updateDonante(id: Int, req: DonanteRequest): Boolean = transaction {
        val updated = Donantes.update({ Donantes.donanteId eq id }) {
            it[tipo] = req.tipo
            it[nombre] = req.nombre
            it[email] = req.email
            it[telefono] = req.telefono
            it[ciudadId] = req.ciudadId
            it[direccion] = req.direccion
        }
        if (updated > 0) Audit.record("Donantes", "UPDATE", id, "Donante actualizado")
        updated > 0
    }

    fun deleteDonante(id: Int): Boolean = transaction {
        val deleted = Donantes.deleteWhere { Donantes.donanteId eq id }
        if (deleted > 0) Audit.record("Donantes", "DELETE", id, "Donante eliminado")
        deleted > 0
    }

    // ---- Equipos ----

    fun equipos(): List<EquipoDto> = transaction {
        val tipos = TiposEquipo.selectAll().associate { it[TiposEquipo.tipoId] to it[TiposEquipo.nombre] }
        val donantes = Donantes.selectAll().associate { it[Donantes.donanteId] to it[Donantes.nombre] }
        Equipos.selectAll().orderBy(Equipos.equipoId).map { it.toEquipoDto(tipos, donantes) }
    }

    fun equipo(id: Int): EquipoDto? = transaction {
        val tipos = TiposEquipo.selectAll().associate { it[TiposEquipo.tipoId] to it[TiposEquipo.nombre] }
        val donantes = Donantes.selectAll().associate { it[Donantes.donanteId] to it[Donantes.nombre] }
        Equipos.selectAll().where { Equipos.equipoId eq id }.singleOrNull()?.toEquipoDto(tipos, donantes)
    }

    fun createEquipo(req: EquipoRequest): EquipoDto? = transaction {
        val id = Equipos.insert {
            it[tipoId] = req.tipoId
            it[donanteId] = req.donanteId
            it[marca] = req.marca
            it[modelo] = req.modelo
            it[serial] = req.serial
            it[estadoIngreso] = req.estadoIngreso
            it[estadoActual] = req.estadoActual
            it[descripcion] = req.descripcion
            it[fechaRecepcion] = now()
            it[usuarioId] = req.usuarioId
        } get Equipos.equipoId
        Audit.record("Equipos", "INSERT", id, "Equipo recibido: ${req.modelo ?: req.marca ?: "sin referencia"}")
        equipo(id)
    }

    fun updateEquipo(id: Int, req: EquipoRequest): Boolean = transaction {
        val updated = Equipos.update({ Equipos.equipoId eq id }) {
            it[tipoId] = req.tipoId
            if (req.donanteId != null) it[donanteId] = req.donanteId
            if (req.marca != null) it[marca] = req.marca
            if (req.modelo != null) it[modelo] = req.modelo
            if (req.serial != null) it[serial] = req.serial
            if (req.estadoIngreso != null) it[estadoIngreso] = req.estadoIngreso
            it[estadoActual] = req.estadoActual
            if (req.descripcion != null) it[descripcion] = req.descripcion
            if (req.usuarioId != null) it[usuarioId] = req.usuarioId
        }
        if (updated > 0) Audit.record("Equipos", "UPDATE", id, "Equipo actualizado")
        updated > 0
    }

    fun deleteEquipo(id: Int): Boolean = transaction {
        val deleted = Equipos.deleteWhere { Equipos.equipoId eq id }
        if (deleted > 0) Audit.record("Equipos", "DELETE", id, "Equipo eliminado")
        deleted > 0
    }
}