package com.example.ecotech

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(user: UserResponse?, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B5E20), Color(0xFF66BB6A))
                )
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "♻️", fontSize = 96.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "¡Bienvenido, ${user?.name ?: "amigo"}!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${user?.name} ${user?.lastName}",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.9f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = user?.email ?: "",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = user?.phone ?: "",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Rol: ${user?.role ?: ""}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1B5E20),
            ),
        ) {
            Text(text = "Cerrar sesión", fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            user = UserResponse(
                id = 1,
                name = "Juan",
                lastName = "Pérez",
                email = "juan@mail.com",
                phone = "1234567890",
                role = "Usuario",
            ),
            onLogout = {}
        )
    }
}
