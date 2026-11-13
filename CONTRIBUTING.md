# Guía de contribución · Biblioteca (FP065 Producto 4)

Proyecto en equipo de la asignatura FP065 (UOC): aplicación web de gestión de una biblioteca con Spring Boot,
Thymeleaf, Spring Data JPA y MariaDB, ejecutable con Docker. Aquí están las normas comunes del equipo:
léelas antes de empezar tu parte para que todas las pantallas se vean y funcionen igual.

- Enunciado completo y rúbrica: `docs/enunciado.md` · Entrega: **15 dic 2026**
- Reparto de tareas, calendario y "definición de hecho": `docs/reparto-tareas.md`
- Modelo de datos (E/R, relacional, reglas): `docs/modelo-datos.md`
- Base de partida: Producto 3 → https://github.com/EricKColl/FP.449-Producto3
- Equipo: Erick Coll Rodríguez (coordinador), Carles Miguel Millán, Jacobo Barrera Toba, Antonio Charneco Alcalá

## Stack

Java 21 · Spring Boot 4.1.2 (`spring-boot-starter-webmvc`, `-thymeleaf`, `-data-jpa`, `-validation`, `-actuator`,
`-security-oauth2-resource-server`) · springdoc-openapi 3.2
· MariaDB 11.8 · H2 (tests y perfil `h2`) · Bootstrap 5.3, Tabler Icons y las fuentes Inter y Playfair Display como WebJars · Maven Wrapper.

Spring Boot 4 tiene paquetes de test distintos a los de la versión 3:
`org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`,
`org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`,
`org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase`,
`org.springframework.test.context.bean.override.mockito.MockitoBean` (en lugar de `@MockBean`).

## Seguridad

- **Reglas de acceso:** están todas en `security/SecurityConfig.java`. Lo que no se abra ahí expresamente queda reservado a ADMIN: una ruta pública nueva, como `/registro`, hay que añadirla a las reglas.
- **Cuenta de la sesión:** en las rutas `/user/*` se usa `@AuthenticationPrincipal CuentaUsuario cuenta` y `cuenta.getId()`. El usuario nunca se recibe por parámetro ni por formulario, para que nadie actúe en nombre de otro.
- **Formularios:** todo POST necesita `th:action`, que añade el token CSRF. Con un `action` normal, Spring Security responde 403.
- **Contraseñas:** las cifra `UsuarioService`. Nunca se guardan en claro ni se muestran. Los campos que puede rellenar un formulario se limitan con `@InitBinder`.
- **Vistas según el rol:** `sec:authorize="hasRole('ADMIN')"` o `sec:authorize="isAuthenticated()"`, con `xmlns:sec="http://www.thymeleaf.org/extras/spring-security"` en la etiqueta `html`.
- **Tests:** `@WithMockUser(roles = "ADMIN")` para simular el rol y `.with(csrf())` en cada `post(...)`. Hay ejemplos en `SeguridadTest`.
- **JavaScript:** solo en ficheros de `static/js` (`app.js` para todas las páginas). La política de contenidos (CSP) de `SecurityConfig` bloquea los scripts escritos dentro del HTML y los atributos como `onsubmit`. Para pedir confirmación antes de borrar: `th:data-confirmar="|¿Eliminar «${libro.titulo}»?|"` en el `<form>`.
- **Librerías nuevas:** como WebJar en el `pom.xml`, nunca desde un CDN: la aplicación tiene que funcionar sin conexión y la CSP no deja cargar scripts de otros dominios.

## API REST (Producto 4)

- **Dónde va:** todo en `src/main/java/edu/uoc/biblioteca/api/`. Controladores con `@RestController` y rutas que empiezan por `/api/`; las respuestas, en `api/dto/`.
- **Nunca devolver entidades:** cada respuesta es un `record` de `api/dto` con un método estático `de(...)` que lo construye desde la entidad. Así no salen contraseñas ni relaciones circulares.
- **La lógica, en los servicios:** el controlador llama a `LibroService`, `PrestamoService`… y convierte el resultado a DTO. Si falta algo, se añade un método al servicio, no una consulta en el controlador.
- **Seguridad:** las reglas de `/api/**` están en `api/ApiSecurityConfig.java`, aparte de las de la web. `/api/admin/**` es solo ADMIN, `/api/user/**` solo USER y lo demás pide un token de cualquier rol: una ruta abierta nueva hay que añadirla a las reglas.
- **Usuario del token:** `@AuthenticationPrincipal Jwt token` y `TokenService.usuarioId(token)`. Nunca un id de usuario en la URL o en el cuerpo de `/api/user/*`.
- **Errores:** se lanzan las mismas excepciones que en la web. `ApiErrores` las convierte en JSON: `ResponseStatusException` con su código, `IllegalStateException` → 409, `IllegalArgumentException` → 400 y `@Valid` → 400 con `errores`.
- **Códigos:** `GET` → 200; crear → 201 con la cabecera `Location`; borrar → 204 sin cuerpo.
- **Swagger:** en cada controlador `@Tag(name = "Catálogo" | "Administración" | "Cliente")` y, si es protegido, `@SecurityRequirement(name = OpenApiConfig.TOKEN)`. En cada método `@Operation(summary, description)` y un `@ApiResponse` por código. En los DTO, `@Schema(example = ...)`. Los 401 y 403 de las rutas protegidas los añade `OpenApiConfig` solo.
- **Tests:** las clases de test extienden `ApiTest` (en `src/test/java/edu/uoc/biblioteca/api/`), que da `mvc` y `token(ADMIN)`, `token(LAURA)` o `token(JAVIER)`: `mvc.perform(get("/api/cuenta").with(token(LAURA)))`. El JSON se comprueba con `jsonPath("$.campo")`.
- **Postman:** cada punto de acceso nuevo, con al menos una petición en su carpeta de `postman/biblioteca-api.postman_collection.json` y sus `pm.test`. Se exporta desde Postman en formato *Collection v2.1* encima del fichero.


## Comandos

```bash
./mvnw verify                                        # compila y pasa los tests (H2, sin Docker)
docker compose up -d db adminer                      # solo la BD → ejecutar la app desde el IDE
./mvnw spring-boot:run                               # app contra el MariaDB de Docker (localhost:3307)
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2 # app sin Docker (BD en memoria)
docker compose up -d --build                         # todo en Docker → http://localhost:8080
docker compose down -v                               # borra la BD; se recrea desde los scripts
```

Windows: `.\mvnw.cmd` en lugar de `./mvnw`. Adminer: http://localhost:8081 (servidor `db`, usuario/contraseña `biblioteca`).

## Estructura

```
src/main/java/edu/uoc/biblioteca/
  model/        entidades JPA: Autor, Libro, Usuario, Prestamo, EstadoPrestamo
  repository/   interfaces Spring Data (JpaRepository)
  service/      lógica de negocio (@Service, @Transactional)
  controller/   controladores MVC (@Controller)
  api/          API REST: ApiSecurityConfig, TokenService, ApiErrores, OpenApiConfig y los @RestController
  api/dto/      respuestas y cuerpos de la API (records)
src/main/resources/
  db/01-esquema.sql, db/02-datos.sql   esquema y datos (los usan MariaDB en Docker y el perfil h2)
  templates/fragments/layout.html      head, navbar, alertas y footer comunes
  templates/<entidad>/*.html           vistas de cada entidad
  static/css/estilos.css               estilos propios sobre Bootstrap
```

## Convenciones (obligatorias)

**Idioma**: todo en español (clases, métodos, variables, rutas, vistas, mensajes, commits), como en el Producto 1.

**Capas**: `Controller → Service → Repository`. Los controladores no usan repositorios directamente.
Inyección por constructor (sin `@Autowired` en campos). Servicios con `@Transactional(readOnly = true)` a nivel
de clase y `@Transactional` en los métodos que escriben. No se usa Lombok.

**Rutas** (los formularios HTML solo envían GET y POST):

| | |
|---|---|
| `GET /libros` | listado → `libros/lista` |
| `GET /libros/{id}` | detalle → `libros/detalle` |
| `GET /admin/libros/nuevo` · `GET /admin/libros/{id}/editar` | formulario → `libros/formulario` |
| `POST /admin/libros` · `POST /admin/libros/{id}` | crear · actualizar → redirige al detalle |
| `POST /admin/libros/{id}/eliminar` | eliminar → redirige al listado |

Id inexistente → `throw new ResponseStatusException(HttpStatus.NOT_FOUND, "...")` (se muestra `error.html`).

**Escritura separada**: las rutas que crean, modifican o borran están en su propio controlador
(`LibroAdminController`, `AutorAdminController`, `UsuarioAdminController`), bajo `/admin/...`, y las
consultas de libros y autores en el de siempre. Todas las rutas de usuarios, incluidas las consultas,
están bajo `/admin/usuarios`, porque contienen datos personales.

Las altas, ediciones y bajas de autores usan `/admin/autores`; tras guardar se vuelve al catálogo público
`/autores` o `/autores/{id}`. Las consultas públicas de libros siguen en `/libros` y `/libros/{id}`.
Los botones y la columna de acciones de los catálogos solo se renderizan para ADMIN; el acceso directo
se protege en `SecurityConfig` mediante `/admin/**`.

**Vistas**: cada página usa los fragmentos de `fragments/layout.html` (hay una plantilla mínima en su comentario):
`head('Título')`, `navbar('libros'|'autores'|'usuarios'|'prestamos'|'inicio')`, `alertas` y `footer`.
Componentes de Bootstrap 5 (tablas `table table-hover`, `card`, `btn btn-primary`, `badge`, `form-control`,
`is-invalid` + `invalid-feedback`) e iconos `ti ti-*` (Tabler). Los colores del tema están en `css/estilos.css`. Nada de CSS ni JS por CDN: todo sale de los WebJars.
No se añade CSS propio en cada vista: solo clases de Bootstrap. Si hace falta un estilo nuevo, se pide a Erick.

- Estado de un préstamo con su color: `<span th:replace="~{fragments/layout :: estadoPrestamo(${prestamo})}"></span>`.
- Fechas siempre así: `${#temporals.format(fecha, 'dd/MM/yyyy')}` (si puede ser `null`, comprobarlo antes).
- Valor vacío: `th:text="${libro.editorial} ?: '—'"`.
- Selección de entidades en un formulario (p. ej. los autores de un libro): `<select th:field="*{autores}" multiple>`
  con `th:value="${autor.id}"`. Spring Data convierte solo los ids en entidades y marca las opciones ya elegidas.
  La lista de opciones va en el modelo con otro nombre (`todosLosAutores`) para no confundirla con el campo.

**Mensajes**: tras crear, editar o eliminar, redirigir (patrón PRG) con
`redirectAttributes.addFlashAttribute("mensajeExito" | "mensajeError", "...")`; el fragmento `alertas` los muestra.

**Validación**: las restricciones ya están en las entidades (`@NotBlank`, `@Size`, `@Email`...) con mensajes en español.
En el controlador: `@Valid @ModelAttribute("libro") Libro libro, BindingResult resultado`; si hay errores, volver al formulario.
Las reglas de negocio (ISBN repetido, borrar con préstamos, ejemplares disponibles) se comprueban en el servicio.

**Relaciones LAZY**: `spring.jpa.open-in-view=true`, así que las vistas pueden recorrer `libro.autores`,
`prestamo.usuario`, etc. En listados, usar los métodos con `@EntityGraph` del repositorio para evitar N+1 consultas.

**Modelo de datos compartido**: Hibernate está en `ddl-auto=validate` y **no crea tablas**. Cualquier cambio en una
entidad exige cambiar también `db/01-esquema.sql` (y `02-datos.sql`) y `docs/modelo-datos.md`, y avisar al equipo.
`./mvnw verify` detecta si no coinciden. No cambiar a `ddl-auto=update`.

**Tests**: cada controlador nuevo, con su clase de test en `src/test/java/edu/uoc/biblioteca/controller/`.
Patrón común: `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("h2")` + `@Transactional`
(aplicación completa sobre H2 con los datos de `02-datos.sql`; cada test deshace sus cambios al terminar).
Datos de prueba: 14 libros, 11 autores, 6 usuarios, 11 préstamos (7 sin devolver, 2 de ellos vencidos).

**Ficheros compartidos** (los cambia solo Erick, o se le avisa antes): `pom.xml`, `application*.properties`,
`db/*.sql`, `model/*`, `fragments/layout.html`, `estilos.css`, `Dockerfile`, `docker-compose.yml`.
Añadir un método nuevo a un repositorio sí se puede (no cambia las tablas).

**Git**: `main` ← `develop` ← `feature/<tarea>`. Pull request a `develop` con revisión. `./mvnw verify` en verde
antes de abrir la PR (el CI de GitHub Actions lo comprueba y también construye la imagen Docker).
