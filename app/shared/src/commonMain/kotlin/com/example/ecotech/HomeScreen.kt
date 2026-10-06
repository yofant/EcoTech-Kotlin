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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(user: UserResponse?, onLogout: () -> Unit) {
    EcoBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Profile Header Badge
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌱", fontSize = 52.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            EcoTitle(text = "¡Bienvenido, ${user?.name ?: "Amigo"}!", fontSize = 30)
            EcoSubtitle(text = "Panel de control personal en EcoTech")

            Spacer(modifier = Modifier.height(24.dp))

            // User Info Section Card
            EcoSectionCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Información del Usuario",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        EcoStatusChip(
                            text = user?.role ?: "Usuario",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    UserDetailRow(label = "Nombre completo", value = "${user?.name ?: ""} ${user?.lastName ?: ""}")
                    UserDetailRow(label = "Correo electrónico", value = user?.email ?: "No registrado")
                    UserDetailRow(label = "Teléfono de contacto", value = user?.phone ?: "No registrado")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Eco Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EcoStatCard(
                    emoji = "📦",
                    title = "Reciclados",
                    value = "12 ítems",
                    subtitle = "+2 este mes",
                    modifier = Modifier.weight(1f)
                )
                EcoStatCard(
                    emoji = "⭐",
                    title = "Puntos Eco",
                    value = "450 pts",
                    subtitle = "Nivel Verde",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Action
            EcoSecondaryButton(
                text = "Cerrar sesión",
                onClick = onLogout
            )
        }
    }
}

@Composable
private fun UserDetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    EcoTheme {
        HomeScreen(
            user = UserResponse(
                id = 1,
                name = "Juan",
                lastName = "Pérez",
                email = "juan@mail.com",
                phone = "1234567890",
                role = "Usuario",
            ),
            onLogout = {}
        )
    }
}
