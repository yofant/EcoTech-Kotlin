package com.example.ecotech

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class ConfirmationType(val emoji: String, val title: String, val message: String) {
    Success("✅", "¡Listo!", "El registro se modificó correctamente."),
    Delete("🗑️", "Registro eliminado", "El registro fue eliminado definitivamente."),
    Deactivate("🚫", "Registro inhabilitado", "El registro fue desactivado. El usuario ya no podrá acceder."),
}

@Composable
fun ConfirmationScreen(type: ConfirmationType, onDone: () -> Unit) {
    EcoBackground {
        Spacer(Modifier.height(56.dp))
        Text(type.emoji, fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        EcoTitle(type.title, fontSize = 30)
        Spacer(Modifier.height(8.dp))
        EcoSubtitle(type.message)
        Spacer(Modifier.height(32.dp))
        EcoPrimaryButton(text = "Entendido", onClick = onDone)
    }
}

@Preview(showBackground = true)
@Composable
fun ConfirmationPreview() {
    MaterialTheme {
        ConfirmationScreen(type = ConfirmationType.Success, onDone = {})
    }
}