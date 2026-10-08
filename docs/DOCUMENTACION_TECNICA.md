# Documentación técnica de EcoTech

## 1. Propósito y alcance

EcoTech es una aplicación para gestionar la reutilización y entrega de equipos electrónicos. Facilita la publicación y exploración de equipos, el contacto entre usuarios, vendedores y operadores, la gestión de donantes y beneficiarios, el diagnóstico y reparación técnica, la administración de puntos de recolección y la consulta de actividad y auditoría.

Esta guía describe la implementación presente en el repositorio: módulos, lenguajes, datos, procesos, autenticación, autorización y operaciones de backend. Las funcionalidades heredadas de pantallas de demostración no deben confundirse con operaciones persistidas: las operaciones descritas como persistentes son las que el cliente `PortalApi` conecta con la API Ktor.

## 2. Lenguajes, frameworks y módulos

| Área | Lenguaje | Tecnologías | Responsabilidad |
|---|---|---|---|
| Interfaz multiplataforma | Kotlin | Kotlin Multiplatform, Compose Multiplatform, Material 3 | Pantallas compartidas, navegación y paneles por cargo. |
| Cliente HTTP | Kotlin | Ktor Client, kotlinx.serialization | Solicitudes JSON a la API; agrega el token Bearer a las operaciones autenticadas. |
| Backend/API | Kotlin/JVM | Ktor Server, Netty, kotlinx.serialization | Autenticación, autorización, validación HTTP, coordinación de repositorios y respuestas REST/JSON. |
| Acceso a datos | Kotlin/JVM | Exposed, JDBC, HikariCP | Mapeo del esquema relacional, consultas y transacciones. |
| Base de datos | SQL / MySQL | MySQL Connector/J | Almacenamiento relacional y restricciones. |
| Configuración de compilación | Kotlin DSL | Gradle, version catalog | Configuración de módulos, targets y dependencias. |

### Targets del cliente

El módulo `app/shared` declara targets para Android, JVM, navegador JavaScript, navegador WebAssembly y frameworks compartidos iOS (`iosArm64` e `iosSimulatorArm64`). Los módulos ejecutables incluidos por Gradle son Android, Desktop, Web y Server. Los targets iOS de la biblioteca compartida no implican que este repositorio incluya actualmente una aplicación iOS completa: no hay un módulo `iosApp` incluido en `settings.gradle.kts`.

### Estructura relevante

- `app/shared`: pantallas Compose y estado/navegación compartidos.
- `app/androidApp`: punto de entrada Android.
- `app/desktopApp`: punto de entrada JVM/Desktop.
- `app/webApp`: puntos de entrada web JavaScript y Wasm.
- `core`: código Kotlin común compartido por la aplicación y el backend.
- `server`: aplicación Ktor, rutas, modelos, repositorios y acceso a MySQL.

## 3. Arquitectura y responsabilidades

La aplicación sigue una arquitectura cliente-servidor:

1. La UI Compose presenta el panel asociado al cargo autenticado.
2. Las clases cliente (`AuthApi` y `PortalApi`) serializan datos JSON y envían HTTP.
3. Ktor recibe la solicitud y aplica autenticación Bearer y reglas de autorización.
4. Las rutas validan la forma de las solicitudes y delegan en repositorios.
5. Los repositorios ejecutan operaciones Exposed dentro de transacciones JDBC.
6. MySQL conserva usuarios, inventario, operación, conversaciones, historial y auditoría.
7. La respuesta JSON vuelve al cliente, que actualiza el estado observable del panel.

El servidor también contiene lógica de agregación para dashboard, portal y conversación. El cliente y el backend están en Kotlin, pero se ejecutan en runtimes diferentes; el contrato entre ellos es HTTP/JSON, no una llamada directa a los repositorios del servidor.

## 4. Base de datos: `ecotech` y sus 14 tablas

El servidor usa MySQL a través del catálogo configurado por `DB_NAME` (el valor alternativo presente en el código es `ecotech`). En la terminología habitual de MySQL, el catálogo/base de datos contiene el esquema relacional. No hay una versión de MySQL declarada por el proyecto; el conector configurado es MySQL Connector/J.

El modelo Exposed de `DatabaseFactory.kt` declara **14 tablas**:

| Tabla | Propósito |
|---|---|
| `Ciudades` | Catálogo de ciudades y departamentos; referencia geográfica de donantes, beneficiarios y puntos. |
| `TiposEquipo` | Catálogo de categorías de equipos. |
| `Donantes` | Personas u organizaciones que aportan equipos. |
| `Usuarios` | Cuentas, credenciales cifradas, cargo, activación y datos de contacto. |
| `Beneficiarios` | Personas registradas para recibir equipos. |
| `Equipos` | Inventario de equipos, procedencia, publicación y estado operacional. |
| `Diagnosticos` | Evaluaciones técnicas vinculadas a un equipo y un técnico. |
| `Reparaciones` | Trabajo técnico, repuestos, costo y estado de reparación. |
| `Entregas` | Registro de entrega de un equipo a un beneficiario. |
| `Auditoria` | Eventos de operación administrativa y del API, con detalle y campos disponibles para snapshots. |
| `AppSessions` | Huellas de tokens de sesión, usuario asociado y vencimiento. |
| `Conversaciones` | Conversaciones entre dos usuarios, clasificadas por contexto y, opcionalmente, equipo. |
| `Mensajes` | Mensajes de una conversación y su estado de lectura. |
| `PuntosRecoleccion` | Puntos públicos de recepción/recolección, horarios y estado activo. |

### Relaciones de negocio

- Una ciudad puede referenciar a muchos donantes, beneficiarios y puntos de recolección.
- Un tipo clasifica muchos equipos; un donante puede ser la procedencia de varios equipos.
- Un usuario vendedor puede publicar equipos; los diagnósticos y reparaciones relacionan equipos con usuarios de cargo Técnico.
- Una entrega relaciona un equipo, un beneficiario y el usuario responsable de registrarla.
- Una conversación relaciona dos usuarios; puede tratar de un equipo publicado o del contacto con un operador. Sus mensajes identifican al emisor.
- Una sesión pertenece a una cuenta; desactivar la cuenta impide aceptar sus sesiones aunque el token no haya vencido aún.

### Inicialización y migraciones

Al inicializarse, `DatabaseFactory` conecta el pool Hikari y ejecuta `SchemaUtils.createMissingTablesAndColumns`, luego carga catálogos básicos si las tablas de ciudades o tipos están vacías. Este mecanismo es una sincronización automática limitada, **no sustituye una herramienta/versionado formal de migraciones**.

Es importante mantener alineados el modelo Kotlin y los scripts SQL del sitio web. Por ejemplo, el script web declara `Conversaciones.equipo_contexto` como columna generada e índices/constraints propios, mientras que el modelo Exposed actual declara `equipo_contexto` como una columna entera con valor predeterminado. La sincronización automática no garantiza recrear esa semántica generada ni todos los índices del script. Antes de aplicar cambios a una base compartida, comparar `DatabaseFactory.kt`, `ECOTECH_KTOR_SCHEMA.sql` y los scripts de migración del portal, realizar copia de seguridad y ejecutar la migración explícita correspondiente.

La longitud de `Auditoria.operacion` en el modelo es 50 caracteres para admitir los eventos existentes del esquema. Reducirla a 15 provoca errores al sincronizar si ya hay operaciones largas. El test `DatabaseConnectionTest` verifica conexión, inicialización y ejecución de `SELECT 1`; también puede realizar cambios de esquema y poblar datos semilla, por lo que requiere una base de pruebas o una base explícitamente autorizada.

## 5. Autenticación y sesiones

### Alta de cuenta

`POST /api/auth/register` recibe nombre, apellido, correo, teléfono, cargo y contraseña. El API valida campos requeridos, formato básico del correo y longitud mínima de contraseña de seis caracteres. El alta pública solo admite `Usuario` y `Vendedor`; los cargos internos deben crearse desde el flujo protegido de administración. Al completar el alta, se crea una sesión y se responde con el usuario y un token.

### Inicio de sesión

`POST /api/auth/login` localiza la cuenta por correo, comprueba la contraseña y rechaza cuentas inhabilitadas. Las credenciales nuevas se almacenan con BCrypt (factor configurado en 12). Para contraseñas heredadas el repositorio reconoce el hash SHA-256 legado y, si coincide, lo reemplaza por un hash BCrypt.

### Tipo de token y validación de sesión

- La API devuelve un token opaco aleatorio de 32 bytes codificado como Base64 URL-safe; **no es un JWT**.
- El token sin procesar se entrega al cliente una sola vez y no se persiste en claro. La tabla `AppSessions` contiene su huella SHA-256, el ID de cuenta y la expiración.
- La duración de sesión configurada es de 30 días.
- El cliente conserva el token en `AuthApi.currentToken` en memoria y lo envía mediante `Authorization: Bearer <token>` en llamadas a `PortalApi`.
- Antes de procesar una ruta privada, el servidor calcula la huella del token, busca una sesión no vencida y verifica que la cuenta siga activa.
- `POST /api/auth/logout` revoca la sesión eliminando la fila asociada. La interfaz del portal intenta cerrar la sesión remota antes de limpiar el estado local; si el API no responde, presenta una opción para cerrar solo localmente.

### Límites de seguridad que deben considerarse

1. El token del cliente se conserva en memoria, no hay persistencia de sesión cliente entre reinicios.
2. El servidor Ktor escucha en `0.0.0.0:8080`; el repositorio no configura TLS. En producción debe colocarse detrás de HTTPS/TLS y establecer CORS para orígenes concretos.
3. `Application.kt` permite CORS desde cualquier host. Es una configuración amplia para desarrollo y debe restringirse al desplegar.
4. `DatabaseFactory` todavía contiene valores alternativos para conexión. No deben utilizarse en despliegue: inyectar secretos por un mecanismo de entorno seguro, no imprimirlos y rotar cualquier credencial previamente expuesta.
5. La contraseña mínima es una política básica; no se observa en este flujo limitación de intentos, segundo factor ni mecanismo de recuperación integrado con el token de sesión.

## 6. Autorización por cargo

La autorización combina dos comprobaciones: primero, la cuenta debe tener una sesión válida y activa; después, el middleware revisa la ruta, el método HTTP y el cargo. Los cargos reconocidos son `Usuario`, `Vendedor`, `Operador`, `Tecnico`, `Auditor` y `Administrador`.

| Cargo | Funciones principales disponibles en el portal |
|---|---|
| Usuario | Explorar equipos publicados, consultar puntos activos y conversar con vendedores u operadores. |
| Vendedor | Funciones de Usuario y publicación de equipos propios. |
| Operador | Recibir y responder conversaciones de coordinación asignadas al operador. |
| Técnico | Consultar inventario, registrar diagnósticos/reparaciones y revisar su actividad. La API limita el listado al técnico autenticado y fuerza su ID en altas de registros. |
| Auditor | Consultar registros de auditoría, estadísticas e información de inspección permitida. |
| Administrador | Administrar usuarios, catálogos, donantes, beneficiarios, equipos, puntos y operaciones según las rutas habilitadas. |

El alta y la edición de usuarios internos están restringidas a Administración; un usuario no puede eliminar su propia cuenta. Las mutaciones de ciudades y tipos de equipo son administrativas. El API limita la visibilidad pública de inventario a equipos publicados. Las conversaciones y sus mensajes requieren que el usuario sea participante; para Operador, la conversación debe tener al operador como interlocutor y el contexto `operador`.

**Advertencia sobre el alcance actual:** las reglas del middleware son el control central de autorización y deben revisarse cuando se agreguen rutas o métodos. La autorización por ruta no sustituye una revisión de propiedad fila por fila para cualquier nuevo endpoint; algunas operaciones administrativas CRUD generales se rigen por rol, no por permisos finos por recurso.

## 7. Auditoría: qué registra y qué no

El registro de auditoría se implementa en `Audit.record` y se invoca desde repositorios en varias altas, modificaciones y eliminaciones de usuarios, ciudades, tipos, donantes, equipos, beneficiarios, diagnósticos, reparaciones y entregas. El endpoint `GET /api/auditoria` permite consultar eventos a Administración y Auditoría; el panel auditor permite buscar, filtrar por tabla/operación/fecha y paginar localmente los datos recibidos.

Cada evento registra tabla afectada, operación, ID de registro cuando existe, fecha, detalle y el identificador de actor fijo `ecotech-api`. Aunque el DTO y la tabla contienen `valores_anteriores` y `valores_nuevos`, `Audit.record` de la aplicación no los rellena actualmente: los snapshots aparecerán únicamente si otro mecanismo, como los triggers instalados por scripts SQL del portal, los genera.

**La auditoría del API es actualmente best-effort:** `Audit.record` atrapa excepciones sin propagarlas ni notificar al solicitante. Por tanto, una operación de negocio puede finalizar aunque no se haya guardado su evento de auditoría. También hay operaciones que no tienen llamada de auditoría en sus rutas/repositorios (por ejemplo, cambios de estado o acciones nuevas deben verificarse individualmente). No tratar el registro actual como una bitácora transaccional completa ni como evidencia inmutable.

Para trazabilidad de producción se recomienda: propagar/loggear explícitamente los errores de auditoría; registrar el ID autenticado real, no un actor fijo; persistir snapshots de manera consistente; confirmar cobertura de todas las mutaciones; y usar triggers/migraciones versionados si se requiere auditar escrituras realizadas fuera del API. Los scripts SQL del sitio y la auditoría del API son mecanismos separados.

## 8. Flujos principales

### 8.1 Registro e inicio de sesión

1. El cliente envía registro o login a la ruta pública de autenticación.
2. El API normaliza correo/cargo y ejecuta las validaciones de entrada.
3. En login, busca al usuario, verifica BCrypt o actualiza un hash SHA-256 legado y comprueba que la cuenta esté activa.
4. Crea una sesión con token aleatorio y guarda únicamente su hash y vencimiento.
5. Devuelve el perfil junto con el token. El cliente guarda el token en memoria y navega al portal del cargo.
6. Las rutas protegidas validan el token en cada solicitud; logout elimina la sesión remota.

### 8.2 Publicación, exploración y conversación

1. Administración mantiene los tipos de equipo; los usuarios autenticados pueden consultarlos.
2. Un Vendedor completa los datos del equipo en el portal. El servidor utiliza el ID del vendedor autenticado, marca la publicación como publicada y asigna el estado `Publicado`.
3. El catálogo comunitario solo ofrece equipos publicados que pertenecen a vendedores activos.
4. Un Usuario o Vendedor inicia conversación con el propietario del equipo o contacta a un Operador activo.
5. El servidor comprueba el cargo del contacto, que el propietario coincida con el vendedor y que el equipo continúe publicado. Evita abrir conversación consigo mismo y reutiliza una conversación del mismo contexto si ya existe.
6. Los participantes intercambian mensajes. Solo participantes autorizados pueden leer o enviar; al leer, se marcan mensajes entrantes como leídos.

### 8.3 Recepción, diagnóstico y reparación

1. El equipo se registra en inventario con tipo y estado.
2. Un Técnico consulta el inventario disponible y registra un diagnóstico. El backend ignora el ID técnico enviado por el cliente y usa la cuenta autenticada.
3. Según el diagnóstico, el estado del equipo cambia a `En Reparación` o `Listo para entrega`.
4. Si se requiere trabajo, el Técnico registra reparación, repuestos, costo y estado.
5. Al completar la reparación, el estado del equipo pasa a `Reacondicionado`; si continúa abierta, permanece `En Reparación`.
6. Administración u Operación registra una entrega a un beneficiario mediante el flujo de gestión.

### 8.4 Gestión administrativa y transparencia

Administración consulta métricas, administra usuarios/cargos, donantes, catálogos, puntos y datos operativos. Auditoría consulta métricas e historial disponible; puede filtrar texto, tabla, operación y fechas en el cliente. Los snapshots pueden estar vacíos en eventos creados por `Audit.record` del API, como se explica en la sección anterior.

## 9. API por grupos funcionales

Todas las rutas privadas requieren cabecera Bearer, excepto el documento raíz, salud, registro e inicio de sesión. El formato principal de intercambio es JSON.

| Grupo | Rutas de entrada | Uso |
|---|---|---|
| Estado | `GET /`, `GET /api/health` | Información del servicio y comprobación de conectividad a base de datos. |
| Autenticación | `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout` | Crear cuenta pública permitida, abrir sesión y revocarla. |
| Catálogos | `/api/ciudades`, `/api/tipos-equipo` | Consultar referencias; Administración mantiene datos. |
| Inventario/personas | `/api/equipos`, `/api/donantes`, `/api/beneficiarios` | Gestión administrativa y consultas de inventario, con visibilidad pública restringida. |
| Operación | `/api/diagnosticos`, `/api/reparaciones`, `/api/entregas` | Actividad técnica, reparación y entrega. |
| Administración | `/api/usuarios` | Consultar, crear, editar, activar/desactivar y eliminar cuentas bajo controles de rol. |
| Métricas/auditoría | `/api/stats/*`, `/api/auditoria` | Resumen operativo y eventos disponibles para cargos autorizados. |
| Portal comunitario | `/api/portal/initial`, `/api/portal/messages`, `/api/portal/conversations`, `/api/portal/equipment` | Carga inicial, chat y publicación de equipo por vendedor. |
| Puntos | `/api/puntos-recoleccion` | Consulta pública de activos; altas y bajas administrativas. |

El contrato de modelos JSON se define en `server/.../Models.kt` y `AuthModels.kt`; el cliente duplicado y serializable correspondiente está en `app/shared/.../PortalApi.kt`. Cambios en DTO deben mantenerse compatibles en ambos lados.

## 10. Datos de ejemplo y cálculos

Al arrancar contra una base vacía, el servidor inserta ciudades y tipos de equipo de muestra. Si no existe ningún usuario, el código actual también inserta una cuenta administrativa inicial con una contraseña ya hasheada. Esta semilla es un mecanismo de desarrollo/arranque, no una estrategia recomendada de provisión de identidad productiva; su presencia y credenciales asociadas deben sustituirse por un proceso seguro de bootstrap antes de publicar el servicio.

El resumen estadístico agrega conteos de usuarios, equipos, beneficiarios, donantes, reparaciones y entregas; cuenta entregas y recepciones del mes, agrupa inventario por estado y estima CO₂ como `totalEquipos × 51 kg`. Es una regla aproximada codificada, no un cálculo basado en mediciones por equipo.

## 11. Configuración y arranque

Variables reconocidas por `DatabaseFactory` (la variable de entorno prevalece sobre la propiedad JVM):

| Variable | Uso |
|---|---|
| `DB_HOST` | Host MySQL. |
| `DB_PORT` | Puerto MySQL; valor alternativo: `3306`. |
| `DB_NAME` | Base/catálogo; valor alternativo: `ecotech`. |
| `DB_USER` | Usuario MySQL. |
| `DB_PASSWORD` | Contraseña MySQL. Debe inyectarse desde un gestor de secretos o entorno protegido. |

Ejemplo local:

```bash
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=ecotech
export DB_USER=ecotech_app
export DB_PASSWORD='secreto-local'
./gradlew :server:run
```

La API escucha actualmente en el puerto `8080`. Desde navegador/cliente, configura `AuthApi.customBaseUrl` para señalar al backend. Si no se configura, el código usa `localhost` en JVM/Desktop/Web/iOS y `10.0.2.2` en Android (emulador).

La inicialización normal de la aplicación tolera fallos de conexión e informa estado degradado en `/api/health`; la prueba de conexión usa `throwOnError=true` y falla si la conexión o la sincronización falla.

## 12. Pruebas y validación

Comandos Gradle principales:

```bash
./gradlew :server:compileKotlin
./gradlew :server:test
./gradlew :server:test --tests com.example.ecotech.ApplicationTest
./gradlew :server:test --tests com.example.ecotech.DatabaseConnectionTest
./gradlew :app:shared:compileKotlinJvm
./gradlew :app:shared:jvmTest
```

`ApplicationTest` verifica la ruta raíz y el endpoint de salud con Ktor test host. `DatabaseConnectionTest` conecta a la base indicada en configuración, sincroniza el esquema, ejecuta `SELECT 1` y comprueba `DatabaseFactory.isConnected`. No es una prueba aislada: requiere red y una cuenta SQL capaz de ejecutar los cambios de esquema necesarios. Para CI es preferible usar MySQL efímero o una base dedicada y descartar sus datos al finalizar.

## 13. Consideraciones operativas y trabajo pendiente

- Crear un sistema de migraciones versionadas y probar cambios sobre una copia de la base real; no confiar en `createMissingTablesAndColumns` para representar migraciones complejas.
- Alinear la columna/contexto de conversación generada y los índices del script web con el modelo Exposed antes de aprovisionar una base limpia.
- Eliminar credenciales predeterminadas de la configuración, rotar credenciales expuestas, no registrar valores secretos y desplegar detrás de TLS.
- Restringir CORS a los orígenes del cliente desplegado.
- Convertir auditoría best-effort en persistencia observable y asociar eventos al ID autenticado real.
- Agregar pruebas de autorización por rol, ownership de equipos/conversaciones y aislamiento de datos de Técnico.
- Mantener `Models.kt` y `PortalApi.kt` sincronizados y distinguir pantallas con datos persistidos de pantallas de demostración.
- Asegurar que las acciones reportadas como realizadas desde la UI correspondan a una ruta real y a una operación persistida.
