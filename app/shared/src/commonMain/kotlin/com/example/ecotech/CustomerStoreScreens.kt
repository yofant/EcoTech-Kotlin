package com.example.ecotech

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomerHomeScreen(
    user: UserResponse?,
    onLogout: () -> Unit,
    onOpenCatalog: () -> Unit,
    onSelectCollectionPoint: () -> Unit,
    onCheckout: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenChat: () -> Unit,
) {
    EcoBackground {
        EcoRoleHero(
            kicker = "Comunidad EcoTech",
            title = "Hola, ${user?.name ?: "Usuario"}",
            subtitle = "Encuentra tecnología, conversa con vendedores y coordina tus entregas.",
            icon = "♻️",
        )

        EcoBody {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                EcoStatCard(
                    emoji = "💻",
                    title = "Equipos",
                    value = "${DemoData.featuredProducts.size}",
                    modifier = Modifier.weight(1f),
                )
                EcoStatCard(
                    emoji = "📍",
                    title = "Puntos",
                    value = "${DemoData.collectionPoints.size}",
                    modifier = Modifier.weight(1f),
                )
                EcoStatCard(
                    emoji = "💬",
                    title = "Chats",
                    value = "1",
                    modifier = Modifier.weight(1f),
                )
            }

            EcoSectionTitle("📂 Categorías")
            EcoSectionCard {
                DemoData.categories.forEach { category ->
                    EcoNavItem(emoji = category.take(2), title = category.drop(2), onClick = onOpenCatalog)
                }
            }

            EcoSectionTitle("⚡ Accesos rápidos")
            EcoSectionCard {
                EcoNavItem("🔎", "Buscar catálogo", "Productos, promociones y sugerencias", onClick = onOpenCatalog)
                EcoNavItem("📍", "Punto de recolección", "Unilago • 1.2 km", onClick = onSelectCollectionPoint)
                EcoNavItem("🛒", "Mi pedido", "Resumen y pago", onClick = onCheckout)
                EcoNavItem("📦", "Mis pedidos y trueques", "Historial y seguimiento", onClick = onOpenProfile)
            }

            EcoSectionTitle("⭐ Destacados")
            EcoSectionCard {
                DemoData.featuredProducts.forEach { product ->
                    ProductRow(product)
                }
            }

            EcoSectionTitle("💬 Atención al usuario")
            EcoSectionCard {
                EcoNavItem("💬", "Contactar a un vendedor", "Resuelve dudas sobre productos y pedidos", onClick = onOpenChat)
                EcoNavItem("🔔", "Mis notificaciones", "Novedades de pedidos y promociones", onClick = onOpenNotifications)
            }

            Spacer(Modifier.height(8.dp))
            EcoPrimaryButton(text = "Ver catálogo completo", onClick = onOpenCatalog)
            EcoSecondaryButton(text = "Ver mi perfil", onClick = onOpenProfile)
            EcoSecondaryButton(text = "Cerrar sesión", onClick = onLogout)
        }
    }
}

@Composable
fun ProductRow(product: ProductItem, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(48.dp)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(product.emoji, fontSize = 24.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(product.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "${product.condition} • ${product.category}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            "$${product.price.let { price -> "$price" }}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun CatalogScreen(onBack: () -> Unit, onCheckout: () -> Unit) {
    var query by remember { mutableStateOf("") }

    EcoBackground {
        EcoBackHeader(title = "Catálogo", onBack = onBack)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Buscar productos…") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = Color(0xFF0B1510),
                unfocusedContainerColor = Color(0xFF0B1510),
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
        )

        Spacer(Modifier.height(4.dp))
        EcoBody {
            EcoSectionTitle("🔥 Promociones")
            EcoSectionCard { DemoData.promotions.forEach { ProductRow(it) } }

            EcoSectionTitle("💡 Sugerencias")
            EcoSectionCard { DemoData.recommendations.forEach { ProductRow(it) } }

            EcoSectionTitle("⚠️ Pocas unidades")
            EcoSectionCard {
                DemoData.lowStock.forEach { ProductRow(it) }
                val total = DemoData.featuredProducts + DemoData.promotions
                val filtered = total.filter {
                    query.isBlank() ||
                        it.name.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
                }
                if (query.isNotBlank()) {
                    Text(
                        "Resultados para «$query»: ${filtered.size}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            EcoPrimaryButton(text = "Ir a resumen del pedido", onClick = onCheckout)
        }
    }
}

@Composable
fun CollectionPointScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    var selected by remember { mutableStateOf(DemoData.collectionPoints.first().name) }

    EcoBackground {
        EcoBackHeader(title = "Punto de recolección", onBack = onBack)
        Spacer(Modifier.height(12.dp))
        EcoSubtitle("Selecciona dónde entregaremos tu equipo reciclado o recogerás tu compra.")
        Spacer(Modifier.height(8.dp))

        EcoBody {
            EcoSectionCard {
                DemoData.collectionPoints.forEach { point ->
                    val isSelected = selected == point.name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(point.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(point.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "📍 ${point.distanceKm}  ⭐ ${point.rating}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                        if (isSelected) {
                            Text("✓", fontSize = 22.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            EcoPrimaryButton(text = "Continuar", onClick = { onContinue() })
        }
    }
}

@Composable
fun OrderSummaryScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    val subtotal = DemoData.featuredProducts.take(2).sumOf { it.price }
    val discount = (subtotal * 0.10).toInt()
    val tax = ((subtotal - discount) * 0.19).toInt()
    val total = subtotal - discount + tax + 8000

    EcoBackground {
        EcoBackHeader(title = "Resumen del pedido", onBack = onBack)
        Spacer(Modifier.height(12.dp))

        EcoBody {
            EcoSectionTitle("🛒 Productos")
            EcoSectionCard {
                ProductRow(DemoData.featuredProducts[0])
                ProductRow(DemoData.featuredProducts[1])
            }

            EcoSectionTitle("📍 Dirección y entrega")
            EcoSectionCard {
                InfoLine("Dirección", "Cra. 11 #71-55, Bogotá")
                InfoLine("Entrega", "En punto de recolección • Unilago")
                InfoLine("Vendedor", "TechVerde Store")
            }

            EcoSectionTitle("💳 Pago y total")
            EcoSectionCard {
                InfoLine("Subtotal", "$subtotal")
                InfoLine("Promoción (-10%)", "-$$discount")
                InfoLine("Impuestos (19%)", "+$$tax")
                InfoLine("Transporte", "+8.000")
                Text(
                    "TOTAL  $$total",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            }

            Spacer(Modifier.height(4.dp))
            EcoPrimaryButton(text = "Continuar al pago", onClick = onContinue)
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PaymentScreen(onBack: () -> Unit, onConfirm: () -> Unit) {
    var selected by remember { mutableStateOf("Tarjeta") }
    val methods = listOf(
        "💵 Efectivo" to "Paga al recibir tu equipo",
        "💳 Tarjeta" to "Crédito o débito",
        "🔄 Trueque" to "Intercambia tu equipo",
    )

    EcoBackground {
        EcoBackHeader(title = "Método de pago", onBack = onBack)
        Spacer(Modifier.height(12.dp))
        EcoSubtitle("Elige cómo quieres pagar tu pedido.")
        Spacer(Modifier.height(8.dp))

        EcoBody {
            EcoSectionCard {
                methods.forEach { (label, description) ->
                    EcoCheckboxRow(
                        label = "$label  —  $description",
                        checked = selected == label,
                        onClick = { selected = label },
                    )
                }
            }

            if (selected == "💳 Tarjeta") {
                EcoSectionTitle("Datos de la tarjeta")
                EcoSectionCard {
                    OutlinedTextField(
                        value = "**** **** **** 1234",
                        onValueChange = {},
                        label = { Text("Número de tarjeta") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = "12/28",
                        onValueChange = {},
                        label = { Text("Vencimiento") },
                        singleLine = true,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            EcoPrimaryButton(text = "Confirmar compra", onClick = onConfirm)
        }
    }
}

@Composable
fun OrderConfirmationScreen(onHome: () -> Unit, onOpenProfile: () -> Unit) {
    EcoBackground {
        EcoBody {
            Spacer(Modifier.height(48.dp))
            Text("✅", fontSize = 72.sp)
            Spacer(Modifier.height(8.dp))
            EcoTitle("¡Compra exitosa!", fontSize = 30)
            Spacer(Modifier.height(8.dp))
            EcoSubtitle("Tu pedido fue confirmado. Recibirás una notificación cuando esté listo para entrega en tu punto de recolección.")
            Spacer(Modifier.height(16.dp))

            EcoSectionCard {
                InfoLine("Orden", "#Eco-2026-0842")
                InfoLine("Punto de recolección", "Unilago")
                InfoLine("Estado", "En preparación")
            }

            Spacer(Modifier.height(16.dp))
            EcoPrimaryButton(text = "Ir al inicio", onClick = onHome)
            EcoSecondaryButton(text = "Ver mi perfil", onClick = onOpenProfile)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomerHomePreview() {
    MaterialTheme {
        CustomerHomeScreen(
            user = null,
            onLogout = {},
            onOpenCatalog = {},
            onSelectCollectionPoint = {},
            onCheckout = {},
            onOpenProfile = {},
            onOpenNotifications = {},
            onOpenChat = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentPreview() {
    MaterialTheme {
        PaymentScreen(onBack = {}, onConfirm = {})
    }
}