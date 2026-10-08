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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
fun ProfileScreen(
    user: UserResponse?,
    onBack: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenChat: () -> Unit,
    onLogout: () -> Unit,
) {
    EcoBackground {
        EcoBackHeader(title = "Mi perfil", onBack = onBack)
        Spacer(Modifier.height(12.dp))

        EcoBody {
            EcoSectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(56.dp)
                            .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("👤", fontSize = 28.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("${user?.name} ${user?.lastName}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(user?.email ?: "", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(user?.phone ?: "", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Rol: ${user?.role ?: "Cliente"}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            EcoSectionTitle("📋 Opciones")
            EcoSectionCard {
                EcoNavItem("💌", "Invitaciones", "Invita amigos y gana descuentos", onClick = onOpenNotifications)
                EcoNavItem("🧾", "Historial de compras", "Pedidos, trueques y seguimiento", onClick = onBack)
                EcoNavItem("💬", "Chats", "Conversaciones con vendedores", onClick = onOpenChat)
                EcoNavItem("🔔", "Notificaciones", "Ver todas mis notificaciones", onClick = onOpenNotifications)
            }

            Spacer(Modifier.height(4.dp))
            EcoSecondaryButton(text = "Cerrar sesión", onClick = onLogout)
        }
    }
}

@Composable
fun NotificationsScreen(onBack: () -> Unit, onOpenChat: () -> Unit) {
    EcoBackground {
        EcoBackHeader(title = "Notificaciones", onBack = onBack)
        Spacer(Modifier.height(12.dp))

        EcoBody {
            EcoSectionCard {
                DemoData.notifications.forEach { notification ->
                    Column(Modifier.padding(vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔔", fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(notification.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.weight(1f))
                            Text(notification.time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(notification.body, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 28.dp))
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            EcoPrimaryButton(text = "Ir al chat", onClick = onOpenChat)
        }
    }
}

@Composable
fun ChatScreen(onBack: () -> Unit, variant: Int = 1) {
    var messages by remember { mutableStateOf(DemoData.chatMessages) }
    var input by remember { mutableStateOf("") }

    val contact = if (variant == 2) "María (Vendedora)" else "TechVerde Store"
    val online = if (variant == 2) "Conectada hace 5 min" else "En línea"

    EcoBackground {
        EcoBackHeader(title = "Chat", onBack = onBack)
        Spacer(Modifier.height(12.dp))

        EcoSectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (variant == 2) "👩‍💼" else "🏪", fontSize = 26.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(contact, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(online, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        EcoSectionCard {
            messages.forEach { message ->
                val bubbleColor = if (message.fromMe) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                val textColor = if (message.fromMe) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.fromMe) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 260.dp)
                            .background(bubbleColor, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                    ) {
                        Text(message.text, fontSize = 14.sp, color = textColor)
                        Text(message.time, fontSize = 10.sp, color = if (message.fromMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Escribe un mensaje…") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = Color(0xFF0B1510),
                    unfocusedContainerColor = Color(0xFF0B1510),
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
            )
            Spacer(Modifier.width(8.dp))
            androidx.compose.material3.Button(
                onClick = {
                    if (input.trim().isNotEmpty()) {
                        messages = messages + MessageItem(fromMe = true, text = input.trim(), time = "Ahora")
                        input = ""
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text("Enviar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    MaterialTheme {
        ProfileScreen(
            user = UserResponse(1, "Juan", "Pérez", "juan@mail.com", "1234567890", "Cliente"),
            onBack = {},
            onOpenNotifications = {},
            onOpenChat = {},
            onLogout = {},
        )
    }
}