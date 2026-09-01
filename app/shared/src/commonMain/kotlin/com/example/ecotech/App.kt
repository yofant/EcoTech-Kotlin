package com.example.ecotech

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

enum class Screen {
    Welcome,
    Login,
    Register,
    Home
}

@Composable
fun App() {
    MaterialTheme {
        var screen by remember { mutableStateOf(Screen.Welcome) }
        var currentUser by remember { mutableStateOf<UserResponse?>(null) }

        when (screen) {
            Screen.Welcome -> WelcomeScreen(
                onContinue = { screen = Screen.Login },
            )
            Screen.Login -> LoginScreen(
                onBack = { screen = Screen.Welcome },
                onNavigateToRegister = { screen = Screen.Register },
                onLoginSuccess = { user ->
                    currentUser = user
                    screen = Screen.Home
                }
            )
            Screen.Register -> RegisterScreen(
                onBackToLogin = { screen = Screen.Login },
                onRegisterSuccess = { user ->
                    currentUser = user
                    screen = Screen.Home
                }
            )
            Screen.Home -> HomeScreen(
                user = currentUser,
                onLogout = { screen = Screen.Login }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    App()
}
