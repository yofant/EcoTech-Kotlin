package com.example.ecotech

import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertTrue

class DatabaseConnectionTest {
    @Test
    fun testDatabaseConnection() {
        println("Iniciando prueba de conexión con la base de datos...")
        DatabaseFactory.init(throwOnError = true)
        assertTrue(DatabaseFactory.isConnected, "La base de datos debería estar conectada")
        transaction {
            exec("SELECT 1")
        }
        println("🎉 ¡Conexión con MySQL en AWS establecida y verificada exitosamente!")
    }
}

