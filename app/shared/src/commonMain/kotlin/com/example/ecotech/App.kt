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
}

@Composable
fun App() {
    MaterialTheme {
        var screen by remember { mutableStateOf(Screen.Welcome) }
        when (screen) {
            Screen.Welcome -> WelcomeScreen(
                onContinue = { screen = Screen.Login },
            )
            Screen.Login -> LoginScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    App()
}
