package com.example.ecotech

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun ForgotPasswordScreen(onBack: () -> Unit, onLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

    EcoBackground {
        EcoBackHeader(title = "Recuperar contraseña", onBack = onBack)
        Spacer(Modifier.height(24.dp))
        Text("🔐", fontSize = 56.sp)
        Spacer(Modifier.height(8.dp))
        EcoTitle("¿Olvidaste tu\ncontraseña?", fontSize = 28)
        Spacer(Modifier.height(8.dp))
        EcoSubtitle("Ingresa tu correo y te enviaremos un enlace para restablecer tu contraseña.")
        Spacer(Modifier.height(24.dp))

        if (sent) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White.copy(alpha = 0.95f), shape = RoundedCornerShape(16.dp))
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("📨", fontSize = 40.sp)
                    Text(
                        "Correo enviado",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = EcoGreenDark,
                    )
                    Text(
                        "Si existe una cuenta con ${email.trim()}, recibirás las instrucciones para restablecer tu contraseña.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                    )
                    Spacer(Modifier.height(8.dp))
                    EcoPrimaryButton(text = "Volver a iniciar sesión", onClick = onLogin)
                }
            }
        } else {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                ),
            )
            Spacer(Modifier.height(24.dp))
            EcoPrimaryButton(
                text = "Enviar enlace",
                onClick = { if (email.trim().isNotEmpty()) sent = true },
                enabled = email.trim().isNotEmpty(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordPreview() {
    MaterialTheme {
        ForgotPasswordScreen(onBack = {}, onLogin = {})
    }
}