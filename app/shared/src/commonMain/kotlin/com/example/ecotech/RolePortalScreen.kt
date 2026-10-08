package com.example.ecotech

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

private val adminSections = listOf("Resumen", "Usuarios", "Donantes", "Operaciones", "Estados", "Puntos de entrega")
private val auditorSections = listOf("Resumen", "Registro de auditoría")
private val operatorSections = listOf("Bandeja de solicitudes")
private val technicianSections = listOf("Resumen", "Inventario", "Mi actividad")
private val customerSections = listOf("Inicio", "Explorar equipos", "Puntos de entrega", "Mis conversaciones")

@Composable
fun RolePortalScreen(user: UserResponse?, onLogout: () -> Unit) {
    val role = user?.role ?: "Usuario"
    val sections = when (role) {
        "Administrador" -> adminSections
        "Auditor" -> auditorSections
        "Operador" -> operatorSections
        "Tecnico" -> technicianSections
        else -> if (role == "Vendedor") customerSections + "Publicar un equipo" else customerSections
    }
    val scope = rememberCoroutineScope()
    var section by remember(role) { mutableStateOf(sections.first()) }
    var loading by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var logoutBusy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var logoutError by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }

    var portal by remember { mutableStateOf(ApiPortalInitial()) }
    var users by remember { mutableStateOf<List<ApiUser>>(emptyList()) }
    var donors by remember { mutableStateOf<List<ApiDonor>>(emptyList()) }
    var cities by remember { mutableStateOf<List<ApiCity>>(emptyList()) }
    var equipment by remember { mutableStateOf<List<ApiEquipment>>(emptyList()) }
    var points by remember { mutableStateOf<List<ApiPoint>>(emptyList()) }
    var stats by remember { mutableStateOf<ApiStats?>(null) }
    var audits by remember { mutableStateOf<List<ApiAudit>>(emptyList()) }
    var diagnoses by remember { mutableStateOf<List<ApiDiagnosis>>(emptyList()) }
    var repairs by remember { mutableStateOf<List<ApiRepair>>(emptyList()) }
    var selectedConversationId by remember { mutableStateOf<Int?>(null) }
    var messages by remember { mutableStateOf<List<ApiMessage>>(emptyList()) }

    suspend fun reload() {
        loading = true
        error = null
        try {
            when (role) {
                "Administrador" -> {
                    stats = PortalApi.loadStats()
                    users = PortalApi.loadUsers()
                    donors = PortalApi.loadDonors()
                    cities = PortalApi.loadCities()
                    equipment = PortalApi.loadEquipment()
                    diagnoses = PortalApi.loadDiagnoses()
                    repairs = PortalApi.loadRepairs()
                    points = PortalApi.loadPoints()
                }
                "Auditor" -> {
                    stats = PortalApi.loadStats()
                    audits = PortalApi.loadAudits()
                }
                "Tecnico" -> {
                    equipment = PortalApi.loadEquipment()
                    diagnoses = PortalApi.loadDiagnoses()
                    repairs = PortalApi.loadRepairs()
                }
                else -> portal = PortalApi.loadPortal()
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (exception: Exception) {
            error = exception.message ?: "No se pudieron cargar los datos del panel."
        } finally {
            loading = false
        }
    }

    fun perform(action: suspend () -> Unit) {
        scope.launch {
            busy = true
            error = null
            notice = null
            try {
                action()
                notice = "Cambios guardados correctamente."
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                error = exception.message ?: "No se pudo completar la operación."
            } finally {
                busy = false
            }
        }
    }

    LaunchedEffect(role) { reload() }
    LaunchedEffect(selectedConversationId) {
        val id = selectedConversationId ?: return@LaunchedEffect
        try {
            messages = PortalApi.loadMessages(id)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (exception: Exception) {
            error = exception.message ?: "No se pudo cargar la conversación."
        }
    }

    EcoBackground {
        EcoRoleHero(
            kicker = roleLabel(role),
            title = "Hola, ${user?.name ?: roleLabel(role)}",
            subtitle = portalSubtitle(role),
            icon = roleIcon(role),
        )

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            sections.forEach { item ->
                PortalTab(label = item, selected = section == item) { section = item }
            }
        }

        error?.let {
            EcoSectionCard {
                Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
            }
        }
        notice?.let {
            EcoSectionCard { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium) }
        }
        if (loading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            when (role) {
                "Administrador" -> AdminPortalSection(
                    section, stats, users, donors, cities, equipment, diagnoses, repairs, points, busy,
                    onAction = ::perform,
                    onReload = { reload() },
                )
                "Auditor" -> AuditorPortalSection(section, stats, audits, onViewRecords = { section = "Registro de auditoría" })
                "Operador" -> ConversationPortalSection(
                    portal.conversaciones, selectedConversationId, messages, user?.id, busy,
                    onSelect = { selectedConversationId = it },
                    onSend = { content ->
                        selectedConversationId?.let { id ->
                            perform {
                                PortalApi.sendMessage(id, content)
                                messages = PortalApi.loadMessages(id)
                                portal = PortalApi.loadPortal()
                            }
                        }
                    },
                )
                "Tecnico" -> TechnicianPortalSection(
                    section, user?.id ?: 0, equipment, diagnoses, repairs, busy,
                    onAction = ::perform,
                    onReload = { reload() },
                )
                else -> CommunityPortalSection(
                    role, section, portal, selectedConversationId, messages, user?.id, busy,
                    onSection = { section = it },
                    onSelectConversation = { selectedConversationId = it },
                    onAction = ::perform,
                    onReload = { reload() },
                    onSendMessage = { id, content ->
                        perform {
                            PortalApi.sendMessage(id, content)
                            messages = PortalApi.loadMessages(id)
                            portal = PortalApi.loadPortal()
                        }
                    },
                    onStartConversation = { type, contactId, equipmentId ->
                        perform {
                            val result = PortalApi.createConversation(type, contactId, equipmentId)
                            val id = result["conversacionId"]
                                ?: error("El servidor no devolvió el identificador de la conversación.")
                            portal = PortalApi.loadPortal()
                            selectedConversationId = id
                            section = "Mis conversaciones"
                        }
                    },
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        logoutError?.let { message ->
            AlertDialog(
                onDismissRequest = { logoutError = null },
                title = { Text("No se pudo cerrar la sesión") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            AuthApi.clearSession()
                            logoutError = null
                            onLogout()
                        },
                    ) { Text("Cerrar sesión localmente") }
                },
                dismissButton = {
                    TextButton(onClick = { logoutError = null }) { Text("Volver al panel") }
                },
            )
        }
        EcoSecondaryButton(
            text = if (logoutBusy) "Cerrando sesión…" else "Cerrar sesión",
            onClick = {
                scope.launch {
                    logoutBusy = true
                    logoutError = null
                    try {
                        AuthApi.logout()
                        onLogout()
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (exception: Exception) {
                        logoutError = exception.message ?: "No se pudo revocar la sesión en el servidor."
                    } finally {
                        logoutBusy = false
                    }
                }
            },
            enabled = !busy && !logoutBusy,
        )
    }
}

private fun roleLabel(role: String) = when (role) {
    "Administrador" -> "Centro de administración"
    "Auditor" -> "Supervisión y trazabilidad"
    "Operador" -> "Centro de recogidas"
    "Tecnico" -> "Centro técnico"
    "Vendedor" -> "Espacio de vendedor"
    else -> "Comunidad EcoTech"
}

private fun roleIcon(role: String) = when (role) {
    "Administrador" -> "🛡️"
    "Auditor" -> "🔎"
    "Operador" -> "📦"
    "Tecnico" -> "🛠️"
    "Vendedor" -> "🧑‍💼"
    else -> "♻️"
}

private fun portalSubtitle(role: String) = when (role) {
    "Administrador" -> "Administra usuarios, donantes, inventario y puntos de entrega."
    "Auditor" -> "Consulta la trazabilidad y revisa los cambios almacenados en el sistema."
    "Operador" -> "Responde las solicitudes de conversación asignadas a tu equipo."
    "Tecnico" -> "Evalúa los equipos, registra diagnósticos y documenta reparaciones."
    "Vendedor" -> "Publica equipos, conversa con compradores y consulta puntos de entrega."
    else -> "Explora equipos publicados, conversa con vendedores y coordina entregas."
}

@Composable
private fun PortalTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CommunityPortalSection(
    role: String,
    section: String,
    portal: ApiPortalInitial,
    selectedConversationId: Int?,
    messages: List<ApiMessage>,
    currentUserId: Int?,
    busy: Boolean,
    onSection: (String) -> Unit,
    onSelectConversation: (Int) -> Unit,
    onAction: (suspend () -> Unit) -> Unit,
    onReload: suspend () -> Unit,
    onSendMessage: (Int, String) -> Unit,
    onStartConversation: (String, Int, Int?) -> Unit,
) {
    when (section) {
        "Inicio" -> {
            EcoSectionTitle("Resumen de la comunidad")
            EcoSectionCard {
                MetricRow("Equipos disponibles", portal.equipos.size.toString())
                MetricRow("Puntos de entrega", portal.puntos.size.toString())
                MetricRow("Conversaciones", portal.conversaciones.size.toString())
            }
            EcoSectionTitle("Equipos que buscan un nuevo hogar")
            portal.equipos.take(4).forEach { equipment ->
                EquipmentCard(equipment) {
                    val sellerId = equipment.usuarioId
                    if (sellerId != null) onStartConversation("vendedor", sellerId, equipment.equipoId)
                }
            }
            EcoPrimaryButton("Explorar equipos", onClick = { onSection("Explorar equipos") })
            EcoSectionCard {
                Text("¿Necesitas coordinar una recogida?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Escribe al equipo de operadores para encontrar la mejor opción.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                portal.operadores.forEach { operator ->
                    EcoNavItem("💬", operator.nombre, "Contactar equipo de recogidas") {
                        onStartConversation("operador", operator.id, null)
                    }
                }
            }
        }
        "Explorar equipos" -> EquipmentBrowser(portal.equipos, onStartConversation)
        "Puntos de entrega" -> {
            EcoSectionTitle("Puntos de entrega")
            if (portal.puntos.isEmpty()) EmptyPortalState("No hay puntos de entrega activos.")
            portal.puntos.forEach { point ->
                EcoSectionCard {
                    Text(point.nombre, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${point.ciudad ?: "Ciudad por confirmar"} · ${point.direccion}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Horario: ${point.horario}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    point.instrucciones?.takeIf(String::isNotBlank)?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
        "Mis conversaciones" -> ConversationPortalSection(
            portal.conversaciones, selectedConversationId, messages, currentUserId, busy,
            onSelect = onSelectConversation,
            onSend = { content ->
                val id = selectedConversationId ?: return@ConversationPortalSection
                onSendMessage(id, content)
            },
        )
        "Publicar un equipo" -> PublishEquipmentSection(portal.tipos, busy, onAction, onReload)
    }
}

@Composable
private fun EquipmentBrowser(
    equipment: List<ApiEquipment>,
    onStartConversation: (String, Int, Int?) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    EcoSectionTitle("Explorar equipos")
    PortalField("Buscar por equipo, marca o vendedor", query, { query = it })
    val filtered = equipment.filter {
        listOfNotNull(it.marca, it.modelo, it.tipoNombre, it.vendedorNombre)
            .any { value -> value.contains(query, ignoreCase = true) }
    }
    if (filtered.isEmpty()) EmptyPortalState("No encontramos equipos publicados con esa búsqueda.")
    filtered.forEach { item ->
        EquipmentCard(item) {
            item.usuarioId?.let { sellerId -> onStartConversation("vendedor", sellerId, item.equipoId) }
        }
    }
}

@Composable
private fun EquipmentCard(item: ApiEquipment, onContact: () -> Unit) {
    EcoSectionCard {
        Text(
            "${item.marca.orEmpty()} ${item.modelo.orEmpty()}".trim().ifBlank { item.tipoNombre ?: "Equipo" },
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text("${item.tipoNombre ?: "Equipo"} · ${item.estadoActual}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        item.descripcion?.takeIf(String::isNotBlank)?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Text("Vendedor: ${item.vendedorNombre ?: "Comunidad EcoTech"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        EcoPrimaryButton("Consultar al vendedor", onClick = onContact)
    }
}

@Composable
private fun PublishEquipmentSection(
    types: List<ApiEquipmentType>,
    busy: Boolean,
    onAction: (suspend () -> Unit) -> Unit,
    onReload: suspend () -> Unit,
) {
    var selectedType by remember { mutableStateOf(types.firstOrNull()?.tipoId) }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var serial by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    EcoSectionTitle("Publicar un equipo")
    EcoSectionCard {
        Text("Tipo de equipo", fontWeight = FontWeight.SemiBold)
        ChoiceChips(
            options = types.map { it.nombre },
            selected = types.firstOrNull { it.tipoId == selectedType }?.nombre.orEmpty(),
            onSelect = { label -> selectedType = types.firstOrNull { it.nombre == label }?.tipoId },
        )
        PortalField("Marca", brand, { brand = it })
        PortalField("Modelo", model, { model = it })
        PortalField("Número de serie (opcional)", serial, { serial = it })
        PortalField("Descripción (máximo 200 caracteres)", description, { description = it }, maxLength = 200)
        EcoPrimaryButton(
            text = if (busy) "Publicando…" else "Publicar equipo",
            enabled = !busy && selectedType != null && brand.isNotBlank() && model.isNotBlank(),
            onClick = {
                val typeId = selectedType ?: return@EcoPrimaryButton
                onAction {
                    PortalApi.publishEquipment(
                        ApiPublishEquipment(typeId, brand.trim(), model.trim(), serial.trim().ifBlank { null }, description.trim().ifBlank { null })
                    )
                    onReload()
                }
            },
        )
    }
}

@Composable
private fun ConversationPortalSection(
    conversations: List<ApiConversation>,
    selectedConversationId: Int?,
    messages: List<ApiMessage>,
    currentUserId: Int?,
    busy: Boolean,
    onSelect: (Int) -> Unit,
    onSend: (String) -> Unit,
) {
    var input by remember(selectedConversationId) { mutableStateOf("") }
    EcoSectionTitle(if (conversations.isEmpty()) "Conversaciones" else "Solicitudes y conversaciones")
    if (conversations.isEmpty()) {
        EmptyPortalState("Todavía no hay conversaciones. Al contactar a un vendedor u operador aparecerán aquí.")
        return
    }
    conversations.forEach { conversation ->
        val label = listOfNotNull(conversation.equipoMarca, conversation.equipoModelo)
            .joinToString(" ").ifBlank { conversation.tipo.replaceFirstChar { it.uppercase() } }
        EcoSectionCard {
            EcoNavItem(
                "💬",
                conversation.contactoNombre,
                "$label${conversation.ultimoMensaje?.let { " · $it" } ?: ""}${if (conversation.noLeidos > 0) " · ${conversation.noLeidos} sin leer" else ""}",
                onClick = { onSelect(conversation.conversacionId) },
            )
        }
    }
    val selected = conversations.firstOrNull { it.conversacionId == selectedConversationId }
    if (selected != null) {
        EcoSectionTitle("Chat con ${selected.contactoNombre}")
        EcoSectionCard {
            if (messages.isEmpty()) Text("Inicia la conversación con un mensaje.")
            messages.forEach { message ->
                val sender = if (message.emisorId == currentUserId) "Tú" else message.emisorNombre
                Text("$sender · ${message.fechaEnvio}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(message.contenido, modifier = Modifier.padding(bottom = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                PortalField("Escribe un mensaje (máx. 2.000 caracteres)", input, { input = it }, Modifier.weight(1f), maxLength = 2000)
                EcoPrimaryButton(
                    text = if (busy) "…" else "Enviar",
                    enabled = !busy && input.isNotBlank(),
                    onClick = {
                        val content = input.trim()
                        if (content.isNotEmpty()) {
                            onSend(content)
                            input = ""
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun AuditorPortalSection(
    section: String,
    stats: ApiStats?,
    audits: List<ApiAudit>,
    onViewRecords: () -> Unit,
) {
    if (section == "Resumen") {
        EcoSectionTitle("Transparencia operativa")
        EcoSectionCard {
            MetricRow("Eventos registrados", audits.size.toString())
            MetricRow("Tablas con actividad", audits.map { it.tablaAfectada }.distinct().size.toString())
            MetricRow("Equipos registrados", stats?.totalEquipos?.toString() ?: "—")
        }
        EcoSectionTitle("Trazabilidad")
        EcoSectionCard { Text("Consulta eventos, usuarios y snapshots de los cambios registrados.") }
        EcoPrimaryButton("Explorar registros", onClick = onViewRecords)
        return
    }
    var query by remember { mutableStateOf("") }
    var table by remember { mutableStateOf("") }
    var operation by remember { mutableStateOf("") }
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var page by remember { mutableStateOf(0) }
    val pageSize = 30
    val filtered = audits.filter { item ->
        val searchable = listOfNotNull(
            item.tablaAfectada, item.operacion, item.registroId?.toString(), item.usuarioSql,
            item.detalle, item.valoresAnteriores, item.valoresNuevos,
        ).joinToString(" ")
        (query.isBlank() || searchable.contains(query, ignoreCase = true)) &&
            (table.isBlank() || item.tablaAfectada.contains(table, ignoreCase = true)) &&
            (operation.isBlank() || item.operacion.contains(operation, ignoreCase = true)) &&
            (fromDate.isBlank() || (item.fecha ?: "") >= fromDate) &&
            (toDate.isBlank() || (item.fecha ?: "").take(10) <= toDate)
    }.sortedWith(compareByDescending<ApiAudit> { it.fecha }.thenByDescending { it.auditoriaId })
    val maxPage = ((filtered.size - 1).coerceAtLeast(0)) / pageSize
    page = page.coerceIn(0, maxPage)
    EcoSectionTitle("Actividad del sistema")
    EcoSectionCard {
        PortalField("Buscar detalle, registro, tabla o usuario", query, { query = it; page = 0 })
        PortalField("Tabla", table, { table = it; page = 0 })
        PortalField("Operación", operation, { operation = it; page = 0 })
        PortalField("Desde (AAAA-MM-DD)", fromDate, { fromDate = it; page = 0 })
        PortalField("Hasta (AAAA-MM-DD)", toDate, { toDate = it; page = 0 })
        Text("${filtered.size} resultado(s) · ordenados del más reciente al más antiguo", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    val currentPageRecords = filtered.drop(page * pageSize).take(pageSize)
    if (currentPageRecords.isEmpty()) EmptyPortalState("No hay eventos que coincidan con los filtros.")
    currentPageRecords.forEach { record -> AuditRecordCard(record) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        EcoSecondaryButton("Anterior", onClick = { page = (page - 1).coerceAtLeast(0) }, enabled = page > 0)
        Text("Página ${page + 1} de ${maxPage + 1}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        EcoSecondaryButton("Siguiente", onClick = { page = (page + 1).coerceAtMost(maxPage) }, enabled = page < maxPage)
    }
}

@Composable
private fun AuditRecordCard(record: ApiAudit) {
    var expanded by remember(record.auditoriaId) { mutableStateOf(false) }
    EcoSectionCard {
        Text("${record.tablaAfectada} · ${record.operacion}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(record.detalle ?: "Sin detalle disponible", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Registro ${record.registroId ?: "—"} · ${record.usuarioSql ?: "Usuario no especificado"} · ${record.fecha ?: ""}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (record.valoresAnteriores != null || record.valoresNuevos != null) {
            EcoSecondaryButton(if (expanded) "Ocultar snapshots" else "Ver valores antes y después", onClick = { expanded = !expanded })
            if (expanded) {
                record.valoresAnteriores?.let { Text("Antes\n$it", fontSize = 12.sp) }
                record.valoresNuevos?.let { Text("Después\n$it", fontSize = 12.sp) }
            }
        }

    }
}

@Composable
private fun AdminPortalSection(
            section: String,
            stats: ApiStats?,
            users: List<ApiUser>,
            donors: List<ApiDonor>,
            cities: List<ApiCity>,
            equipment: List<ApiEquipment>,
            diagnoses: List<ApiDiagnosis>,
            repairs: List<ApiRepair>,
            points: List<ApiPoint>,
            busy: Boolean,
            onAction: (suspend () -> Unit) -> Unit,
            onReload: suspend () -> Unit,
        ) {
            when (section) {
                "Resumen" -> {
                    EcoSectionTitle("Dashboard de la plataforma")
                    EcoSectionCard {
                        MetricRow("Usuarios registrados", stats?.totalUsuarios?.toString() ?: "—")
                        MetricRow("Administradores", users.count { it.rol == "Administrador" }.toString())
                        MetricRow("Técnicos", users.count { it.rol == "Tecnico" }.toString())
                        MetricRow("Operadores", users.count { it.rol == "Operador" }.toString())
                        MetricRow("Equipos", stats?.totalEquipos?.toString() ?: "—")
                        MetricRow("Donantes", stats?.totalDonantes?.toString() ?: "—")
                        MetricRow("CO₂ estimado", "${stats?.co2EstimadoKg?.toInt() ?: 0} kg")
                    }
                    EcoSectionTitle("Distribución de equipos")
                    if (stats?.equiposPorEstado.isNullOrEmpty()) {
                        EmptyPortalState("No hay equipos registrados para resumir.")
                    } else {
                        stats.equiposPorEstado.forEach { item ->
                            EcoSectionCard {
                                MetricRow(item.label, item.value.toString())
                            }
                        }
                    }
                    EcoSectionTitle("Actividad de la plataforma")
                    EcoSectionCard {
                        MetricRow("Entregas del mes", stats?.entregasDelMes?.toString() ?: "—")
                        MetricRow("Recolecciones del mes", stats?.recoleccionesDelMes?.toString() ?: "—")
                        MetricRow("Diagnósticos", diagnoses.size.toString())
                        MetricRow("Reparaciones", repairs.size.toString())
                    }
                }
                "Usuarios" -> AdminUsersSection(users, busy, onAction, onReload)
                "Donantes" -> AdminDonorsSection(donors, cities, busy, onAction, onReload)
                "Operaciones" -> AdminOperationsSection(equipment, diagnoses, repairs)
                "Estados" -> {
                    EcoSectionTitle("Estado del inventario")
                    stats?.equiposPorEstado.orEmpty().forEach { item ->
                        EcoSectionCard { MetricRow(item.label, item.value.toString()) }
                    }
                    if (stats?.equiposPorEstado.isNullOrEmpty()) EmptyPortalState("No hay estados registrados.")
                }
                "Puntos de entrega" -> AdminPointsSection(points, cities, busy, onAction, onReload)
            }
        }

        @Composable
        private fun AdminUsersSection(
            users: List<ApiUser>,
            busy: Boolean,
            onAction: (suspend () -> Unit) -> Unit,
            onReload: suspend () -> Unit,
        ) {
            var editing by remember { mutableStateOf<ApiUser?>(null) }
            var pendingDelete by remember { mutableStateOf<ApiUser?>(null) }
            var name by remember(editing?.usuarioId) { mutableStateOf(editing?.nombre.orEmpty()) }
            var lastName by remember(editing?.usuarioId) { mutableStateOf(editing?.apellido.orEmpty()) }
            var email by remember(editing?.usuarioId) { mutableStateOf(editing?.email.orEmpty()) }
            var phone by remember(editing?.usuarioId) { mutableStateOf(editing?.telefono.orEmpty()) }
            var role by remember(editing?.usuarioId) { mutableStateOf(editing?.rol ?: "Operador") }
            var password by remember(editing?.usuarioId) { mutableStateOf("") }

            EcoSectionTitle(if (editing == null) "Crear usuario" else "Editar usuario")
            EcoSectionCard {
                PortalField("Nombre", name, { name = it })
                PortalField("Apellidos", lastName, { lastName = it })
                PortalField("Correo electrónico", email, { email = it }, keyboardType = KeyboardType.Email)
                PortalField("Teléfono", phone, { phone = it }, keyboardType = KeyboardType.Phone)
                PortalField(
                    if (editing == null) "Contraseña (mínimo 6 caracteres)" else "Nueva contraseña (opcional)",
                    password,
                    { password = it },
                )
                Text("Cargo", fontWeight = FontWeight.SemiBold)
                ChoiceChips(
                    listOf("Administrador", "Tecnico", "Operador", "Auditor", "Usuario", "Vendedor"),
                    role,
                    { role = it },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EcoPrimaryButton(
                        text = if (busy) "Guardando…" else if (editing == null) "Crear usuario" else "Guardar cambios",
                        enabled = !busy && name.isNotBlank() && lastName.isNotBlank() && email.contains("@") &&
                            phone.isNotBlank() && (editing != null || password.length >= 6),
                        onClick = {
                            val selected = editing
                            onAction {
                                if (selected == null) {
                                    PortalApi.createUser(ApiCreateUser(name.trim(), lastName.trim(), email.trim(), phone.trim(), role, password))
                                } else {
                                    PortalApi.updateUser(
                                        selected.usuarioId,
                                        ApiUpdateUser(name.trim(), lastName.trim(), email.trim(), phone.trim(), role, password.ifBlank { null }),
                                    )
                                }
                                editing = null
                                password = ""
                                onReload()
                            }
                        },
                    )
                    if (editing != null) {
                        EcoSecondaryButton(
                            "Cancelar",
                            onClick = { editing = null; password = "" },
                            enabled = !busy,
                        )
                    }
                }
            }
            EcoSectionTitle("Usuarios registrados (${users.size})")
            if (users.isEmpty()) EmptyPortalState("No hay usuarios registrados.")
            users.forEach { user ->
                EcoSectionCard {
                    Text("${user.nombre} ${user.apellido}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${user.email} · ${user.telefono}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${user.rol} · ${if (user.activo) "Activo" else "Inactivo"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EcoOutlinedButton(
                            "Editar",
                            onClick = { editing = user },
                            enabled = !busy,
                        )
                        EcoOutlinedButton(
                            if (user.activo) "Inhabilitar" else "Activar",
                            onClick = {
                                onAction {
                                    PortalApi.setUserActive(user.usuarioId, !user.activo)
                                    onReload()
                                }
                            },
                            enabled = !busy,
                        )
                        EcoSecondaryButton(
                            "Eliminar",
                            onClick = { pendingDelete = user },
                            enabled = !busy,
                        )
                    }
                    if (pendingDelete?.usuarioId == user.usuarioId) {
                        Text("¿Eliminar permanentemente esta cuenta?", color = MaterialTheme.colorScheme.error)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EcoPrimaryButton(
                                "Confirmar eliminación",
                                enabled = !busy,
                                onClick = {
                                    onAction {
                                        PortalApi.deleteUser(user.usuarioId)
                                        pendingDelete = null
                                        onReload()
                                    }
                                },
                            )
                            EcoSecondaryButton("Cancelar", onClick = { pendingDelete = null }, enabled = !busy)
                        }
                    }
                }
            }
        }

        @Composable
        private fun AdminDonorsSection(
            donors: List<ApiDonor>,
            cities: List<ApiCity>,
            busy: Boolean,
            onAction: (suspend () -> Unit) -> Unit,
            onReload: suspend () -> Unit,
        ) {
            var editing by remember { mutableStateOf<ApiDonor?>(null) }
            var pendingDelete by remember { mutableStateOf<ApiDonor?>(null) }
            var type by remember(editing?.donanteId) { mutableStateOf(editing?.tipo ?: "Empresa") }
            var name by remember(editing?.donanteId) { mutableStateOf(editing?.nombre.orEmpty()) }
            var email by remember(editing?.donanteId) { mutableStateOf(editing?.email.orEmpty()) }
            var phone by remember(editing?.donanteId) { mutableStateOf(editing?.telefono.orEmpty()) }
            var address by remember(editing?.donanteId) { mutableStateOf(editing?.direccion.orEmpty()) }
            var cityId by remember(editing?.donanteId) { mutableStateOf(editing?.ciudadId) }

            EcoSectionTitle(if (editing == null) "Registrar donante" else "Editar donante")
            EcoSectionCard {
                PortalField("Tipo de donante", type, { type = it })
                PortalField("Nombre", name, { name = it })
                PortalField("Correo electrónico", email, { email = it }, keyboardType = KeyboardType.Email)
                PortalField("Teléfono", phone, { phone = it }, keyboardType = KeyboardType.Phone)
                PortalField("Dirección", address, { address = it })
                Text("Ciudad (opcional)", fontWeight = FontWeight.SemiBold)
                ChoiceChips(
                    options = listOf("Sin ciudad") + cities.map { "${it.nombre}, ${it.departamento}" },
                    selected = cities.firstOrNull { it.ciudadId == cityId }?.let { "${it.nombre}, ${it.departamento}" } ?: "Sin ciudad",
                    onSelect = { selection ->
                        cityId = cities.firstOrNull { "${it.nombre}, ${it.departamento}" == selection }?.ciudadId
                    },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EcoPrimaryButton(
                        text = if (busy) "Guardando…" else if (editing == null) "Crear donante" else "Guardar cambios",
                        enabled = !busy && name.isNotBlank() && email.contains("@") && phone.isNotBlank() && type.isNotBlank() && address.isNotBlank(),
                        onClick = {
                            val current = editing
                            val request = ApiDonorRequest(type.trim(), name.trim(), email.trim(), phone.trim(), cityId, address.trim())
                            onAction {
                                if (current == null) PortalApi.createDonor(request) else PortalApi.updateDonor(current.donanteId, request)
                                editing = null
                                onReload()
                            }
                        },
                    )
                    if (editing != null) EcoSecondaryButton("Cancelar", onClick = { editing = null }, enabled = !busy)
                }
            }
            EcoSectionTitle("Donantes registrados (${donors.size})")
            if (donors.isEmpty()) EmptyPortalState("No hay donantes registrados.")
            donors.forEach { donor ->
                EcoSectionCard {
                    Text(donor.nombre, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${donor.tipo} · ${donor.email} · ${donor.telefono}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${donor.direccion}${donor.ciudadNombre?.let { " · $it" } ?: ""}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EcoOutlinedButton("Editar", onClick = { editing = donor }, enabled = !busy)
                        EcoSecondaryButton("Eliminar", onClick = { pendingDelete = donor }, enabled = !busy)
                    }
                    if (pendingDelete?.donanteId == donor.donanteId) {
                        Text("¿Eliminar el donante? No podrá borrarse si tiene equipos asociados.", color = MaterialTheme.colorScheme.error)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EcoPrimaryButton("Confirmar", enabled = !busy, onClick = {
                                onAction {
                                    PortalApi.deleteDonor(donor.donanteId)
                                    pendingDelete = null
                                    onReload()
                                }
                            })
                            EcoSecondaryButton("Cancelar", onClick = { pendingDelete = null }, enabled = !busy)
                        }
                    }
                }
            }
        }

        @Composable
        private fun AdminOperationsSection(
            equipment: List<ApiEquipment>,
            diagnoses: List<ApiDiagnosis>,
            repairs: List<ApiRepair>,
        ) {
            EcoSectionTitle("Operaciones del inventario")
            EcoSectionCard {
                MetricRow("Activos", equipment.size.toString())
                MetricRow("Diagnósticos", diagnoses.size.toString())
                MetricRow("Reparaciones", repairs.size.toString())
            }
            EcoSectionTitle("Equipos recientes")
            equipment.sortedByDescending { it.equipoId }.take(50).forEach {
                EcoSectionCard {
                    Text("${it.marca.orEmpty()} ${it.modelo.orEmpty()}".trim().ifBlank { "Equipo #${it.equipoId}" }, fontWeight = FontWeight.Bold)
                    Text("${it.tipoNombre ?: "Equipo"} · ${it.estadoActual} · ${it.donanteNombre ?: "Sin donante"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            EcoSectionTitle("Diagnósticos recientes")
            diagnoses.sortedByDescending { it.diagnosticoId }.take(50).forEach {
                EcoSectionCard {
                    Text("Equipo #${it.equipoId} · ${it.fecha ?: ""}", fontWeight = FontWeight.Bold)
                    Text(it.descripcion ?: "Sin descripción", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            EcoSectionTitle("Reparaciones recientes")
            repairs.sortedByDescending { it.reparacionId }.take(50).forEach {
                EcoSectionCard {
                    Text("Equipo #${it.equipoId} · ${it.estado ?: "En progreso"}", fontWeight = FontWeight.Bold)
                    Text(it.descripcion ?: "Sin descripción", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        @Composable
        private fun AdminPointsSection(
            points: List<ApiPoint>,
            cities: List<ApiCity>,
            busy: Boolean,
            onAction: (suspend () -> Unit) -> Unit,
            onReload: suspend () -> Unit,
        ) {
            var name by remember { mutableStateOf("") }
            var address by remember { mutableStateOf("") }
            var hours by remember { mutableStateOf("") }
            var instructions by remember { mutableStateOf("") }
            var cityId by remember { mutableStateOf<Int?>(null) }
            var pendingDelete by remember { mutableStateOf<ApiPoint?>(null) }
            EcoSectionTitle("Registrar punto de entrega")
            EcoSectionCard {
                PortalField("Nombre", name, { name = it }, maxLength = 120)
                PortalField("Dirección", address, { address = it }, maxLength = 250)
                PortalField("Horario", hours, { hours = it }, maxLength = 150)
                PortalField("Instrucciones (opcional)", instructions, { instructions = it }, maxLength = 250)
                Text("Ciudad (opcional)", fontWeight = FontWeight.SemiBold)
                ChoiceChips(
                    options = listOf("Sin ciudad") + cities.map { "${it.nombre}, ${it.departamento}" },
                    selected = cities.firstOrNull { it.ciudadId == cityId }?.let { "${it.nombre}, ${it.departamento}" } ?: "Sin ciudad",
                    onSelect = { selected -> cityId = cities.firstOrNull { "${it.nombre}, ${it.departamento}" == selected }?.ciudadId },
                )
                EcoPrimaryButton(
                    text = if (busy) "Guardando…" else "Crear punto",
                    enabled = !busy && name.isNotBlank() && address.isNotBlank() && hours.isNotBlank(),
                    onClick = {
                        onAction {
                            PortalApi.createPoint(ApiPointRequest(name.trim(), cityId, address.trim(), hours.trim(), instructions.trim().ifBlank { null }))
                            name = ""; address = ""; hours = ""; instructions = ""; cityId = null
                            onReload()
                        }
                    },
                )
            }
            EcoSectionTitle("Puntos de entrega (${points.size})")
            if (points.isEmpty()) EmptyPortalState("No hay puntos de entrega registrados.")
            points.forEach { point ->
                EcoSectionCard {
                    Text(point.nombre, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${point.ciudad ?: "Sin ciudad"} · ${point.direccion}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(point.horario, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Estado: ${if (point.activo) "Activo" else "Inactivo"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    EcoSecondaryButton("Eliminar", onClick = { pendingDelete = point }, enabled = !busy)
                    if (pendingDelete?.puntoId == point.puntoId) {
                        Text("¿Eliminar permanentemente este punto?", color = MaterialTheme.colorScheme.error)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EcoPrimaryButton("Confirmar eliminación", enabled = !busy, onClick = {
                                onAction {
                                    PortalApi.deletePoint(point.puntoId)
                                    pendingDelete = null
                                    onReload()
                                }
                            })
                            EcoSecondaryButton("Cancelar", onClick = { pendingDelete = null }, enabled = !busy)
                        }
                    }
                }
            }
        }

        @Composable
        private fun TechnicianPortalSection(
            section: String,
            technicianId: Int,
            equipment: List<ApiEquipment>,
            diagnoses: List<ApiDiagnosis>,
            repairs: List<ApiRepair>,
            busy: Boolean,
            onAction: (suspend () -> Unit) -> Unit,
            onReload: suspend () -> Unit,
        ) {
            val myDiagnoses = diagnoses.filter { it.tecnicoId == technicianId }
            val myRepairs = repairs.filter { it.tecnicoId == technicianId }
            when (section) {
                "Resumen" -> {
                    EcoSectionTitle("Taller EcoTech")
                    EcoSectionCard {
                        MetricRow("Equipos registrados", equipment.size.toString())
                        MetricRow("Pendientes de evaluación", equipment.count { it.estadoActual.equals("Recibido", true) }.toString())
                        MetricRow("En reparación", equipment.count { it.estadoActual.contains("repar", true) }.toString())
                        MetricRow("Mis diagnósticos", myDiagnoses.size.toString())
                    }
                    EcoPrimaryButton("Revisar inventario", onClick = {})
                }
                "Inventario" -> TechnicianInventory(equipment, technicianId, busy, onAction, onReload)
                "Mi actividad" -> {
                    EcoSectionTitle("Mis diagnósticos y reparaciones recientes")
                    val entries = (myDiagnoses.map { Triple(it.fecha.orEmpty(), "Diagnóstico · equipo #${it.equipoId}", it.descripcion.orEmpty()) } +
                        myRepairs.map { Triple(it.fechaInicio.orEmpty(), "Reparación · equipo #${it.equipoId}", it.descripcion.orEmpty()) })
                        .sortedByDescending { it.first }
                    if (entries.isEmpty()) EmptyPortalState("Aún no hay actividad técnica registrada.")
                    entries.take(100).forEach { (date, title, description) ->
                        EcoSectionCard {
                            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(description.ifBlank { "Sin detalle adicional" })
                        }
                    }
                }
            }
        }

        @Composable
        private fun TechnicianInventory(
            equipment: List<ApiEquipment>,
            technicianId: Int,
            busy: Boolean,
            onAction: (suspend () -> Unit) -> Unit,
            onReload: suspend () -> Unit,
        ) {
            var query by remember { mutableStateOf("") }
            EcoSectionTitle("Inventario de trabajo")
            PortalField("Buscar equipo, tipo o estado", query, { query = it })
            val filtered = equipment.filter {
                listOfNotNull(it.marca, it.modelo, it.tipoNombre, it.estadoActual)
                    .any { value -> value.contains(query, ignoreCase = true) }
            }.sortedByDescending { it.equipoId }.take(100)
            if (filtered.isEmpty()) EmptyPortalState("No hay equipos que coincidan con la búsqueda.")
            filtered.forEach { item -> TechnicianEquipmentCard(item, technicianId, busy, onAction, onReload) }
        }

        @Composable
        private fun TechnicianEquipmentCard(
            equipment: ApiEquipment,
            technicianId: Int,
            busy: Boolean,
            onAction: (suspend () -> Unit) -> Unit,
            onReload: suspend () -> Unit,
        ) {
            var diagnosisOpen by remember(equipment.equipoId) { mutableStateOf(false) }
            var repairOpen by remember(equipment.equipoId) { mutableStateOf(false) }
            var diagnosis by remember(equipment.equipoId) { mutableStateOf("") }
            var requiresRepair by remember(equipment.equipoId) { mutableStateOf(true) }
            var estimatedCost by remember(equipment.equipoId) { mutableStateOf("") }
            var repairDescription by remember(equipment.equipoId) { mutableStateOf("") }
            var parts by remember(equipment.equipoId) { mutableStateOf("") }
            var actualCost by remember(equipment.equipoId) { mutableStateOf("") }
            var completed by remember(equipment.equipoId) { mutableStateOf(false) }
            val label = "${equipment.marca.orEmpty()} ${equipment.modelo.orEmpty()}".trim().ifBlank { "Equipo #${equipment.equipoId}" }
            EcoSectionCard {
                Text("${equipment.tipoNombre ?: "Equipo"} · #${equipment.equipoId}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(label, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("${equipment.serial ?: "Sin número de serie"} · ${equipment.estadoActual}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Recibido ${equipment.fechaRecepcion ?: "—"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                EcoOutlinedButton("Registrar diagnóstico", onClick = { diagnosisOpen = !diagnosisOpen }, enabled = !busy)
                if (diagnosisOpen) {
                    PortalField("Hallazgos y diagnóstico", diagnosis, { diagnosis = it }, maxLength = 250)
                    Text("¿Requiere reparación?", fontWeight = FontWeight.SemiBold)
                    ChoiceChips(
                        listOf("Sí, requiere reparación", "No, listo para entrega"),
                        if (requiresRepair) "Sí, requiere reparación" else "No, listo para entrega",
                    ) { requiresRepair = it.startsWith("Sí") }
                    PortalField("Costo estimado (opcional)", estimatedCost, { estimatedCost = it }, keyboardType = KeyboardType.Decimal)
                    EcoPrimaryButton(
                        "Guardar diagnóstico",
                        enabled = !busy && diagnosis.isNotBlank() && (estimatedCost.isBlank() || (estimatedCost.toDoubleOrNull() ?: -1.0) >= 0),
                        onClick = {
                            onAction {
                                PortalApi.createDiagnosis(
                                    ApiDiagnosisRequest(
                                        equipment.equipoId,
                                        technicianId,
                                        diagnosis.trim(),
                                        requiresRepair,
                                        estimatedCost.toDoubleOrNull(),
                                    )
                                )
                                diagnosisOpen = false
                                onReload()
                            }
                        },
                    )
                }
                EcoOutlinedButton("Registrar reparación", onClick = { repairOpen = !repairOpen }, enabled = !busy)
                if (repairOpen) {
                    PortalField("Trabajo realizado", repairDescription, { repairDescription = it }, maxLength = 250)
                    PortalField("Repuestos utilizados (opcional)", parts, { parts = it }, maxLength = 100)
                    PortalField("Costo real (opcional)", actualCost, { actualCost = it }, keyboardType = KeyboardType.Decimal)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = completed, onCheckedChange = { completed = it })
                        Text(if (completed) "Reparación completada" else "Reparación en progreso")
                    }
                    EcoPrimaryButton(
                        "Guardar reparación",
                        enabled = !busy && repairDescription.isNotBlank() &&
                            (actualCost.isBlank() || (actualCost.toDoubleOrNull() ?: -1.0) >= 0),
                        onClick = {
                            onAction {
                                PortalApi.createRepair(
                                    ApiRepairRequest(
                                        equipment.equipoId,
                                        technicianId,
                                        repairDescription.trim(),
                                        parts.trim().ifBlank { null },
                                        actualCost.toDoubleOrNull(),
                                        if (completed) "Completada" else "En reparación",
                                    )
                                )
                                repairOpen = false
                                onReload()
                            }
                        },
                    )
                }
            }
        }

@Composable
private fun PortalField(
            label: String,
            value: String,
            onValueChange: (String) -> Unit,
            modifier: Modifier = Modifier,
            keyboardType: KeyboardType = KeyboardType.Text,
            maxLength: Int = Int.MAX_VALUE,
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = { if (it.length <= maxLength) onValueChange(it) },
                label = { Text(label) },
                modifier = modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                maxLines = if (maxLength >= 100) 3 else 1,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }

        @Composable
        private fun ChoiceChips(options: List<String>, selected: String, onSelect: (String) -> Unit) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                options.forEach { option ->
                    Surface(
                        onClick = { onSelect(option) },
                        shape = RoundedCornerShape(18.dp),
                        color = if (option == selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (option == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        ),
                    ) {
                        Text(
                            option,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }

        @Composable
        private fun EmptyPortalState(message: String) {
            EcoSectionCard {
                Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        @Composable
        private fun MetricRow(label: String, value: String) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
