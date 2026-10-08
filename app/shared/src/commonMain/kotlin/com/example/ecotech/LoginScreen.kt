package com.example.ecotech

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

        EcoSectionCard(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .widthIn(max = 480.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "🔐", fontSize = 34.sp)
                }

                EcoEyebrow("Acceso seguro")
                EcoTitle(text = "¡Hola de nuevo!", fontSize = 30)
                EcoSubtitle(text = "Ingresa tus credenciales para acceder a tu cuenta")

                Spacer(Modifier.height(8.dp))

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

                EcoOutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = "Contraseña",
                    placeholder = "••••••••",
                    leadingIcon = {
                        Text("🔒", fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = !isLoading) {
                            Text(if (passwordVisible) "👁️" else "🙈", fontSize = 18.sp)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    },
                    enabled = !isLoading,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onForgotPassword, enabled = !isLoading) {
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                EcoPrimaryButton(
                    text = "Entrar a mi cuenta",
                    onClick = { validarFormulario() },
                    isLoading = isLoading,
                    enabled = !isLoading,
                )

                TextButton(onClick = onNavigateToRegister, enabled = !isLoading) {
                    Text(
                        text = "¿Aún no tienes cuenta? ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = "Regístrate aquí",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
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
