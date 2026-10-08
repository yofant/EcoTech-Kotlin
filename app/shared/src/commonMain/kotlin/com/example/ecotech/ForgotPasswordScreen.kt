package com.example.ecotech

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ForgotPasswordScreen(onBack: () -> Unit, onLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

    EcoBackground {
        EcoBackHeader(title = "Recuperar Contraseña", onBack = onBack)

        EcoSectionCard(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .widthIn(max = 480.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (sent) "📨" else "🔑", fontSize = 48.sp)
            }

            EcoTitle(text = if (sent) "¡Correo Enviado!" else "¿Olvidaste tu contraseña?", fontSize = 28)
            EcoEyebrow(if (sent) "Revisa tu correo" else "Recuperación segura")
            
            Spacer(Modifier.height(8.dp))

            EcoSubtitle(
                text = if (sent)
                    "Hemos enviado las instrucciones a tu correo electrónico."
                else
                    "Ingresa tu correo registrado y te enviaremos un enlace de restablecimiento."
            )

            if (sent) {
                EcoSectionCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Instrucciones enviadas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Si existe una cuenta asociada a ${email.trim()}, recibirás un mensaje con los pasos a seguir.",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        EcoPrimaryButton(
                            text = "Volver a Iniciar Sesión",
                            onClick = onLogin
                        )
                    }
                }
            } else {
                EcoOutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo electrónico",
                    placeholder = "ejemplo@ecotech.com",
                    leadingIcon = {
                        Text("📧", fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )

                Spacer(Modifier.height(24.dp))

                EcoPrimaryButton(
                    text = "Enviar enlace de recuperación",
                    onClick = { if (email.trim().isNotEmpty()) sent = true },
                    enabled = email.trim().contains("@"),
                )

                Spacer(Modifier.height(16.dp))

                EcoSecondaryButton(
                    text = "Cancelar y volver",
                    onClick = onBack
                )
            }
        }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordPreview() {
    EcoTheme {
        ForgotPasswordScreen(onBack = {}, onLogin = {})
    }
}
