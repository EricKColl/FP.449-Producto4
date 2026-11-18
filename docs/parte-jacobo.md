# Parte de Jacobo · Catálogo público de la API REST

**Autor:** Jacobo Barrera Toba · **Producto:** FP449, Producto 4 · **Equipo:** BackLord  
**Rama:** `feature/jacobo-api-catalogo` · **Trabajo y validación local:** 18 de noviembre de 2026

Este documento reúne la aportación de Jacobo para que Erick pueda integrarla en la memoria conjunta. Los resultados corresponden a esta rama al terminar el catálogo; las cifras globales cambiarán cuando se incorporen los siguientes turnos. La revisión e integración en `develop` y el vídeo se gestionan aparte.

## Texto para integrar en la memoria

### Trabajo realizado y decisiones de diseño

He implementado el catálogo público de la biblioteca mediante cuatro puntos de acceso REST que devuelven JSON:

| Petición | Función |
|---|---|
| `GET /api/libros` | Lista los libros por título. Permite buscar con `q` y filtrar por `genero`, también combinando ambos parámetros. |
| `GET /api/libros/{id}` | Devuelve la ficha de un libro, sus autores y los ejemplares disponibles. |
| `GET /api/autores` | Lista los autores por apellidos e indica cuántos libros tiene cada uno. |
| `GET /api/autores/{id}` | Devuelve la ficha del autor y sus libros ordenados por título. |

Estas consultas son abiertas porque muestran la información pública del catálogo, igual que la web. En `ApiSecurityConfig` he permitido expresamente las peticiones `GET` a estos listados y fichas. Esta apertura no habilita operaciones de escritura ni otras rutas protegidas de la API.

He creado `LibroApiController` y `AutorApiController`, reutilizando `LibroService` y `AutorService`. La búsqueda aprovecha la lógica existente para ignorar mayúsculas y tildes; el filtro de género utiliza la misma normalización. La disponibilidad se obtiene del servicio de libros. Se mantienen el modelo, los servicios, el esquema SQL y los controladores de la web existentes.

Las respuestas utilizan cuatro DTO definidos como `record`: `LibroResumen`, `LibroDetalle`, `AutorResumen` y `AutorDetalle`. Estos objetos seleccionan los campos públicos que necesita cada respuesta, evitando serializar directamente las entidades JPA, sus relaciones circulares o los préstamos y datos de sus lectores. Los controladores y DTO incluyen las anotaciones de OpenAPI para mostrar operaciones, parámetros, respuestas y ejemplos en la sección «Catálogo» de Swagger UI.

### Pruebas y resultados

He añadido doce tests de integración en `CatalogoApiTest`, apoyados en la clase común `ApiTest` y en la base H2. Comprueban listados, ordenación, búsquedas sin tildes, filtros, búsquedas vacías, disponibilidad, privacidad, documentación OpenAPI y errores `400` y `404` en JSON. También verifican que la apertura pública se limita a consultas y no alcanza métodos de escritura ni rutas más profundas.

La ejecución de `./mvnw verify` terminó correctamente: **211 tests en total, sin fallos, errores ni pruebas omitidas**, frente a los 199 de la base inicial. La cobertura global de líneas medida con JaCoCo fue del **94,94 %**.

En Swagger UI probé las consultas sin token: los 14 libros del catálogo, `q=garcia`, `genero=fantasia`, la ficha del libro 2, el error `404` del libro 999 y los listados y fichas de autores. La ficha del libro 2 muestra dos ejemplares totales y uno disponible; la del autor 2 incluye sus dos libros ordenados por título.

En Postman añadí la carpeta «Catálogo» con siete peticiones y sus comprobaciones. Junto con las peticiones de la base de la API, la colección contiene **14 peticiones y 58 comprobaciones por ejecución**. Se ejecutó dos veces consecutivas contra MariaDB con la aplicación de IntelliJ y se repitió la validación con la aplicación completa en Docker: **116 comprobaciones en cada tanda de dos iteraciones, sin fallos ni errores**.

Para la prueba de entrega se compiló y arrancó la aplicación con Docker Compose y Java 21, conservando el volumen de MariaDB. La aplicación y la base de datos quedaron saludables, `/actuator/health` respondió `UP` y tanto la web como Swagger UI respondieron con HTTP `200`.

### Dificultades y resolución

Los puntos que requirieron más cuidado fueron delimitar el acceso anónimo a los cuatro recursos de consulta, construir respuestas sin relaciones privadas y mantener la búsqueda coherente con la web. Se resolvieron con reglas limitadas al método `GET`, DTO específicos y reutilización de los servicios y de la normalización existente. Los tests cubren estas decisiones.

En la preparación del entorno se ajustaron el SDK y Maven de IntelliJ a Java 21. Para alternar entre la ejecución del IDE y Docker se paró primero la aplicación que ocupaba el puerto 8080. Se conservaron los datos locales durante la validación.

### Alcance, mejoras y valoración

La implementación local cubre los cuatro puntos de acceso asignados y sus pruebas. Como mejora futura se podría incorporar paginación para catálogos grandes y una consulta de géneros, propuestas opcionales de la guía que quedan fuera de esta entrega del catálogo. La revisión del equipo y las validaciones de la PR completarán la comprobación antes de integrarlo en `develop`. El vídeo se preparará en la fase prevista por el equipo.

## Referencias y evidencias para Erick

- Guía de Erick: apartado 10, especialmente 10.10 («Tu apartado de la memoria»), y apartado 14.1 (validación con Docker).
- [Enunciado del repositorio](enunciado.md), [normas de contribución](../CONTRIBUTING.md) y [reparto de tareas](reparto-tareas.md).
- Código: `src/main/java/edu/uoc/biblioteca/api/LibroApiController.java`, `AutorApiController.java`, `ApiSecurityConfig.java` y los cuatro DTO de `api/dto/`.
- Tests: `src/test/java/edu/uoc/biblioteca/api/CatalogoApiTest.java`. Informe local generado por Maven: `target/site/jacoco/index.html`.
- Colección compartida: `postman/biblioteca-api.postman_collection.json`; entorno: `postman/local.postman_environment.json`.
- Commits de implementación y pruebas: `76a704d` y `fe1ab33`.
- Este documento recoge resultados locales comprobados; el estado de CI y las revisiones se consultan en la PR de esta rama.
