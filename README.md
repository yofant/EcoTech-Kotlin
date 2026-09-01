This is a Kotlin Multiplatform project targeting Android, iOS, Web, Desktop (JVM), Server.

* [/app/iosApp](./app/iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/app/shared](./app/shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./app/shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./app/shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./app/shared/src/jvmMain/kotlin)
    folder is the appropriate location.

* [/core](./core/src) is for the code that will be shared between all targets in the project.
  The most important subfolder is [commonMain](./core/src/commonMain/kotlin). If preferred, you
  can add code to the platform-specific folders here too.

* [/server](./server/src/main/kotlin) is for the Ktor server application.

### Database configuration (server)

The server saves users to a MySQL database. It reads connection settings from these
environment variables (with the defaults shown):

| Variable       | Default     | Description                  |
|----------------|-------------|------------------------------|
| `DB_HOST`      | `localhost` | MySQL host                   |
| `DB_PORT`      | `3306`      | MySQL port                   |
| `DB_NAME`      | `EcoTech`   | Database name                |
| `DB_USER`      | `root`      | MySQL user                   |
| `DB_PASSWORD`  | *(empty)*   | MySQL password               |

Example (EC2 or local):

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=EcoTech
export DB_USER=root
export DB_PASSWORD=tu_contraseña
./gradlew :server:run
```

The server maps the app auth to your existing `Usuarios` table
(`usuario_id` PK, `nombre`, `apellido`, `email`, `telefono`, `rol`).
On startup it adds the missing `password_hash` column automatically, so the
first time you run it after this change you don't need to alter the table by hand.

> Roles accepted by registration match the `CK_Usuarios_Rol` check:
> `Auditor`, `Operador`, `Tecnico`, `Administrador`.

### API base URL (client)

The app calls the server via `AuthApi.kt` (`app/shared/src/commonMain/kotlin/com/example/ecotech/AuthApi.kt`)
at the `BASE_URL` constant (default `http://localhost:8080/api/auth`). Change it to point
to your EC2 instance, e.g. `http://TU-IP-PUBLICA:8080/api/auth`.

> Note for Android emulator: `localhost` refers to your PC, so use `http://10.0.2.2:8080/api/auth`
> when the server runs on your machine.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :app:androidApp:assembleDebug`
- Desktop app:
  - Hot reload: `./gradlew :app:desktopApp:hotRun --auto`
  - Standard run: `./gradlew :app:desktopApp:run`
- Server: `./gradlew :server:run`
- Web app:
  - Wasm target (faster, modern browsers): `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`
  - JS target (slower, supports older browsers): `./gradlew :app:webApp:jsBrowserDevelopmentRun`
- iOS app: open the [/app/iosApp](./app/iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :app:shared:testAndroidHostTest`
- Desktop tests: `./gradlew :app:shared:jvmTest`
- Server tests: `./gradlew :server:test`
- Web tests:
  - Wasm target: `./gradlew :app:shared:wasmJsTest`
  - JS target: `./gradlew :app:shared:jsTest`
- iOS tests: `./gradlew :app:shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).