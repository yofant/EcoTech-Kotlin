package com.example.ecotech

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction

object PortalRepository {

    private fun ResultRow.toPointDto(cityNames: Map<Int, String> = emptyMap()) = PuntoRecoleccionDto(
        puntoId = this[PuntosRecoleccion.puntoId],
        nombre = this[PuntosRecoleccion.nombre],
        ciudadId = this.getOrNull(PuntosRecoleccion.ciudadId),
        ciudad = this.getOrNull(PuntosRecoleccion.ciudadId)?.let(cityNames::get),
        direccion = this[PuntosRecoleccion.direccion],
        horario = this[PuntosRecoleccion.horario],
        instrucciones = this.getOrNull(PuntosRecoleccion.instrucciones),
        activo = this[PuntosRecoleccion.activo],
    )

    private fun ResultRow.toMessageDto(senderNames: Map<Int, String>) = PortalMessageDto(
        mensajeId = this[Mensajes.mensajeId],
        emisorId = this[Mensajes.emisorId],
        emisorNombre = senderNames[this[Mensajes.emisorId]] ?: "Usuario",
        contenido = this[Mensajes.contenido],
        fechaEnvio = this[Mensajes.fechaEnvio].formatEco(),
    )

    fun initial(user: User): PortalInitialDto = transaction {
        val activeSellers = Usuarios.selectAll()
            .where { (Usuarios.role eq "Vendedor") and (Usuarios.active eq true) }
            .associate { it[Usuarios.id] to "${it[Usuarios.name]} ${it[Usuarios.lastName]}".trim() }

        val types = TiposEquipo.selectAll().orderBy(TiposEquipo.nombre)
            .map { TipoEquipoDto(it[TiposEquipo.tipoId], it[TiposEquipo.nombre], it.getOrNull(TiposEquipo.descripcion)) }
        val equipment = EquipoRepository.equipos()
            .filter { it.publicado && it.estadoActual.equals("Publicado", ignoreCase = true) }
            .filter { it.usuarioId in activeSellers }
            .take(100)
        val cityNames = Ciudades.selectAll().associate { it[Ciudades.ciudadId] to it[Ciudades.nombre] }
        val points = PuntosRecoleccion.selectAll()
            .where { PuntosRecoleccion.activo eq true }
            .orderBy(PuntosRecoleccion.nombre)
            .map { it.toPointDto(cityNames) }
        val operators = if (user.role == "Usuario" || user.role == "Vendedor") {
            Usuarios.selectAll()
                .where { (Usuarios.role eq "Operador") and (Usuarios.active eq true) }
                .orderBy(Usuarios.name)
                .map { ContactoDto(it[Usuarios.id], "${it[Usuarios.name]} ${it[Usuarios.lastName]}".trim()) }
        } else {
            emptyList()
        }
        PortalInitialDto(
            tipos = types,
            equipos = equipment,
            puntos = points,
            operadores = operators,
            conversaciones = conversationsFor(user),
        )
    }

    fun points(includeInactive: Boolean): List<PuntoRecoleccionDto> = transaction {
        val cityNames = Ciudades.selectAll().associate { it[Ciudades.ciudadId] to it[Ciudades.nombre] }
        val query = PuntosRecoleccion.selectAll()
        (if (includeInactive) query else query.where { PuntosRecoleccion.activo eq true })
            .orderBy(PuntosRecoleccion.nombre)
            .map { it.toPointDto(cityNames) }
    }

    fun createPoint(request: PuntoRecoleccionRequest): PuntoRecoleccionDto? = transaction {
        val id = PuntosRecoleccion.insert {
            it[nombre] = request.nombre
            it[ciudadId] = request.ciudadId
            it[direccion] = request.direccion
            it[horario] = request.horario
            it[instrucciones] = request.instrucciones
            it[activo] = true
            it[fechaCreacion] = now()
        } get PuntosRecoleccion.puntoId
        point(id)
    }

    fun deletePoint(id: Int): Boolean = transaction {
        PuntosRecoleccion.deleteWhere { PuntosRecoleccion.puntoId eq id } > 0
    }

    fun startConversation(user: User, request: StartConversationRequest): Int? = transaction {
        if (user.role !in setOf("Usuario", "Vendedor")) return@transaction null
        val target = UserRepository.findById(request.contactoId) ?: return@transaction null
        if (!target.active || target.id == user.id) return@transaction null

        val equipoId = when (request.tipo) {
            "vendedor" -> {
                if (target.role != "Vendedor") return@transaction null
                val id = request.equipoId ?: return@transaction null
                val equipment = Equipos.selectAll().where { Equipos.equipoId eq id }.singleOrNull()
                    ?: return@transaction null
                if (
                    equipment.getOrNull(Equipos.usuarioId) != target.id ||
                    !equipment[Equipos.publicado] ||
                    !equipment[Equipos.estadoActual].equals("Publicado", ignoreCase = true)
                ) return@transaction null
                id
            }
            "operador" -> {
                if (target.role != "Operador") return@transaction null
                null
            }
            else -> return@transaction null
        }

        val existing = Conversaciones.selectAll().where {
            (Conversaciones.usuarioId eq user.id) and
                (Conversaciones.interlocutorId eq target.id) and
                (Conversaciones.tipo eq request.tipo) and
                (if (equipoId == null) Conversaciones.equipoId.isNull() else Conversaciones.equipoId eq equipoId)
        }.singleOrNull()
        existing?.let {
            Conversaciones.update({ Conversaciones.conversacionId eq it[Conversaciones.conversacionId] }) {
                it[fechaActualizacion] = now()
            }
            return@transaction it[Conversaciones.conversacionId]
        }

        Conversaciones.insert {
            it[usuarioId] = user.id
            it[interlocutorId] = target.id
            it[tipo] = request.tipo
            it[Conversaciones.equipoId] = equipoId
            it[fechaCreacion] = now()
            it[fechaActualizacion] = now()
        } get Conversaciones.conversacionId
    }

    fun messages(user: User, conversationId: Int): List<PortalMessageDto>? = transaction {
        val conversation = authorizedConversation(user, conversationId) ?: return@transaction null
        val rows = Mensajes.selectAll()
            .where { Mensajes.conversacionId eq conversation[Conversaciones.conversacionId] }
            .orderBy(Mensajes.mensajeId)
            .toList()
        Mensajes.update({
            (Mensajes.conversacionId eq conversationId) and (Mensajes.emisorId neq user.id)
        }) {
            it[leido] = true
        }
        val latest = rows.takeLast(100)
        val senderNames = Usuarios.selectAll()
            .associate { it[Usuarios.id] to "${it[Usuarios.name]} ${it[Usuarios.lastName]}".trim() }
        latest.map { it.toMessageDto(senderNames) }
    }

    fun sendMessage(user: User, request: SendMessageRequest): Boolean = transaction {
        val conversation = authorizedConversation(user, request.conversacionId) ?: return@transaction false
        Mensajes.insert {
            it[conversacionId] = conversation[Conversaciones.conversacionId]
            it[emisorId] = user.id
            it[contenido] = request.contenido
            it[leido] = false
            it[fechaEnvio] = now()
        }
        Conversaciones.update({ Conversaciones.conversacionId eq request.conversacionId }) {
            it[fechaActualizacion] = now()
        }
        true
    }

    private fun conversationsFor(user: User): List<PortalConversationDto> {
        val conversations = Conversaciones.selectAll()
            .where {
                if (user.role == "Operador") {
                    (Conversaciones.interlocutorId eq user.id) and (Conversaciones.tipo eq "operador")
                } else {
                    (Conversaciones.usuarioId eq user.id) or (Conversaciones.interlocutorId eq user.id)
                }
            }
            .orderBy(Conversaciones.fechaActualizacion, org.jetbrains.exposed.sql.SortOrder.DESC)
            .toList()
        val users = Usuarios.selectAll().associateBy { it[Usuarios.id] }
        return conversations.map { row ->
            val isUser = row[Conversaciones.usuarioId] == user.id
            val contactId = if (isUser) row[Conversaciones.interlocutorId] else row[Conversaciones.usuarioId]
            val contact = users[contactId]
            val equipmentId = row.getOrNull(Conversaciones.equipoId)
            val equipment = equipmentId?.let { id -> Equipos.selectAll().where { Equipos.equipoId eq id }.singleOrNull() }
            val lastMessage = Mensajes.selectAll()
                .where { Mensajes.conversacionId eq row[Conversaciones.conversacionId] }
                .orderBy(Mensajes.mensajeId, org.jetbrains.exposed.sql.SortOrder.DESC)
                .limit(1)
                .singleOrNull()
            val unread = Mensajes.selectAll()
                .where {
                    (Mensajes.conversacionId eq row[Conversaciones.conversacionId]) and
                        (Mensajes.emisorId neq user.id) and (Mensajes.leido eq false)
                }
                .count().toInt()
            PortalConversationDto(
                conversacionId = row[Conversaciones.conversacionId],
                contactoId = contactId,
                contactoNombre = contact?.let { "${it[Usuarios.name]} ${it[Usuarios.lastName]}".trim() } ?: "Contacto",
                tipo = row[Conversaciones.tipo],
                equipoId = equipmentId,
                equipoMarca = equipment?.getOrNull(Equipos.marca),
                equipoModelo = equipment?.getOrNull(Equipos.modelo),
                ultimoMensaje = lastMessage?.getOrNull(Mensajes.contenido),
                ultimaFecha = lastMessage?.getOrNull(Mensajes.fechaEnvio)?.formatEco() ?: row[Conversaciones.fechaActualizacion].formatEco(),
                noLeidos = unread,
            )
        }
    }

    private fun authorizedConversation(user: User, conversationId: Int): ResultRow? {
        val conversation = Conversaciones.selectAll()
            .where { Conversaciones.conversacionId eq conversationId }
            .singleOrNull() ?: return null
        val isParticipant = conversation[Conversaciones.usuarioId] == user.id ||
            conversation[Conversaciones.interlocutorId] == user.id
        val operatorHasAssignment = user.role != "Operador" ||
            (conversation[Conversaciones.interlocutorId] == user.id && conversation[Conversaciones.tipo] == "operador")
        val portalRole = user.role in setOf("Usuario", "Vendedor", "Operador")
        return conversation.takeIf { isParticipant && operatorHasAssignment && portalRole }
    }

    private fun point(id: Int): PuntoRecoleccionDto? = transaction {
        val row = PuntosRecoleccion.selectAll()
            .where { PuntosRecoleccion.puntoId eq id }
            .singleOrNull() ?: return@transaction null
        val cities = Ciudades.selectAll().associate { it[Ciudades.ciudadId] to it[Ciudades.nombre] }
        row.toPointDto(cities)
    }
}
