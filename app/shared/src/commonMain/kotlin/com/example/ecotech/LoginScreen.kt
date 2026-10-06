package com.example.ecotech

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotPassword: () -> Unit = {},
    onLoginSuccess: (UserResponse) -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun validarFormulario() {
        errorMessage = when {
            email.isBlank() || password.isBlank() -> "Completa todos los campos"
            !email.contains("@") -> "Ingresa un correo electrónico válido"
            else -> null
        }
        if (errorMessage == null) {
            scope.launch {
                isLoading = true
                errorMessage = null
                val result = AuthApi.login(email.trim(), password)
                isLoading = false
                if (result.success && result.user != null) {
                    onLoginSuccess(result.user)
                } else {
                    errorMessage = result.error
                }
            }
        }
    }

    EcoBackground {
        EcoBackHeader(title = "Iniciar Sesión", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Header Brand Badge
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔐", fontSize = 42.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            EcoTitle(text = "¡Hola de nuevo!", fontSize = 32)
            EcoSubtitle(text = "Ingresa tus credenciales para acceder a tu cuenta")

            Spacer(modifier = Modifier.height(28.dp))

            // Email Input with Icon
            EcoOutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = "Correo electrónico",
                placeholder = "ejemplo@ecotech.com",
                leadingIcon = {
                    Text("📧", fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = errorMessage != null,
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input with Visibility Toggle
            EcoOutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = "Contraseña",
                placeholder = "••••••••",
                leadingIcon = {
                    Text("🔒", fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(if (passwordVisible) "👁️" else "🙈", fontSize = 18.sp)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = errorMessage != null,
                supportingText = errorMessage?.let {
                    { Text(it, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                },
                enabled = !isLoading,
            )

            // Forgot Password Option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onForgotPassword) {
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Login Primary Action
            EcoPrimaryButton(
                text = "Entrar a mi cuenta",
                onClick = { validarFormulario() },
                isLoading = isLoading,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary Back Option
            EcoSecondaryButton(
                text = "Volver",
                onClick = onBack,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Register Link
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = "¿Aún no tienes cuenta? ",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
                Text(
                    text = "Regístrate aquí",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    EcoTheme {
        LoginScreen(onBack = {}, onNavigateToRegister = {})
    }
}
