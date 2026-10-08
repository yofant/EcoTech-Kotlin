package com.example.ecotech

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminHomeScreen(
    user: UserResponse?,
    onLogout: () -> Unit,
    onOpenUsers: () -> Unit,
    onOpenTracking: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    EcoBackground {
        EcoRoleHero(
            kicker = "Centro de control EcoTech",
            title = "Hola, ${user?.name ?: "Administrador"}",
            subtitle = "Administra usuarios, entregas y actividad desde un solo lugar.",
            icon = "🛡️",
        )

        EcoBody {
            EcoSectionTitle("🌍 Indicadores generales")
            EcoSectionCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    AdminMetric("Usuarios", "1.240", "👥", Modifier.weight(1f))
                    AdminMetric("Equipos", "3.850", "♻️", Modifier.weight(1f))
                    AdminMetric("CO₂", "12.4 t", "🌱", Modifier.weight(1f))
                }
            }

            EcoSectionTitle("⚙️ Administración")
            EcoSectionCard {
                EcoNavItem("👥", "Usuarios", "Listado y control de estados", onClick = onOpenUsers)
                EcoNavItem("🚚", "Seguimiento de entregas", "Equipos recolectados y actividad", onClick = onOpenTracking)
                EcoNavItem("📈", "Estadísticas", "Indicadores comerciales y ambientales", onClick = onOpenStats)
                EcoNavItem("📅", "Eventos de recolección", "Registros asociados a usuarios", onClick = onOpenEvents)
                EcoNavItem("💬", "Chats", "Soporte y conversaciones", onClick = onOpenChat)
                EcoNavItem("🔔", "Notificaciones", "Alertas de la plataforma", onClick = onOpenNotifications)
            }

            Spacer(Modifier.height(4.dp))
            EcoSecondaryButton(text = "Cerrar sesión", onClick = onLogout)
        }
    }
}

@Composable
private fun AdminMetric(
    title: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .height(96.dp)
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.07f), RoundedCornerShape(15.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(15.dp))
            .padding(8.dp),
    ) {
        Text(emoji, fontSize = 20.sp)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun DeliveryTrackingScreen(onBack: () -> Unit) {
    EcoBackground {
        EcoBackHeader(title = "Seguimiento de entregas", onBack = onBack)
        Spacer(Modifier.height(12.dp))
        EcoSubtitle("Entregas en curso, equipos recolectados y actividad mensual.")
        Spacer(Modifier.height(8.dp))

        EcoBody {
            EcoSectionTitle("🚚 Entregas en curso")
            EcoSectionCard {
                DemoData.deliveries.forEach { delivery ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(delivery.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            Text(delivery.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${3 - delivery.totalOrders + 1}/3", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            EcoSectionTitle("♻️ Equipos recolectados")
            EcoSectionCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    AdminMetric("Este mes", "312", "📦", Modifier.weight(1f))
                    AdminMetric("Reciclad", "2.1 t", "♻️", Modifier.weight(1f))
                }
            }

            EcoSectionTitle("📅 Actividad mensual")
            EcoSectionCard {
                MetricRow("Recolecciones", "28 eventos")
                MetricRow("Entregas completadas", "24")
                MetricRow("Equipos en tránsito", "8")
            }
        }
    }
}

@Composable
fun UserListScreen(onBack: () -> Unit, onOpenUserOptions: () -> Unit) {
    EcoBackground {
        EcoBackHeader(title = "Usuarios", onBack = onBack)
        Spacer(Modifier.height(12.dp))
        EcoSubtitle("Listado de usuarios con control de estado.")
        Spacer(Modifier.height(8.dp))

        EcoBody {
            EcoSectionCard {
                DemoData.userList.forEach { user ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(38.dp)
                                .height(38.dp)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(19.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("👤", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(user.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            Text(user.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("Activo", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            EcoSectionTitle("🖱️ Acciones")
            EcoSectionCard {
                Text("Selecciona un usuario para gestionar sus registros (modificar, eliminar o inhabilitar).", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(4.dp))
            EcoPrimaryButton(text = "Gestionar usuario", onClick = onOpenUserOptions)
        }
    }
}

@Composable
fun UserOptionsScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onDelete: () -> Unit,
    onDeactivate: () -> Unit,
) {
    var selected by remember { mutableStateOf("Modificar") }

    EcoBackground {
        EcoBackHeader(title = "Gestión de usuario", onBack = onBack)
        Spacer(Modifier.height(12.dp))
        EcoSubtitle("Opciones disponibles para el registro seleccionado.")
        Spacer(Modifier.height(8.dp))

        EcoBody {
            EcoSectionCard {
                EcoCheckboxRow("✏️ Modificar registro", selected == "Modificar", onClick = { selected = "Modificar" })
                EcoCheckboxRow("🗑️ Eliminar registro", selected == "Eliminar", onClick = { selected = "Eliminar" })
                EcoCheckboxRow("🚫 Inhabilitar / desactivar", selected == "Inhabilitar", onClick = { selected = "Inhabilitar" })
            }

            when (selected) {
                "Eliminar" -> EcoPrimaryButton("Confirmar eliminación", onClick = onDelete)
                "Inhabilitar" -> EcoPrimaryButton("Confirmar inhabilitación", onClick = onDeactivate)
                else -> EcoPrimaryButton("Guardar cambios", onClick = onSuccess)
            }
        }
    }
}

@Composable
fun CollectionEventsScreen(onBack: () -> Unit) {
    EcoBackground {
        EcoBackHeader(title = "Eventos", onBack = onBack)
        Spacer(Modifier.height(12.dp))
        EcoSubtitle("Eventos de recolección asociados a usuarios.")
        Spacer(Modifier.height(8.dp))

        EcoBody {
            EcoSectionCard {
                DemoData.events.forEach { event ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(38.dp)
                                .height(38.dp)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(19.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("📅", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(event.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            Text(event.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            EcoSectionCard {
                Text("Total eventos del mes: 28", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun AuditorHomeScreen(
    user: UserResponse?,
    onLogout: () -> Unit,
    onOpenUsers: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenEvents: () -> Unit,
) {
    EcoBackground {
        EcoRoleHero(
            kicker = "Vista de consulta",
            title = "Hola, ${user?.name ?: "Auditor"}",
            subtitle = "Revisa registros, indicadores y eventos de la plataforma.",
            icon = "🔎",
        )

        EcoBody {
            EcoSectionTitle("📋 Resumen de auditoría")
            EcoSectionCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    AdminMetric("Usuarios", "1.240", "👥", Modifier.weight(1f))
                    AdminMetric("Eventos", "28", "📅", Modifier.weight(1f))
                    AdminMetric("Revisiones", "96", "✅", Modifier.weight(1f))
                }
            }

            EcoSectionTitle("🧭 Herramientas de revisión")
            EcoSectionCard {
                EcoNavItem("👥", "Revisar usuarios", "Consulta los registros de la plataforma", onClick = onOpenUsers)
                EcoNavItem("📈", "Revisar indicadores", "Consulta las métricas comerciales", onClick = onOpenStats)
                EcoNavItem("📅", "Revisar eventos", "Consulta la actividad de recolección", onClick = onOpenEvents)
            }

            EcoSecondaryButton(text = "Cerrar sesión", onClick = onLogout)
        }
    }
}

@Composable
fun OperatorHomeScreen(
    user: UserResponse?,
    onLogout: () -> Unit,
    onOpenTracking: () -> Unit,
    onOpenEvents: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    EcoBackground {
        EcoRoleHero(
            kicker = "Centro de recogidas",
            title = "Hola, ${user?.name ?: "Operador"}",
            subtitle = "Responde solicitudes y coordina entregas de equipos.",
            icon = "📦",
        )

        EcoBody {
            EcoSectionTitle("🚚 Operación de hoy")
            EcoSectionCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    AdminMetric("Entregas", "8", "🚚", Modifier.weight(1f))
                    AdminMetric("En curso", "3", "📍", Modifier.weight(1f))
                    AdminMetric("Eventos", "2", "📅", Modifier.weight(1f))
                }
            }

            EcoSectionTitle("🧰 Tareas")
            EcoSectionCard {
                EcoNavItem("🚚", "Coordinar entregas", "Consulta envíos y su estado", onClick = onOpenTracking)
                EcoNavItem("♻️", "Gestionar recolecciones", "Consulta eventos y equipos recolectados", onClick = onOpenEvents)
                EcoNavItem("💬", "Contactar usuarios", "Resuelve preguntas sobre entregas", onClick = onOpenChat)
                EcoNavItem("🔔", "Ver alertas", "Revisa novedades operativas", onClick = onOpenNotifications)
            }

            EcoSecondaryButton(text = "Cerrar sesión", onClick = onLogout)
        }
    }
}

@Composable
fun TechnicianHomeScreen(
    user: UserResponse?,
    onLogout: () -> Unit,
    onOpenInspections: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    EcoBackground {
        EcoRoleHero(
            kicker = "Centro técnico",
            title = "Hola, ${user?.name ?: "Técnico"}",
            subtitle = "Inspecciona y registra el estado de los equipos asignados.",
            icon = "🛠️",
        )

        EcoBody {
            EcoSectionTitle("🔧 Trabajo técnico")
            EcoSectionCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    AdminMetric("Asignados", "${DemoData.featuredProducts.size}", "📋", Modifier.weight(1f))
                    AdminMetric("Reparaciones", "3", "🔩", Modifier.weight(1f))
                    AdminMetric("Listos", "12", "✅", Modifier.weight(1f))
                }
            }

            EcoSectionTitle("🧰 Herramientas")
            EcoSectionCard {
                EcoNavItem("🔍", "Inspeccionar equipos", "Registra el diagnóstico de cada equipo", onClick = onOpenInspections)
                EcoNavItem("💬", "Consultar soporte", "Coordina con el equipo EcoTech", onClick = onOpenChat)
                EcoNavItem("🔔", "Ver alertas técnicas", "Revisa avisos de equipos", onClick = onOpenNotifications)
            }

            EcoSecondaryButton(text = "Cerrar sesión", onClick = onLogout)
        }
    }
}

@Composable
fun TechnicalInspectionsScreen(onBack: () -> Unit) {
    val reviewedProducts = remember { mutableStateMapOf<String, Boolean>() }
    val reviewedCount = reviewedProducts.values.count { it }

    EcoBackground {
        EcoBackHeader(title = "Inspección de equipos", onBack = onBack)
        Spacer(Modifier.height(12.dp))
        EcoSubtitle("Registra la revisión técnica de los equipos asignados.")
        Spacer(Modifier.height(8.dp))

        EcoBody {
            EcoSectionCard {
                MetricRow("Equipos revisados", "$reviewedCount de ${DemoData.featuredProducts.size}")
            }
            DemoData.featuredProducts.forEach { product ->
                EcoSectionCard {
                    Text("${product.emoji} ${product.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${product.category} • ${product.condition}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    EcoPrimaryButton(
                        text = if (reviewedProducts[product.name] == true) "Inspección registrada" else "Marcar como revisado",
                        onClick = { reviewedProducts[product.name] = true },
                        enabled = reviewedProducts[product.name] != true,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Preview(showBackground = true)
@Composable
fun AdminHomePreview() {
    MaterialTheme {
        AdminHomeScreen(
            user = null,
            onLogout = {},
            onOpenUsers = {},
            onOpenTracking = {},
            onOpenStats = {},
            onOpenEvents = {},
            onOpenChat = {},
            onOpenNotifications = {},
        )
    }
}