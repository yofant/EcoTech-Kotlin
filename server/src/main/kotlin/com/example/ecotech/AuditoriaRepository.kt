package com.example.ecotech

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.YearMonth

object Audit {

    fun record(tabla: String, operacion: String, registroId: Int?, detalle: String?) {
        runCatching {
            transaction {
                Auditorias.insert {
                    it[Auditorias.tablaAfectada] = tabla
                    it[Auditorias.operacion] = operacion
                    it[Auditorias.registroId] = registroId
                    it[Auditorias.usuarioSql] = "ecotech-api"
                    it[Auditorias.fecha] = now()
                    it[Auditorias.detalle] = detalle
                }
            }
        }
    }

    fun all(): List<AuditoriaDto> = transaction {
        Auditorias.selectAll().orderBy(Auditorias.auditoriaId).map { row ->
            row.toAuditoriaDto()
        }
    }
}

private fun ResultRow.toAuditoriaDto() = AuditoriaDto(
    auditoriaId = this[Auditorias.auditoriaId],
    tablaAfectada = this[Auditorias.tablaAfectada],
    operacion = this[Auditorias.operacion],
    registroId = this.getOrNull(Auditorias.registroId),
    usuarioSql = this.getOrNull(Auditorias.usuarioSql),
    fecha = this.getOrNull(Auditorias.fecha)?.formatEco(),
    detalle = this.getOrNull(Auditorias.detalle),
    valoresAnteriores = this.getOrNull(Auditorias.valoresAnteriores),
    valoresNuevos = this.getOrNull(Auditorias.valoresNuevos),
)

object StatsRepository {

    fun resumen(): StatsDto = transaction {
        val nowDt = LocalDateTime.now()
        val startOfMonth = nowDt.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)

        val totalUsuarios = Usuarios.selectAll().count().toInt()
        val totalEquipos = Equipos.selectAll().count().toInt()
        val totalBeneficiarios = Beneficiarios.selectAll().count().toInt()
        val totalDonantes = Donantes.selectAll().count().toInt()
        val totalReparaciones = Reparaciones.selectAll().count().toInt()
        val totalEntregas = Entregas.selectAll().count().toInt()

        val entregasDelMes = Entregas.selectAll()
            .where { Entregas.fechaEntrega greaterEq startOfMonth }
            .count().toInt()

        val recoleccionesDelMes = Equipos.selectAll()
            .where { Equipos.fechaRecepcion greaterEq startOfMonth }
            .count().toInt()

        val countCol = Equipos.equipoId.count()
        val equiposPorEstado = Equipos
            .select(listOf(Equipos.estadoActual, countCol))
            .groupBy(Equipos.estadoActual)
            .map { LabelValue(label = it[Equipos.estadoActual], value = it[countCol].toInt()) }

        StatsDto(
            totalUsuarios = totalUsuarios,
            totalEquipos = totalEquipos,
            equiposPorEstado = equiposPorEstado,
            totalBeneficiarios = totalBeneficiarios,
            totalDonantes = totalDonantes,
            totalReparaciones = totalReparaciones,
            totalEntregas = totalEntregas,
            entregasDelMes = entregasDelMes,
            recoleccionesDelMes = recoleccionesDelMes,
            co2EstimadoKg = totalEquipos * 51.0,
        )
    }

    fun actividadMensual(): List<ActividadMensual> = transaction {
        val conteos = mutableMapOf<String, IntArray>()
        fun cuenta(sql: String, index: Int) {
            exec(sql) { rs ->
                while (rs.next()) {
                    val mes = rs.getString("mes") ?: return@exec
                    conteos.getOrPut(mes) { IntArray(3) }[index] = rs.getInt("cnt")
                }
            }
        }
        cuenta("SELECT DATE_FORMAT(fecha_entrega, '%Y-%m') AS mes, COUNT(*) AS cnt FROM Entregas GROUP BY mes", 0)
        cuenta("SELECT DATE_FORMAT(fecha_inicio, '%Y-%m') AS mes, COUNT(*) AS cnt FROM Reparaciones GROUP BY mes", 1)
        cuenta("SELECT DATE_FORMAT(fecha_recepcion, '%Y-%m') AS mes, COUNT(*) AS cnt FROM Equipos GROUP BY mes", 2)

        val actual = YearMonth.now()
        (5 downTo 0).map { offset ->
            val ym = actual.minusMonths(offset.toLong())
            val key = "%04d-%02d".format(ym.year, ym.monthValue)
            val valores = conteos[key] ?: IntArray(3)
            ActividadMensual(
                mes = key,
                entregas = valores[0],
                reparaciones = valores[1],
                recolecciones = valores[2],
            )
        }
    }
}