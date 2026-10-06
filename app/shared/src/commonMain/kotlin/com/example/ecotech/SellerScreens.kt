package com.example.ecotech

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SellerHomeScreen(
    user: UserResponse?,
    onLogout: () -> Unit,
    onOpenSold: () -> Unit,
    onOpenBought: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    EcoBackground {
        EcoTitle(text = "🧑‍💼 Panel Vendedor", fontSize = 28, topPadding = 4)
        Spacer(Modifier.height(4.dp))
        EcoSubtitle("¡Hola, ${user?.name ?: "Vendedor"}! Gestiona tus equipos y ventas.")
        Spacer(Modifier.height(8.dp))

        EcoBody {
            EcoSectionTitle("📊 Resumen rápido")
            EcoSectionCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricBox("Vendidos", "24", "📦", Modifier.weight(1f))
                    MetricBox("Publicados", "12", "🏷️", Modifier.weight(1f))
                    MetricBox("Clientes", "18", "👥", Modifier.weight(1f))
                }
            }

            EcoSectionTitle("⚙️ Gestión")
            EcoSectionCard {
                EcoNavItem("🧾", "Productos vendidos", "Historial de equipos vendidos", onClick = onOpenSold)
                EcoNavItem("🛍️", "Equipos comprados", "Galería de compras", onClick = onOpenBought)
                EcoNavItem("👥", "Registros y clientes", "Asociados a tus productos", onClick = onOpenRecords)
                EcoNavItem("📈", "Estadísticas", "Ventas y métricas de rendimiento", onClick = onOpenStats)
                EcoNavItem("💬", "Chats", "Conversaciones con clientes", onClick = onOpenChat)
                EcoNavItem("🔔", "Notificaciones", "Alertas de ventas y pedidos", onClick = onOpenNotifications)
            }

            EcoSectionTitle("🕘 Historial reciente")
            EcoSectionCard {
                EcoNavItem("📱", "iPhone 11 vendido", "María López • Hace 2 días", onClick = onOpenSold)
                EcoNavItem("💻", "HP Pavilion 15 vendido", "Carlos Ruiz • Hace 1 semana", onClick = onOpenSold)
            }

            Spacer(Modifier.height(4.dp))
            EcoSecondaryButton(text = "Cerrar sesión", onClick = onLogout)
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .height(86.dp)
            .padding(horizontal = 4.dp)
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(8.dp),
    ) {
        Text(emoji, fontSize = 20.sp)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ProductGallery(title: String, subtitle: String, products: List<ProductItem>, onBack: () -> Unit) {
    EcoBackground {
        EcoBackHeader(title = title, onBack = onBack)
        Spacer(Modifier.height(12.dp))
        EcoSubtitle(subtitle)
        Spacer(Modifier.height(8.dp))

        EcoBody {
            EcoSectionCard {
                if (products.isEmpty()) {
                    Text("No hay equipos registrados", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    products.forEach { ProductRow(it) }
                }
            }
        }
    }
}

@Composable
fun SoldProductsScreen(onBack: () -> Unit) {
    ProductGallery(
        title = "Vendidos",
        subtitle = "Galería de equipos que ya vendiste.",
        products = DemoData.soldProducts,
        onBack = onBack,
    )
}

@Composable
fun BoughtProductsScreen(onBack: () -> Unit) {
    ProductGallery(
        title = "Comprados",
        subtitle = "Equipos que has adquirido.",
        products = DemoData.boughtProducts,
        onBack = onBack,
    )
}

@Composable
fun CustomerRecordsScreen(onBack: () -> Unit) {
    EcoBackground {
        EcoBackHeader(title = "Registros y clientes", onBack = onBack)
        Spacer(Modifier.height(12.dp))
        EcoSubtitle("Registros asociados a tus productos y clientes.")
        Spacer(Modifier.height(8.dp))

        EcoBody {
            EcoSectionCard {
                DemoData.clientRecords.forEach { record ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("🧾", fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(record.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            Text(record.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${record.totalOrders} pedidos", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@Composable
fun SellerStatsScreen(onBack: () -> Unit) {
    val salesByMonth = listOf(12, 18, 15, 24, 20, 28)
    val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun")

    EcoBackground {
        EcoBackHeader(title = "Estadísticas", onBack = onBack)
        Spacer(Modifier.height(12.dp))
        EcoSubtitle("Rendimiento comercial de tu tienda.")
        Spacer(Modifier.height(8.dp))

        EcoBody {
            EcoSectionTitle("📈 Clientes y ventas")
            EcoSectionCard {
                MetricRow("Clientes", "18")
                MetricRow("Ventas del mes", "28")
                MetricRow("Equipos vendidos", "24")
                MetricRow("Equipos reciclados", "9")
            }

            EcoSectionTitle("📊 Ventas por mes")
            EcoSectionCard {
                val maxSales = salesByMonth.maxOrNull() ?: 1
                salesByMonth.forEachIndexed { index, sales ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(months[index], fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(36.dp))
                        Box(
                            modifier = Modifier
                                .height(18.dp)
                                .fillMaxWidth((sales.toFloat() / maxSales) * 0.75f)
                                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(4.dp)),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("$sales", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            EcoSectionTitle("🎯 Métricas de rendimiento")
            EcoSectionCard {
                MetricRow("Valoración promedio", "4.7 ★")
                MetricRow("Tiempo de venta", "6 días")
                MetricRow("Índice de satisfacción", "95%")
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
fun SellerHomePreview() {
    MaterialTheme {
        SellerHomeScreen(
            user = null,
            onLogout = {},
            onOpenSold = {},
            onOpenBought = {},
            onOpenRecords = {},
            onOpenStats = {},
            onOpenChat = {},
            onOpenNotifications = {},
        )
    }
}