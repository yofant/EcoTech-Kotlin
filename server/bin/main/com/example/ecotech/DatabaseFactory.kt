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
./gradlew :server:run
object DatabaseFactory {

    @Volatile
    var isConnected: Boolean = false
        private set

    fun init(throwOnError: Boolean = false) {
        val host = System.getenv("DB_HOST") ?: System.getProperty("DB_HOST") ?: "54.242.212.118"
        val port = System.getenv("DB_PORT") ?: System.getProperty("DB_PORT") ?: "3306"
        val databaseName = System.getenv("DB_NAME") ?: System.getProperty("DB_NAME") ?: "ecotech"
        val user = System.getenv("DB_USER") ?: System.getProperty("DB_USER") ?: "admin"
        val password = System.getenv("DB_PASSWORD") ?: System.getProperty("DB_PASSWORD") ?: "EcoTech"

        println("=========================================================")
        println("📡 Conectando a MySQL ($host:$port/$databaseName)...")
        println("   - Host: $host")
        println("   - Puerto: $port")
        println("   - Base de Datos: $databaseName")
        println("   - Usuario: $user")
        println("=========================================================")

        try {
            val config = HikariConfig().apply {
                jdbcUrl = "jdbc:mysql://$host:$port/$databaseName?useSSL=false&allowPublicKeyRetrieval=true&autoReconnect=true&serverTimezone=UTC"
                driverClassName = "com.mysql.cj.jdbc.Driver"
                username = user
                this.password = password
                maximumPoolSize = 10
                connectionTimeout = 5000 // 5 segundos maximo antes de timeout
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
                    AuthSessions,
                    Conversaciones,
                    Mensajes,
                    PuntosRecoleccion,
                )
            }

            println("✅ Conexión a la base de datos establecida con éxito.")
            println("✅ Tablas sincronizadas e inicializadas correctamente.")

            seedIfEmpty()
            isConnected = true
        } catch (e: Exception) {
            isConnected = false
            println("⚠️ Advertencia: No se pudo conectar con la base de datos MySQL ($host:$port/$databaseName):")
            println("   Detalle: ${e.message}")
            println("   Sugerencia: Puedes sobreescribir DB_HOST, DB_PORT, DB_NAME, DB_USER y DB_PASSWORD.")
            println("   El servidor continuará en ejecución. Consulta /api/health para verificar conectividad.")
            if (throwOnError) throw e
        }
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

            if (Usuarios.selectAll().empty()) {
                Usuarios.insert {
                    it[name] = "Yofan"
                    it[lastName] = "Tellez"
                    it[email] = "yojantellez8@gmail.com"
                    it[phone] = "3001234567"
                    it[role] = "Administrador"
                    it[active] = true
                    it[registrationDate] = now()
                    it[passwordHash] = "61a66994d1c3805ccdee514aa7a9cc55936dd1e97203b76932b74832287a7702" // Clave inicial: EcoTech2026!
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
    val publicado = bool("publicado").default(false)

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
    val operacion = varchar("operacion", 50)
    val registroId = integer("registro_id").nullable()
    val usuarioSql = varchar("usuario_sql", 100).nullable()
    val fecha = datetime("fecha")
    val detalle = text("detalle").nullable()
    val valoresAnteriores = text("valores_anteriores").nullable()
    val valoresNuevos = text("valores_nuevos").nullable()

    override val primaryKey = PrimaryKey(auditoriaId)
}

object AuthSessions : Table("AppSessions") {
    val sessionId = integer("session_id").autoIncrement()
    val usuarioId = integer("usuario_id").references(Usuarios.id).index()
    val tokenHash = varchar("token_hash", 64).uniqueIndex()
    val expiresAt = datetime("expires_at")

    override val primaryKey = PrimaryKey(sessionId)
}

object Conversaciones : Table("Conversaciones") {
    val conversacionId = integer("conversacion_id").autoIncrement()
    val usuarioId = integer("usuario_id").references(Usuarios.id)
    val interlocutorId = integer("interlocutor_id").references(Usuarios.id)
    val tipo = varchar("tipo", 20)
    val equipoId = integer("equipo_id").references(Equipos.equipoId).nullable()
    val equipoContexto = integer("equipo_contexto").default(0)
    val fechaCreacion = datetime("fecha_creacion")
    val fechaActualizacion = datetime("fecha_actualizacion")

    override val primaryKey = PrimaryKey(conversacionId)
}

object Mensajes : Table("Mensajes") {
    val mensajeId = long("mensaje_id").autoIncrement()
    val conversacionId = integer("conversacion_id").references(Conversaciones.conversacionId)
    val emisorId = integer("emisor_id").references(Usuarios.id)
    val contenido = text("contenido")
    val leido = bool("leido").default(false)
    val fechaEnvio = datetime("fecha_envio")

    override val primaryKey = PrimaryKey(mensajeId)
}

object PuntosRecoleccion : Table("PuntosRecoleccion") {
    val puntoId = integer("punto_id").autoIncrement()
    val nombre = varchar("nombre", 120)
    val ciudadId = integer("ciudad_id").references(Ciudades.ciudadId).nullable()
    val direccion = varchar("direccion", 250)
    val horario = varchar("horario", 150)
    val instrucciones = varchar("instrucciones", 250).nullable()
    val activo = bool("activo").default(true)
    val fechaCreacion = datetime("fecha_creacion")

    override val primaryKey = PrimaryKey(puntoId)
}

fun now() = LocalDateTime.now()
