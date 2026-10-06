package com.example.ecotech

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    val themeOverride = when (System.getenv("ECOTECH_THEME")?.lowercase()) {
        "dark" -> true
        "light" -> false
        else -> null
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "EcoTech",
    ) {
        App(darkTheme = themeOverride)
    }
}