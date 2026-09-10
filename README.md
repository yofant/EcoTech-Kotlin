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

### Deploying the server to EC2

1. Build a self-contained fat JAR (requires a JDK on your machine):

   ```bash
   ./gradlew :server:buildFatJar
   # artifact: server/build/libs/server-all.jar
   ```

2. Upload it to your instance, for example:

   ```bash
   scp -i TU-CLAVE.pem server/build/libs/server-all.jar ubuntu@TU-IP:~
   ```

3. On the instance, install a JDK (Amazon Corretto 21 works) and a MySQL/MariaDB
   service, then import the schema dump (`EcoTech.sql`) into a database named
   `EcoTech`. The server auto-adds the missing `password_hash` column on `Usuarios`
   on first boot.

4. Run it with the environment variables from the table above:

   ```bash
   export DB_HOST=localhost
   export DB_PORT=3306
   export DB_NAME=EcoTech
   export DB_USER=ecotech
   export DB_PASSWORD=tu_contraseña_segura
   java -jar server-all.jar
   ```

5. Open TCP port `8080` in the EC2 security group and verify:

   ```bash
   curl http://TU-IP-PUBLICA:8080/api/health
   # {"status":"ok","database":true}
   ```

To run it as a background service, create a systemd unit
(`/etc/systemd/system/ecotech.service`):

```ini
[Unit]
Description=EcoTech API
After=network.target mariadb.service

[Service]
Environment=DB_HOST=localhost
Environment=DB_NAME=EcoTech
Environment=DB_USER=ecotech
Environment=DB_PASSWORD=tu_contraseña_segura
ExecStart=/usr/bin/java -jar /home/ubuntu/server-all.jar
Restart=on-failure
User=ubuntu

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now ecotech
sudo systemctl status ecotech
```

### API endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET    | `/` | API info |
| GET    | `/api/health` | Health check (reports DB connectivity) |
| POST   | `/api/auth/register` | Register user (JSON: `name, lastName, email, phone, role, password`) |
| POST   | `/api/auth/login` | Login (JSON: `email, password`) |
| GET/POST | `/api/ciudades` | List / create cities |
| PUT/DELETE | `/api/ciudades/{id}` | Update / delete a city |
| GET/POST | `/api/tipos-equipo` | List / create equipment types |
| PUT/DELETE | `/api/tipos-equipo/{id}` | Update / delete an equipment type |
| GET/POST | `/api/donantes` | List / create donors |
| PUT/DELETE | `/api/donantes/{id}` | Update / delete a donor |
| GET/POST | `/api/equipos` | List (`?estado=`) / create equipment |
| PUT/DELETE | `/api/equipos/{id}` | Update / delete equipment |
| GET/POST | `/api/beneficiarios` | List / create beneficiaries |
| PUT/DELETE | `/api/beneficiarios/{id}` | Update / delete a beneficiary |
| GET/POST | `/api/diagnosticos` | List / create diagnostics |
| PUT | `/api/diagnosticos/{id}` | Update a diagnostic |
| GET/POST | `/api/reparaciones` | List / create repairs |
| PUT | `/api/reparaciones/{id}` | Update a repair |
| GET/POST | `/api/entregas` | List / create deliveries |
| PUT | `/api/entregas/{id}` | Update a delivery |
| GET/PUT | `/api/usuarios` | List / update users |
| PATCH | `/api/usuarios/{id}/estado` | Enable/disable a user (JSON: `{ "activo": bool }`) |
| GET    | `/api/auditoria` | Audit log |
| GET    | `/api/stats/resumen` | Global stats |
| GET    | `/api/stats/actividad-mensual` | Monthly activity (`?mes=YYYY-MM`) |

All timestamps are serialized as `yyyy-MM-dd HH:mm:ss`. `Auditoria` is written
automatically by the server on every INSERT/UPDATE/DELETE.

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