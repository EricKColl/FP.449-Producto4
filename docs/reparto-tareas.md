# Reparto de tareas · Producto 4

Entrega: **martes 15 de diciembre de 2026, 23:59**. Fecha interna de entrega: **lunes 14 de diciembre** (un día de margen).

El reparto sigue la rúbrica (3 criterios, 25 puntos). La aplicación web del Producto 3 no cambia: el Producto 4 le añade
una **API REST** en `/api/*` que lee y escribe en la misma base de datos a través de los servicios que ya existen.
Cada persona es responsable de **un grupo de rutas de la API** completo: controlador, DTO, documentación de Swagger,
tests y sus peticiones en la colección de Postman.

**El código se hace por turnos**, uno detrás de otro, como en los productos 2 y 3: cada persona programa su parte cuando
le toca, partiendo del trabajo del anterior ya unido en `develop`. Así nadie tiene que resolver conflictos al juntar el código.

| Turno | Persona | Rama | Parte | Criterio de la rúbrica |
|---|---|---|---|---|
| **1** | Erick Coll | `feature/erick-base-api` | Base de la API: token JWT, cadena de seguridad de `/api/**`, errores en JSON, Swagger y Postman | Puntos securizados · Swagger |
| **2** | Jacobo Barrera | `feature/jacobo-api-catalogo` | Catálogo abierto: `/api/libros` y `/api/autores` | ApiRestFull |
| **3** | Carles Miguel | `feature/carles-api-admin` | Administración con token de ADMIN: `/api/admin/*` | Puntos securizados |
| **4** | Antonio Charneco | `feature/antonio-api-user` | Cuenta del cliente con token de USER: `/api/user/*` | Puntos securizados |

Erick revisa e integra cada pull request, monta el vídeo y la memoria y prepara la entrega.

## Decisiones de diseño

- **Misma base de datos, mismos servicios.** Los controladores de la API llaman a `LibroService`, `PrestamoService`,
  `ReservaService`… igual que los de la web, así que las reglas (límite de tres préstamos, cola de reservas, no borrar
  un libro con préstamos) son las mismas en las dos puertas.
- **Nunca se devuelven entidades.** Cada respuesta es un `record` de `api/dto` con los campos justos: así no salen
  contraseñas ni relaciones circulares, y el JSON no cambia aunque cambie una entidad.
- **Token JWT firmado (Spring Security OAuth2 Resource Server).** `POST /api/auth/token` con email y contraseña devuelve
  un token que caduca en 60 minutos. Se manda en la cabecera `Authorization: Bearer <token>`. La API no usa sesión ni
  cookies, así que no necesita CSRF.
- **Errores en JSON estándar** (*Problem Details*, RFC 9457): 400, 401, 403, 404 y 409 llevan siempre `status`,
  `title` y `detail`, y los de validación, la lista de campos que fallan.
- **Swagger UI** documenta todos los puntos de acceso, con ejemplos y el botón *Authorize* para probar los protegidos.

## Turno 1 · Base de la API (Erick)

Rama `feature/erick-base-api` · **Del viernes 13 al lunes 16 de noviembre** · Terminado el 13 de noviembre

- [x] Dependencias: springdoc-openapi 3.2 (Swagger UI) y Spring Security OAuth2 Resource Server (JWT).
- [x] Cadena de seguridad propia para `/api/**` (`api/ApiSecurityConfig`): sin sesión, sin CSRF, CORS abierto a terceros y token JWT.
- [x] `POST /api/auth/token`: email y contraseña → token firmado (HS256, 60 minutos), con el mismo bloqueo tras cinco fallos que el login web.
- [x] `GET /api/cuenta`: primer punto securizado, de ejemplo para los demás turnos.
- [x] Errores de la API en JSON (*Problem Details*) y 404 en JSON para cualquier ruta `/api/*` que no exista.
- [x] Swagger UI en `/swagger-ui.html` con la descripción de la API, el esquema de seguridad del token y el esquema `Error`.
- [x] Colección y entorno de Postman en `postman/` (7 peticiones, 28 comprobaciones), y job «API con Postman» en GitHub Actions con Newman.
- [x] Clase base `ApiTest` para los tests de la API, con `token(ADMIN)`, `token(LAURA)` y `token(JAVIER)`.
- [x] Tests del token, de la cadena de seguridad, de los errores y de Swagger: `./mvnw verify` da **199 tests**.
- [x] Docker: nombres de contenedores propios del Producto 4 y variable `BIBLIOTECA_API_CLAVE`.

## Turno 2 · Catálogo abierto (Jacobo)

Rama `feature/jacobo-api-catalogo` · **Del martes 17 al domingo 22 de noviembre** · Implementación y pruebas terminadas el 18 de noviembre

- [x] `GET /api/libros` con búsqueda (`?q=`) y filtro por género (`?genero=`), y `GET /api/libros/{id}`.
- [x] `GET /api/autores` y `GET /api/autores/{id}` con sus libros.
- [x] DTO `LibroResumen`, `LibroDetalle`, `AutorResumen` y `AutorDetalle`, con ejemplos para Swagger.
- [x] Abrir los `GET` del catálogo en la cadena de seguridad de la API: funcionan sin token.
- [x] Carpeta «Catálogo» en la colección de Postman, con siete peticiones sin token. Colección completa: 14 peticiones y 58 comprobaciones; dos ejecuciones consecutivas en verde contra MariaDB.
- [x] Tests: JSON correcto, búsqueda, filtros, 400 y 404 en JSON, privacidad y acceso solo a consultas sin token. `./mvnw verify` da **211 tests**, sin fallos; cobertura de líneas **94,94 %**.

## Turno 3 · Administración (Carles)

Rama `feature/carles-api-admin` · **Del lunes 23 al sábado 28 de noviembre**

- [x] `POST /api/admin/libros`, `PUT /api/admin/libros/{id}` y `DELETE /api/admin/libros/{id}`, con validación (400 con los campos que fallan) y 409 si el libro tiene préstamos.
- [x] `GET /api/admin/usuarios`, sin contraseñas.
- [x] `GET /api/admin/prestamos` con filtro por estado y `POST /api/admin/prestamos/{id}/devolucion`.
- [x] Solo con token de ADMIN: sin token, 401; con token de USER, 403.
- [x] Carpeta «Administración» en Postman, con un libro que se crea, se edita y se borra.
- [x] Tests de los tres perfiles: sin token, USER y ADMIN.

- [x] Seis operaciones documentadas en Swagger bajo «Administración», con DTO para el cuerpo de libros y las respuestas JSON.
- [x] Validación del turno: **237 tests**, **95,40 %** de cobertura de líneas y comprobaciones Postman **90/90** en la ejecución sin escrituras y **25/25** en el ciclo de libros contra MariaDB. Las devoluciones se verificaron en H2.

[Sección técnica para la memoria](memoria-administracion.md).

## Turno 4 · Cuenta del cliente (Antonio)

Rama `feature/antonio-api-user` · **Del domingo 29 de noviembre al viernes 4 de diciembre**

- [ ] `GET /api/user/prestamos` y `POST /api/user/prestamos/{id}/renovacion`.
- [ ] `GET`, `POST` y `DELETE` de `/api/user/reservas`, con el puesto en la cola.
- [ ] `GET`, `PUT` y `DELETE` de `/api/user/favoritos/{libroId}`.
- [ ] El usuario sale siempre del token, nunca de la URL ni del cuerpo: nadie ve ni toca lo de otro.
- [ ] Solo con token de USER: sin token, 401; con token de ADMIN, 403.
- [ ] Carpeta «Cliente» en Postman y tests de aislamiento entre usuarios.

## Calendario por turnos

| Fechas | Qué | Quién |
|---|---|---|
| 13 – 16 nov | Base de la API, token, Swagger y Postman | Erick |
| 17 – 22 nov | Catálogo abierto: `/api/libros` y `/api/autores` | Jacobo |
| 23 – 28 nov | Administración: `/api/admin/*` | Carles |
| 29 nov – 4 dic | Cuenta del cliente: `/api/user/*` | Antonio |
| 5 – 7 dic | Repaso final de Swagger y Postman | Erick |
| 8 dic | Congelación del código | Todos |
| 9 – 11 dic | Vídeo: cada uno graba su parte (ver «Vídeo») y la envía el viernes 11 | Todos |
| 12 dic | Erick une el vídeo y lo sube a YouTube | Erick |
| 12 – 13 dic | Memoria en PDF | Erick |
| 14 dic | Entrega interna: todo revisado | Todos |
| **15 dic** | **Entrega** | |

Cada turno empieza con `git switch` a su rama y `git merge develop`, para partir de lo último. Erick avisa en el grupo
cuando une una pull request: es la señal para que empiece el turno siguiente. Si alguien acaba antes, puede abrir su
pull request antes de su fecha.

## Memoria

Cada persona envía a Erick, al acabar su turno, qué ha hecho y qué dificultades ha tenido.

## Vídeo (unos 10 min): presentación y cuatro partes en el orden de los turnos

El vídeo enseña **toda la API funcionando** y cómo se comprueba: cada ruta en **Swagger UI** y en **Postman**, con el JSON
que devuelve y los errores que da cuando algo no se puede hacer. Sigue el orden de los turnos, de la base al cliente.
Cada persona explica **su parte** con su voz, empezando por lo que ha hecho. Erick abre el vídeo (2:30) y lo cierra (1:00),
y une todos los cortes.

| Orden | Quién | Duración | Pantallas | Qué enseña |
|---|---|---|---|---|
| 1 | Erick | 2:30 | Cartelas, IntelliJ, Docker, Swagger UI, Postman | Presentación del equipo y del producto: una API REST en `/api/*` sobre la misma base de datos y los mismos servicios que la web. Tecnologías nuevas: Spring Security OAuth2 Resource Server (token JWT firmado), springdoc-openapi (Swagger UI) y Postman con Newman. En IntelliJ IDEA: las dependencias del `pom.xml`, `ApiSecurityConfig` (cadena propia sin sesión ni CSRF y reglas por rol) y `TokenService` (qué lleva el token). `docker compose up -d --build` y `docker compose ps` con los contenedores `biblioteca-p4-*`. Swagger UI: las cuatro secciones y el esquema `Error`. `POST /api/auth/token` con `admin@example.com`, el token en **Authorize** y `GET /api/cuenta`; sin token, **401** en JSON. En Postman: importar la colección y el entorno y ejecutar la carpeta **Token**. |
| 2 | Jacobo | 2:00 | IntelliJ, Swagger UI, navegador, Postman | Por qué la API devuelve DTO (`record`) y no entidades: `LibroResumen` y `LibroDetalle`, y la regla que abre el catálogo en `ApiSecurityConfig`. En Swagger UI, sin token: `GET /api/libros` (14 libros), con `q` = `garcia` y con `genero` = `fantasia`; la ficha del libro **2** con sus autores y los ejemplares libres; el libro **999** da **404** en JSON. `GET /api/autores/2` con sus libros. `http://localhost:8080/api/libros/2` abierto en el navegador. En Postman: la carpeta **Catálogo** ejecutada, con sus comprobaciones en verde. |
| 3 | Carles | 2:30 | IntelliJ, Swagger UI, Postman | `LibroEntrada` con las reglas de validación y `LibroAdminApiController`. En Swagger UI con el token de `admin@example.com`: alta de *Eva Luna* (ISBN `978-84-9759-243-7`, autor 3) → **201** con la cabecera `Location`; modificarla (**200**) y borrarla (**204**). *Don Quijote de la Mancha* (libro 1) no se borra: **409**. Un alta con el título y los autores vacíos: **400** con `errores`. `GET /api/admin/usuarios` sin contraseñas y `GET /api/admin/prestamos` con `estado` = `VENCIDO`; registrar la devolución del préstamo **5** (no el 6: su libro lo reserva Antonio en su parte). Con el token de `laura.martinez@example.com`: **403**; sin token: **401**. En Postman: la carpeta **Administración** ejecutada. |
| 4 | Antonio | 2:30 | IntelliJ, Swagger UI, Postman | Por qué el usuario sale siempre del token (`TokenService.usuarioId`) y nunca de la URL. En Swagger UI con el token de `laura.martinez@example.com`: «Mis préstamos» y renovar el préstamo **7** (**200**, y la segunda vez **409**); reservar *El señor de los anillos* (libro **10**) → **201** con su puesto en la cola, intentar reservar *1984* (**409**) y cancelar la reserva (**204**); marcar y quitar *La sombra del viento* (libro **4**) de favoritos. Con el token de `javier.lopez@example.com`, renovar el préstamo 7 de Laura da **404**; con el de `admin@example.com`, `/api/user/prestamos` da **403**. En Postman: la carpeta **Cliente** ejecutada. |
| 5 | Erick | 1:00 | Postman, terminal, GitHub | Cierre: **Run collection** con la colección entera (33 peticiones en verde), `./mvnw verify` con los 242 tests y la cobertura, y en GitHub Actions los cuatro controles en verde, con el resumen de **API con Postman**. La especificación OpenAPI en `/v3/api-docs`. |

Cada parte empieza en la pantalla donde acaba la anterior, así los cortes se unen sin saltos. Si alguien se pasa de
su tiempo en más de 15 segundos, que lo regrabe: el total no debe superar los 11 minutos.

**Cómo grabar tu parte**

1. Después de la congelación (martes 8), ponte al día: `git switch develop` y `git pull`.
2. Si tienes arrancado el Producto 3, páralo (`docker compose stop` en su carpeta), y para la aplicación de IntelliJ (■).
3. Arranca todo como en la entrega, con los datos de prueba limpios: `docker compose down -v` y `docker compose up -d --build`.
   Espera a que `docker compose ps` diga *healthy* en `biblioteca-p4-app`. Repítelo antes de cada toma si has cambiado datos.
4. En Postman, importa la colección y el entorno de `postman/` (elige **Replace**) y el entorno **Biblioteca · local**.
5. Prepara Chrome: ventana maximizada, zoom al 100 % (`Ctrl`+`0`), barra de marcadores oculta (`Ctrl`+`Mayús`+`B`) y
   notificaciones del sistema desactivadas («No molestar» o «Concentración»). Que no se vea nada personal: ni tu cuenta de
   Postman (cierra el panel de la derecha) ni otras pestañas.
6. Graba la **pantalla completa con micrófono** en formato mp4, a 1920×1080. Recomendado: **OBS Studio** (Windows y Mac).
7. Habla como si se lo explicaras al profesor: qué haces y por qué responde así. Mueve el ratón despacio y ensáyalo una vez antes de grabar.
8. Llama al fichero `video-<orden>-<nombre>.mp4` (por ejemplo `video-3-carles.mp4`) y envíaselo a Erick
   (Google Drive o WeTransfer) **el viernes 11 de diciembre**.

**Detalles de la API que conviene saber antes de grabar**

- Cuentas: `admin@example.com` / `Admin1234`, `laura.martinez@example.com` y `javier.lopez@example.com` / `Usuario1234`.
- El token caduca a los 60 minutos y **deja de valer si se reinicia la aplicación**: si todo da 401, pide uno nuevo.
- En Swagger UI el token se pega en **Authorize** sin comillas y sin la palabra «Bearer». Para cambiar de cuenta: **Logout** y otro token.
- Cinco contraseñas mal seguidas con el mismo email lo bloquean cinco minutos (**429**): si pasa, espera o usa otra cuenta.
- La renovación del préstamo 7 y la devolución del préstamo 5 cambian los datos: para repetir la toma, `docker compose down -v` y `docker compose up -d --build`.
- Si Docker Desktop no arranca en Windows con un error de `engine.sock` o `dockerInference`, **no** pulses
  «Reset to factory defaults»: avisa a Erick.

**Montaje (Erick)**: une los cortes en orden con la presentación y el cierre, sube el vídeo a YouTube como **Oculto** y
pasa el enlace al grupo el **sábado 12**: va en la memoria.

## Definición de hecho

- `./mvnw verify` en verde (tests y cobertura mínima del 90 %) y `docker compose up -d --build` funcionando.
- Cada punto de acceso nuevo aparece en Swagger UI con su descripción, sus respuestas y un ejemplo.
- Cada punto de acceso nuevo tiene al menos una petición en la colección de Postman con sus comprobaciones, y
  el job «API con Postman» de GitHub Actions está en verde.
- Ninguna respuesta de la API devuelve una entidad JPA ni una contraseña.
