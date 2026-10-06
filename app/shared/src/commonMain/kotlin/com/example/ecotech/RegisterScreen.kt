package com.example.ecotech

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
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

private val roles = listOf("Auditor", "Operador", "Tecnico", "Administrador")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onBackToLogin: () -> Unit, onRegisterSuccess: (UserResponse) -> Unit = {}) {
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var roleExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun validarFormulario() {
        errorMessage = when {
            name.isBlank() || lastName.isBlank() || email.isBlank() || phone.isBlank() ||
                role.isBlank() || password.isBlank() || confirmPassword.isBlank() ->
                "Completa todos los campos obligatorios"
            !email.contains("@") -> "Ingresa un correo electrónico válido"
            phone.length < 7 -> "Ingresa un número de teléfono válido"
            role.isBlank() -> "Selecciona un rol"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            password != confirmPassword -> "Las contraseñas no coinciden"
            else -> null
        }
        if (errorMessage == null) {
            scope.launch {
                isLoading = true
                errorMessage = null
                val result = AuthApi.register(
                    name = name.trim(),
                    lastName = lastName.trim(),
                    email = email.trim(),
                    phone = phone.trim(),
                    role = role.trim(),
                    password = password,
                )
                isLoading = false
                if (result.success && result.user != null) {
                    onRegisterSuccess(result.user)
                } else {
                    errorMessage = result.error
                }
            }
        }
    }

    EcoBackground {
        EcoBackHeader(title = "Crear Cuenta", onBack = onBackToLogin)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📝", fontSize = 38.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            EcoTitle(text = "Únete a EcoTech", fontSize = 30)
            EcoSubtitle(text = "Crea tu cuenta para comenzar a reciclar y gestionar equipos")

            Spacer(modifier = Modifier.height(24.dp))

            // First Name
            EcoOutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMessage = null },
                label = "Nombre",
                placeholder = "Juan",
                leadingIcon = {
                    Text("👤", fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                },
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Last Name
            EcoOutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it; errorMessage = null },
                label = "Apellido",
                placeholder = "Pérez",
                leadingIcon = {
                    Text("👤", fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                },
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Email
            EcoOutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = "Correo electrónico",
                placeholder = "juan@ecotech.com",
                leadingIcon = {
                    Text("📧", fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Phone
            EcoOutlinedTextField(
                value = phone,
                onValueChange = { phone = it; errorMessage = null },
                label = "Teléfono",
                placeholder = "+57 300 123 4567",
                leadingIcon = {
                    Text("📱", fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Role Selector Dropdown
            ExposedDropdownMenuBox(
                expanded = roleExpanded,
                onExpandedChange = { if (!isLoading) roleExpanded = it },
            ) {
                EcoOutlinedTextField(
                    value = role,
                    onValueChange = {},
                    label = "Rol de usuario",
                    placeholder = "Selecciona tu rol",
                    leadingIcon = {
                        Text("🛡️", fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    enabled = !isLoading,
                )
                ExposedDropdownMenu(
                    expanded = roleExpanded,
                    onDismissRequest = { roleExpanded = false },
                ) {
                    roles.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontWeight = FontWeight.Medium) },
                            onClick = {
                                role = option
                                roleExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Password
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
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm Password
            EcoOutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null },
                label = "Confirmar contraseña",
                placeholder = "••••••••",
                leadingIcon = {
                    Text("🔐", fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Text(if (confirmPasswordVisible) "👁️" else "🙈", fontSize = 18.sp)
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = errorMessage != null,
                supportingText = errorMessage?.let {
                    { Text(it, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                },
                enabled = !isLoading,
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Register CTA Button
            EcoPrimaryButton(
                text = "Registrar mi cuenta",
                onClick = { validarFormulario() },
                isLoading = isLoading,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBackToLogin) {
                Text(
                    text = "¿Ya tienes cuenta? ",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
                Text(
                    text = "Inicia sesión",
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
fun RegisterPreview() {
    EcoTheme {
        RegisterScreen(onBackToLogin = {})
    }
}
