package com.example.ecotech

import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        EcoTitle(text = "🛡️ Panel Administrador", fontSize = 26, topPadding = 4)
        Spacer(Modifier.height(4.dp))
        EcoSubtitle("¡Hola, ${user?.name ?: "Administrador"}! Controla plataforma, usuarios y recolecciones.")
        Spacer(Modifier.height(8.dp))

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
            .background(EcoGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(8.dp),
    ) {
        Text(emoji, fontSize = 20.sp)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
        Text(title, fontSize = 11.sp, color = Color.Gray)
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
                            Text(delivery.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = EcoGreenDark)
                            Text(delivery.email, fontSize = 12.sp, color = Color.Gray)
                        }
                        Text("${3 - delivery.totalOrders + 1}/3", fontSize = 13.sp, color = EcoGreen, fontWeight = FontWeight.Bold)
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
                                .background(EcoGreen.copy(alpha = 0.2f), RoundedCornerShape(19.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("👤", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(user.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = EcoGreenDark)
                            Text(user.email, fontSize = 12.sp, color = Color.Gray)
                        }
                        Text("Activo", fontSize = 12.sp, color = EcoGreen, fontWeight = FontWeight.Medium)
                    }
                }
            }

            EcoSectionTitle("🖱️ Acciones")
            EcoSectionCard {
                Text("Selecciona un usuario para gestionar sus registros (modificar, eliminar o inhabilitar).", fontSize = 13.sp, color = Color.Gray)
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
                                .background(EcoGreen.copy(alpha = 0.2f), RoundedCornerShape(19.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("📅", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(event.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = EcoGreenDark)
                            Text(event.email, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
            EcoSectionCard {
                Text("Total eventos del mes: 28", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
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
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EcoGreenDark)
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