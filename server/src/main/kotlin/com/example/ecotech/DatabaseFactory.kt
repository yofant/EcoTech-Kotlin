package com.example.ecotech

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object DatabaseFactory {

    fun init() {
        val host = System.getenv("DB_HOST") ?: "localhost"
        val port = System.getenv("DB_PORT") ?: "3306"
        val databaseName = System.getenv("DB_NAME") ?: "EcoTech"
        val user = System.getenv("DB_USER") ?: "root"
        val password = System.getenv("DB_PASSWORD") ?: ""

        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:mysql://$host:$port/$databaseName?useSSL=false&connectionTimeZone=LOCAL&allowPublicKeyRetrieval=true"
            driverClassName = "com.mysql.cj.jdbc.Driver"
            username = user
            this.password = password
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        Database.connect(HikariDataSource(config))

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Ciudades,
                TiposEquipo,
                Donantes,
                Usuarios,
                Beneficiarios,
                Equipos,
                Diagnosticos,
                Reparaciones,
                Entregas,
                Auditorias,
            )
        }

        seedIfEmpty()
    }

    private fun seedIfEmpty() {
        transaction {
            if (Ciudades.selectAll().empty()) {
                Ciudades.batchInsert(
                    listOf(
                        "Bogotá" to "Cundinamarca",
                        "Medellín" to "Antioquia",
                        "Cali" to "Valle del Cauca",
                        "Barranquilla" to "Atlántico",
                        "Cartagena" to "Bolívar",
                    ),
                ) { (nombreCiudad, departamentoVal) ->
                    this[Ciudades.nombre] = nombreCiudad
                    this[Ciudades.departamento] = departamentoVal
                }
            }

            if (TiposEquipo.selectAll().empty()) {
                TiposEquipo.batchInsert(
                    listOf(
                        "Computador de escritorio" to "Equipos de escritorio",
                        "Portátil" to "Equipos móviles",
                        "Celular" to "Smartphones",
                        "Tablet" to "Tabletas",
                        "Impresora" to "Periféricos de impresión",
                        "Monitor" to "Pantallas",
                        "Periférico" to "Teclados, ratones, etc.",
                    ),
                ) { (nombreTipo, descripcionTipo) ->
                    this[TiposEquipo.nombre] = nombreTipo
                    this[TiposEquipo.descripcion] = descripcionTipo
                }
            }
        }
    }
}

object Ciudades : Table("Ciudades") {
    val ciudadId = integer("ciudad_id").autoIncrement()
    val nombre = varchar("nombre", 100)
    val departamento = varchar("departamento", 100)

    override val primaryKey = PrimaryKey(ciudadId)
}

object TiposEquipo : Table("TiposEquipo") {
    val tipoId = integer("tipo_id").autoIncrement()
    val nombre = varchar("nombre", 100)
    val descripcion = varchar("descripcion", 250).nullable()

    override val primaryKey = PrimaryKey(tipoId)
}

object Donantes : Table("Donantes") {
    val donanteId = integer("donante_id").autoIncrement()
    val tipo = varchar("tipo", 20)
    val nombre = varchar("nombre", 150)
    val email = varchar("email", 150)
    val telefono = varchar("telefono", 20)
    val ciudadId = integer("ciudad_id").references(Ciudades.ciudadId).nullable()
    val direccion = varchar("direccion", 250)
    val fechaRegistro = datetime("fecha_registro")

    override val primaryKey = PrimaryKey(donanteId)
}

object Usuarios : Table("Usuarios") {
    val id = integer("usuario_id").autoIncrement()
    val name = varchar("nombre", 100)
    val lastName = varchar("apellido", 100)
    val email = varchar("email", 150).uniqueIndex()
    val phone = varchar("telefono", 20)
    val role = varchar("rol", 50)
    val active = bool("activo").default(true)
    val registrationDate = datetime("fecha_registro")
    val passwordHash = varchar("password_hash", 255)

    override val primaryKey = PrimaryKey(id)
}

object Beneficiarios : Table("Beneficiarios") {
    val beneficiarioId = integer("beneficiario_id").autoIncrement()
    val nombre = varchar("nombre", 100)
    val apellido = varchar("apellido", 100)
    val documento = varchar("documento", 20).uniqueIndex()
    val email = varchar("email", 150)
    val telefono = varchar("telefono", 20)
    val ciudadId = integer("ciudad_id").references(Ciudades.ciudadId).nullable()
    val direccion = varchar("direccion", 250)
    val estrato = integer("estrato").nullable()
    val necesidad = text("necesidad").nullable()
    val fechaRegistro = datetime("fecha_registro")

    override val primaryKey = PrimaryKey(beneficiarioId)
}

object Equipos : Table("Equipos") {
    val equipoId = integer("equipo_id").autoIncrement()
    val tipoId = integer("tipo_id").references(TiposEquipo.tipoId)
    val donanteId = integer("donante_id").references(Donantes.donanteId).nullable()
    val marca = varchar("marca", 100).nullable()
    val modelo = varchar("modelo", 100).nullable()
    val serial = varchar("serial", 100).nullable().uniqueIndex()
    val estadoIngreso = varchar("estado_ingreso", 50).nullable()
    val estadoActual = varchar("estado_actual", 50)
    val descripcion = varchar("descripcion", 200).nullable()
    val fechaRecepcion = datetime("fecha_recepcion")
    val usuarioId = integer("usuario_id").references(Usuarios.id).nullable()

    override val primaryKey = PrimaryKey(equipoId)
}

object Diagnosticos : Table("Diagnosticos") {
    val diagnosticoId = integer("diagnostico_id").autoIncrement()
    val equipoId = integer("equipo_id").references(Equipos.equipoId)
    val tecnicoId = integer("tecnico_id").references(Usuarios.id)
    val descripcion = varchar("descripcion", 250).nullable()
    val requiereReparacion = bool("requiere_repara").default(true)
    val costoEstimado = decimal("costo_estimado", 18, 2).nullable()
    val fecha = datetime("fecha")

    override val primaryKey = PrimaryKey(diagnosticoId)
}

object Reparaciones : Table("Reparaciones") {
    val reparacionId = integer("reparacion_id").autoIncrement()
    val equipoId = integer("equipo_id").references(Equipos.equipoId)
    val tecnicoId = integer("tecnico_id").references(Usuarios.id)
    val descripcion = varchar("descripcion", 250).nullable()
    val repuestosUsados = varchar("repuestos_usados", 100).nullable()
    val costoReal = decimal("costo_real", 18, 2).nullable()
    val estado = varchar("estado", 50).nullable()
    val fechaInicio = datetime("fecha_inicio")
    val fechaFin = datetime("fecha_fin").nullable()

    override val primaryKey = PrimaryKey(reparacionId)
}

object Entregas : Table("Entregas") {
    val entregaId = integer("entrega_id").autoIncrement()
    val equipoId = integer("equipo_id").references(Equipos.equipoId)
    val beneficiarioId = integer("beneficiario_id").references(Beneficiarios.beneficiarioId)
    val usuarioId = integer("usuario_id").references(Usuarios.id)
    val fechaEntrega = datetime("fecha_entrega")
    val condiciones = varchar("condiciones", 100).nullable()
    val observaciones = varchar("observaciones", 100).nullable()

    override val primaryKey = PrimaryKey(entregaId)
}

object Auditorias : Table("Auditoria") {
    val auditoriaId = integer("auditoria_id").autoIncrement()
    val tablaAfectada = varchar("tabla_afectada", 100)
    val operacion = varchar("operacion", 15)
    val registroId = integer("registro_id").nullable()
    val usuarioSql = varchar("usuario_sql", 100).nullable()
    val fecha = datetime("fecha")
    val detalle = varchar("detalle", 100).nullable()

    override val primaryKey = PrimaryKey(auditoriaId)
}

fun now() = LocalDateTime.now()