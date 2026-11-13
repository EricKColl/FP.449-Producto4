# Biblioteca · Producto 4

[![CI](https://github.com/EricKColl/FP.449-Producto4/actions/workflows/ci.yml/badge.svg)](https://github.com/EricKColl/FP.449-Producto4/actions/workflows/ci.yml)
[![CodeQL](https://github.com/EricKColl/FP.449-Producto4/actions/workflows/codeql.yml/badge.svg)](https://github.com/EricKColl/FP.449-Producto4/actions/workflows/codeql.yml)
![Cobertura mínima](https://img.shields.io/badge/cobertura%20m%C3%ADnima-90%25-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F)

Aplicación web de gestión de una biblioteca: catálogo de libros y autores, usuarios y préstamos.
Proyecto en equipo de la asignatura FP065 *Aplicación backend con tecnología Java en servidores de aplicaciones* (UOC).

Este producto parte del [Producto 3](https://github.com/EricKColl/FP.449-Producto3), la aplicación web completa con usuarios,
Spring Security y roles, y añade una **API REST** en `/api/*` sobre la misma base de datos: un catálogo abierto, puntos de
acceso protegidos con token JWT, documentación con Swagger UI y una colección de Postman para comprobarla.

- 🔌 **API REST**: Swagger UI en http://localhost:8080/swagger-ui.html y colección de Postman en [`postman/`](postman)
- 📄 [Enunciado y rúbrica](docs/enunciado.md)
- 👥 [Reparto de tareas y calendario](docs/reparto-tareas.md)
- 🗂️ [Modelo de datos (E/R y relacional)](docs/modelo-datos.md)
- 🎨 [Mockup en Figma (wireframe, pantallas y guía de estilo)](https://www.figma.com/design/iGemzWYavhu4GhgD3aQQ0q)
- 📐 [Guía de contribución y convenciones](CONTRIBUTING.md)

## Tecnologías

| Componente | Versión |
|---|---|
| Java (Eclipse Temurin) | 21 LTS |
| Spring Boot | 4.1.2 (Web MVC, Security, OAuth2 Resource Server, Thymeleaf, Data JPA, Validation, Actuator) |
| API REST | Token JWT firmado (HS256, Nimbus JOSE) · springdoc-openapi 3.2 (OpenAPI 3.1 y Swagger UI) · Postman y Newman |
| Base de datos | MariaDB 11.8 (H2 en memoria para los tests) |
| Interfaz | Thymeleaf + Bootstrap 5.3 + Tabler Icons + fuentes Inter y Playfair Display (WebJars) |
| Gestión del proyecto | Maven (wrapper incluido) |
| Contenedores | Docker y Docker Compose |

## Requisitos

- **JDK 21** (Eclipse Temurin), con `JAVA_HOME` apuntando a él.
  - Windows: `winget install --id EclipseAdoptium.Temurin.21.JDK -e`
  - macOS: `brew install --cask temurin@21`
- **Git**.
- **Docker Desktop** en marcha.

No hace falta instalar Maven ni MariaDB: el proyecto incluye el wrapper de Maven y la base de datos corre en Docker.

## Puesta en marcha

### Opción A · Todo en Docker (como en la entrega)

```bash
docker compose up -d --build
```

| Servicio | URL | |
|---|---|---|
| Aplicación | http://localhost:8080 | |
| Adminer (gestor de la BD) | http://localhost:8081 | Servidor `db` · usuario `biblioteca` · contraseña `biblioteca` · BD `biblioteca` |
| MariaDB | `localhost:3307` | Para MySQL Workbench, HeidiSQL, DBeaver o IntelliJ |

```bash
docker compose ps          # biblioteca-p4-app y biblioteca-p4-db deben aparecer como (healthy)
docker compose logs -f app # logs de la aplicación
docker compose down        # parar (los datos se conservan)
docker compose down -v     # parar y borrar los datos (se recrean desde los scripts al arrancar)
```

### Opción B · Desarrollo: la BD en Docker y la aplicación en el IDE

```bash
docker compose up -d db adminer
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run
```

O ejecutar `BibliotecaApplication` desde IntelliJ. Con DevTools, los cambios en las plantillas se ven al recargar la página.

### Opción C · Sin Docker (base de datos en memoria)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

Útil para trabajar en las vistas sin Docker. Usa los mismos scripts SQL; los cambios se pierden al parar.

### Cuentas de prueba

Las crea `02-datos.sql` y solo sirven para desarrollar y hacer la demostración.

| Rol | Email | Contraseña |
|---|---|---|
| ADMIN | `admin@example.com` | `Admin1234` |
| USER | `laura.martinez@example.com` (y los demás socios de los datos de prueba) | `Usuario1234` |

### Control de acceso

| Rutas | Quién entra |
|---|---|
| `/` (portada), `/login`, `/registro`, consulta de `/libros` y `/autores` | Cualquiera, sin cuenta |
| `/inicio` y `/user/cuenta` | Cualquier cuenta: `/inicio` lleva al administrador a `/admin/panel` y al cliente a `/user/inicio` |
| `/user/*`: inicio, mis préstamos, mis reservas y mis favoritos | Solo el rol USER (el administrador no pide préstamos ni reserva) |
| `/admin/*`: alta, edición y borrado de libros, autores y usuarios, y gestión de préstamos; y todo lo demás | Solo el rol ADMIN |

Una ruta que no aparezca en `SecurityConfig` queda reservada a ADMIN. El menú y las vistas cambian según el rol.
Además, la aplicación envía una política de seguridad de contenidos (CSP) que solo ejecuta los scripts servidos por ella misma.

## API REST

La API sirve los datos de la biblioteca en JSON a otras aplicaciones. Usa la misma base de datos y los mismos servicios
que la web, así que las reglas son las mismas: límite de tres préstamos, cola de reservas, no borrar un libro con préstamos…

| Qué | Dónde |
|---|---|
| **Swagger UI** (documentación y pruebas) | http://localhost:8080/swagger-ui.html |
| Especificación OpenAPI en JSON | http://localhost:8080/v3/api-docs |
| Colección y entorno de Postman | [`postman/`](postman) |

### Pedir un token

Las rutas protegidas necesitan un token JWT. Se pide con el email y la contraseña de una cuenta y caduca a los 60 minutos:

```bash
curl -X POST http://localhost:8080/api/auth/token \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@example.com","password":"Admin1234"}'
# {"token":"eyJhbGciOiJIUzI1NiJ9...","tipo":"Bearer","caducaEn":3600,"rol":"ADMIN"}

curl http://localhost:8080/api/cuenta -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

En Windows (PowerShell):

```powershell
$t = (Invoke-RestMethod -Method Post http://localhost:8080/api/auth/token -ContentType 'application/json' `
      -Body '{"email":"admin@example.com","password":"Admin1234"}').token
Invoke-RestMethod http://localhost:8080/api/cuenta -Headers @{ Authorization = "Bearer $t" }
```

En Swagger UI: ejecuta `POST /api/auth/token`, copia el token, pulsa **Authorize** y pégalo.

### Puntos de acceso

| Método y ruta | Token | Qué hace | Turno |
|---|---|---|---|
| `POST /api/auth/token` | No | Devuelve un token con email y contraseña | Erick ✅ |
| `GET /api/cuenta` | Cualquiera | Datos de la cuenta del token | Erick ✅ |
| `GET /api/libros` · `GET /api/libros/{id}` | No | Catálogo con búsqueda (`?q=`) y filtro (`?genero=`), y ficha de un libro | Jacobo |
| `GET /api/autores` · `GET /api/autores/{id}` | No | Autores y sus libros | Jacobo |
| `POST` · `PUT` · `DELETE /api/admin/libros` | ADMIN | Alta, edición y baja de libros | Carles |
| `GET /api/admin/usuarios` | ADMIN | Usuarios, sin contraseñas | Carles |
| `GET /api/admin/prestamos` · `POST /api/admin/prestamos/{id}/devolucion` | ADMIN | Préstamos por estado y devoluciones | Carles |
| `GET /api/user/prestamos` · `POST /api/user/prestamos/{id}/renovacion` | USER | Mis préstamos y renovarlos | Antonio |
| `GET` · `POST` · `DELETE /api/user/reservas` | USER | Mis reservas y su puesto en la cola | Antonio |
| `GET` · `PUT` · `DELETE /api/user/favoritos` | USER | Mis favoritos | Antonio |

Sin token, las rutas protegidas responden **401**; con un token de otro rol, **403**. El usuario de `/api/user/*` sale
siempre del token, nunca de la URL ni del cuerpo.

### Errores

Todos los errores son JSON con el formato estándar *Problem Details* (RFC 9457):

```json
{"detail":"No existe el libro 99","instance":"/api/libros/99","status":404,"title":"Not Found"}
```

| Código | Cuándo |
|---|---|
| 400 | Datos que no son válidos (con `errores`: un mensaje por campo) o JSON mal escrito |
| 401 | Falta el token, no es válido o ha caducado; o email y contraseña incorrectos |
| 403 | El token es de una cuenta de otro rol |
| 404 | No existe lo que se pide, o la ruta |
| 409 | Una regla lo impide: el libro tiene préstamos, ya está reservado… |
| 429 | Cinco fallos seguidos al pedir el token: ese email queda bloqueado 5 minutos |

### Postman

1. *Import* → `postman/biblioteca-api.postman_collection.json` y `postman/local.postman_environment.json`.
2. Arriba a la derecha, elegir el entorno **Biblioteca · local**.
3. *Run collection*: la carpeta *Token* guarda los tokens y las demás los usan. Cada petición comprueba su código de
   respuesta y que devuelve JSON.

La misma colección se ejecuta con Newman en GitHub Actions en cada pull request (job *API con Postman*). En local:

```bash
npx newman run postman/biblioteca-api.postman_collection.json -e postman/local.postman_environment.json
```

La clave que firma los tokens sale de la variable `BIBLIOTECA_API_CLAVE` (al menos 32 caracteres). Si no se da, se
genera una al arrancar y los tokens dejan de valer al reiniciar la aplicación.

### Tests

```bash
./mvnw verify               # Windows: .\mvnw.cmd verify
```

No necesitan Docker: se ejecutan sobre H2 con los scripts de MariaDB, y fallan si alguna entidad no coincide con las tablas.
GitHub Actions los pasa en cada pull request y además levanta el `docker compose` completo.

### Calidad del código

Cada subida a GitHub pasa tres controles automáticos, y sus resultados se ven en el propio repositorio:

| Control | Qué comprueba | Dónde se ve |
|---|---|---|
| **Tests y cobertura** (JaCoCo) | Todos los tests y que cubran al menos el **90 % de las líneas**: si baja, la compilación falla | Insignia *CI*, resumen de cada ejecución y el informe descargable `informe-cobertura` |
| **API con Postman** (Newman) | La colección de `postman/` contra la aplicación arrancada: códigos, JSON y seguridad de cada punto de acceso | Resumen de cada ejecución del CI |
| **CodeQL** | Análisis de seguridad del código Java y JavaScript: inyecciones, XSS, redirecciones abiertas… | Insignia *CodeQL* y pestaña *Security* |
| **Dependabot** | Avisos de seguridad si alguna dependencia tiene una vulnerabilidad conocida (las versiones nuevas las aplica el equipo a mano, probándolas antes) | Pestaña *Security* |

En local, el informe de cobertura queda en `target/site/jacoco/index.html` después de `./mvnw verify`.

## Qué hay en Docker

`docker-compose.yml` levanta tres contenedores:

- **db** · MariaDB 11.8 con `utf8mb4`. La primera vez ejecuta `src/main/resources/db/01-esquema.sql` y
  `02-datos.sql` (carpeta montada en `/docker-entrypoint-initdb.d`). Los datos persisten en el volumen `datos-mariadb`.
- **app** · la aplicación, compilada con el `Dockerfile` multi-etapa (Maven + JDK 21 → JRE 21 alpine, usuario sin
  privilegios). Espera a que la BD esté *healthy* y recibe la conexión por variables de entorno
  `SPRING_DATASOURCE_*`. Su healthcheck usa `/actuator/health`, que también comprueba la conexión con la BD.
- **adminer** · gestor web de la base de datos.

Los valores por defecto (usuarios, contraseñas y puertos) se pueden cambiar copiando `.env.example` a `.env`.
La BD se publica en el **3307** del equipo para no chocar con un MySQL o XAMPP local en el 3306.

## Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| `Port 8080 was already in use` al arrancar desde el IDE, o en http://localhost:8080 sale la aplicación de otro producto | Sigue en marcha el contenedor de un producto anterior, que se arranca solo con Docker Desktop. Pararlo desde la carpeta de ese producto con `docker compose down`, o con `docker rm -f biblioteca` en el caso del Producto 1 |
| `port is already allocated` en el 8080, el 8081 o el 3307 | Siguen en marcha los contenedores de un producto anterior. En su carpeta: `docker compose stop` |
| `Port 8080 was already in use` y el contenedor `biblioteca-p4-app` está en marcha | La app ya corre en Docker. Usar una de las dos formas: o Docker (`docker compose up -d`), o el IDE (`docker compose stop app` y ▶ en IntelliJ) |
| `port is already allocated` en el 3307 | Otro MariaDB/MySQL usa ese puerto. Cambiar `DB_PORT` en un fichero `.env` (ver `.env.example`) |
| `Schema-validation: missing column` o `missing table` | La BD de Docker se creó con un esquema anterior. Recrearla: `docker compose down -v && docker compose up -d db adminer` |
| IntelliJ marca errores en todo el código | *File → Project Structure → SDK*: `temurin-21` y recargar Maven (pestaña Maven → flechas en círculo) |

## Estructura

```
src/main/java/edu/uoc/biblioteca/
├── BibliotecaApplication.java
├── model/          # entidades JPA: Autor, Libro, Usuario, Prestamo, Reserva
├── repository/     # repositorios Spring Data
├── service/        # lógica de negocio
├── security/       # SecurityConfig, cuentas y login
├── controller/     # controladores MVC (la web)
└── api/            # API REST: seguridad con token, controladores @RestController y dto/
src/main/resources/
├── db/             # 01-esquema.sql y 02-datos.sql
├── templates/      # vistas Thymeleaf (fragments/layout.html = cabecera, menú y pie comunes)
├── static/css/     # estilos propios
└── application.properties, application-h2.properties
docs/               # enunciado, reparto, modelo de datos y diagramas
postman/            # colección y entorno de Postman
```

## Abrir el proyecto en IntelliJ IDEA

1. *File → Open* y seleccionar el `pom.xml` → *Open as Project*.
2. *File → Project Structure → Project → SDK*: JDK 21 (Temurin).
3. *Settings → Editor → File Encodings*: UTF-8.
4. `docker compose up -d db adminer` y ejecutar `BibliotecaApplication`.

## Flujo de trabajo con Git

- `main`: versión estable. Se iguala con `develop` tras cada integración.
- `develop`: rama de integración. Recibe los cambios mediante pull request revisada.
- `feature/<tarea>`: una rama por tarea, creada desde `develop`.

```bash
git switch develop
git pull
git switch -c feature/crud-libros
# ... cambios ...
./mvnw verify
git add .
git commit -m "Añade el listado de libros"
git push -u origin feature/crud-libros
```

Después se abre una pull request hacia `develop` y otro miembro del equipo la revisa antes de fusionarla.

Los fines de línea se normalizan con `.gitattributes`. En Windows se recomienda `git config --global core.autocrlf true`
y en macOS `git config --global core.autocrlf input`.

## Equipo

- Erick Coll Rodríguez
- Carles Miguel Millán
- Jacobo Barrera Toba
- Antonio Charneco Alcalá
