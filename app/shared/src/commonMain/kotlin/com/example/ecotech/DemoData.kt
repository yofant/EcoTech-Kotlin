package com.example.ecotech

data class ProductItem(
    val name: String,
    val price: Int,
    val category: String,
    val condition: String,
    val emoji: String,
)

data class CollectionPointItem(
    val name: String,
    val distanceKm: String,
    val rating: Double,
    val address: String,
)

data class NotificationItem(
    val title: String,
    val body: String,
    val time: String,
)

data class MessageItem(
    val fromMe: Boolean,
    val text: String,
    val time: String,
)

data class RecordItem(
    val name: String,
    val email: String,
    val totalOrders: Int,
)

object DemoData {

    val categories = listOf(
        "📱 Celulares", "💻 Computadores", "🖥️ Monitores",
        "⌨️ Accesorios", "🎧 Audífonos", "🕹️ Consolas",
    )

    val featuredProducts = listOf(
        ProductItem("iPhone 12 128GB", 2200000, "Celulares", "Como nuevo", "📱"),
        ProductItem("MacBook Air M1", 3800000, "Computadores", "Usado", "💻"),
        ProductItem("Monitor LG 24\"", 650000, "Monitores", "Como nuevo", "🖥️"),
        ProductItem("AirPods Pro", 780000, "Audífonos", "Nuevo", "🎧"),
        ProductItem("PlayStation 5", 2900000, "Consolas", "Como nuevo", "🕹️"),
    )

    val promotions = listOf(
        ProductItem("iPhone 11 64GB", 1350000, "Celulares", "Promoción", "📱"),
        ProductItem("Dell XPS 13", 2900000, "Computadores", "Promoción", "💻"),
    )

    val recommendations = listOf(
        ProductItem("iPad 9 generación", 1600000, "Tablets", "Como nuevo", "📲"),
        ProductItem("Teclado mecánico", 280000, "Accesorios", "Nuevo", "⌨️"),
    )

    val lowStock = listOf(
        ProductItem("Samsung S21", 1900000, "Celulares", "Pocas unidades", "📱"),
    )

    val collectionPoints = listOf(
        CollectionPointItem("Unilago", "1.2 km", 4.6, "Cra. 69 #14-55, Bogotá"),
        CollectionPointItem("Centro Comercial Gran Estación", "3.4 km", 4.4, "Cra. 60 #1-17, Bogotá"),
        CollectionPointItem("San Victorino", "5.1 km", 4.1, "Av. Carrera 10, Bogotá"),
    )

    val notifications = listOf(
        NotificationItem("Pedido confirmado", "Tu pedido de iPhone 12 ya está en preparación.", "Hace 2 h"),
        NotificationItem("Nueva promoción", "20% de descuento en computadores reacondicionados.", "Hace 5 h"),
        NotificationItem("Punto de recolección", "Unilago actualizó su horario de atención.", "Ayer"),
        NotificationItem("Trueque disponible", "Un vendedor acepta tu oferta de trueque.", "Hace 2 días"),
    )

    val chatMessages = listOf(
        MessageItem(false, "Hola, ¿el iPhone 12 sigue disponible?", "10:05"),
        MessageItem(true, "¡Hola! Sí, está disponible y en excelente estado.", "10:08"),
        MessageItem(false, "¿Aceptan trueque por una Nintendo Switch?", "10:10"),
        MessageItem(true, "Sí, podemos coordinar esa opción.", "10:12"),
        MessageItem(false, "Perfecto, ¿cuándo nos vemos en Unilago?", "10:15"),
    )

    val soldProducts = listOf(
        ProductItem("iPhone 11 64GB", 1350000, "Celulares", "Vendido", "📱"),
        ProductItem("Galaxy A52", 850000, "Celulares", "Vendido", "📱"),
        ProductItem("HP Pavilion 15", 1200000, "Computadores", "Vendido", "💻"),
    )

    val boughtProducts = listOf(
        ProductItem("iPad 9 generación", 1600000, "Tablets", "Entregado", "📲"),
        ProductItem("Teclado mecánico", 280000, "Accesorios", "Entregado", "⌨️"),
    )

    val clientRecords = listOf(
        RecordItem("María López", "maria@mail.com", 4),
        RecordItem("Carlos Ruiz", "carlos@mail.com", 2),
        RecordItem("Ana Torres", "ana@mail.com", 7),
    )

    val userList = listOf(
        RecordItem("María López", "maria@mail.com", 4),
        RecordItem("Carlos Ruiz", "carlos@mail.com", 2),
        RecordItem("Ana Torres", "ana@mail.com", 7),
        RecordItem("Pedro Gil", "pedro@mail.com", 0),
    )

    val deliveries = listOf(
        RecordItem("Envío #1023", "iPhone 12 → Cliente", 3),
        RecordItem("Envío #1024", "MacBook Air → Cliente", 2),
        RecordItem("Envío #1025", "Monitor LG → Cliente", 1),
    )

    val events = listOf(
        RecordItem("Recolección Unilago", "12 equipos recolectados", 1),
        RecordItem("Recolección Gran Estación", "8 equipos recolectados", 1),
    )
}