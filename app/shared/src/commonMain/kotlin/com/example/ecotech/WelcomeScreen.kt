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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    EcoBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Eco Hero Badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "♻️", fontSize = 64.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                EcoEyebrow("Tecnología con segunda vida")
                EcoTitle(text = "EcoTech", fontSize = 42)
                
                Spacer(modifier = Modifier.height(8.dp))

                EcoSubtitle(
                    text = "Transforma tu tecnología antigua en impacto positivo para el planeta."
                )
            }

            // Feature Highlights Card
            EcoSectionCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    FeatureRow(emoji = "🌱", title = "Reciclaje tecnológico", desc = "Dale una segunda vida a tus componentes")
                    FeatureRow(emoji = "🎁", title = "Beneficios y puntos", desc = "Gana incentivos por cada dispositivo reciclado")
                    FeatureRow(emoji = "🚚", title = "Puntos de entrega", desc = "Encuentra el centro de recolección más cercano")
                }
            }

            // CTA Button
            Column(modifier = Modifier.fillMaxWidth()) {
                EcoPrimaryButton(
                    text = "Comenzar ahora ➔",
                    onClick = onContinue
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FeatureRow(emoji: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 18.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    EcoTheme {
        WelcomeScreen(onContinue = {})
    }
}
