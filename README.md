# EcoTech

EcoTech es una aplicación multiplataforma para la gestión de equipos electrónicos reutilizables. Permite publicar y explorar equipos, coordinarse mediante conversaciones, registrar recepción, diagnóstico, reparación y entrega, y administrar usuarios y operaciones según el cargo.

## Documentación

- [Documentación técnica de la aplicación](./docs/DOCUMENTACION_TECNICA.md): arquitectura, lenguajes, base de datos de 14 tablas, autenticación, autorización, auditoría, funciones por cargo, API, ejecución y límites conocidos.
- [Flujos de negocio](./.github/modernize/assessment/engines/facts/business-workflows.md): entidades de dominio, procesos principales, reglas y diagrama de secuencia.

## Componentes y lenguajes

El proyecto usa Kotlin en la aplicación multiplataforma y en el backend. La UI se implementa con Compose Multiplatform; el servidor HTTP, con Ktor. El cliente está configurado para Android, JVM/Desktop, navegador mediante JavaScript y WebAssembly, e incluye targets de framework compartido para iOS. En esta configuración no hay un módulo de aplicación iOS incluido en Gradle.

La persistencia está implementada con MySQL, JDBC, HikariCP y Exposed. La guía técnica describe el esquema, cómo ejecutar cada componente y qué tareas usar para validar el proyecto.

## Configuración del backend

El servidor se configura con variables de entorno. Define las credenciales fuera del código y no las incluyas en Git:

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=ecotech
export DB_USER=ecotech_app
export DB_PASSWORD='valor-configurado-en-tu-entorno'
./gradlew :server:run
```

La configuración efectiva y las limitaciones de los valores alternativos que conserva el código están descritas en la [guía técnica](./docs/DOCUMENTACION_TECNICA.md#configuracion-y-arranque).

## Comandos frecuentes

```bash
# Backend
./gradlew :server:run
./gradlew :server:test

# UI compartida en JVM
./gradlew :app:shared:jvmTest

# Android
./gradlew :app:androidApp:assembleDebug

# Desktop
./gradlew :app:desktopApp:run

# Web (elige JavaScript o WebAssembly)
./gradlew :app:webApp:jsBrowserDevelopmentRun
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun
```

El endpoint público de estado es `GET /api/health`. Devuelve el estado del servicio y el resultado de una comprobación de base de datos; consulta la guía técnica para interpretar el resultado y para el catálogo de rutas por área.
