# Producto 4 · Servicio APIRestFul (FP065)

> Copia del enunciado y de la rúbrica del aula virtual, para tenerlos siempre junto al código.
> Fecha de entrega: **martes 15 de diciembre de 2026, 23:59** (se permite 1 intento).

## Descripción

Una vez finalizada la aplicación para el uso a través de la web, queremos poder servir datos a través de un nuevo servicio APIRestFul atacando a la base de datos que ya disponemos y ofreciendo un nuevo punto de acceso con datos que puedan ser consumidas por terceros.

## Objetivos

- Crear servicio APIRestFull con Spring.io
- Usar postman u otra aplicación para la comprobación del buen funcionamiento

## Pasos a seguir

1. Leer detenidamente estas instrucciones e identificar los requerimientos de la actividad.
2. Revisar detenidamente la rúbrica de evaluación.
3. Consultar los recursos necesarios facilitados en el aula.
4. Se creará una nueva ruta del tipo `/api/` donde se presentará al menos 2 endpoints a partir de las entidades que ya existen en la base de datos. En el caso de la aplicación de Alquiler de vehiculos podrían ser:
   - `GET /api/alquilerVehiculos`
   - `GET /api/clientes`
5. Se creará 3 puntos de acceso securizado con algún mecanismo de seguridad dentro de la comunicación APIRestFull, tipo tokens o bien OAuth2.
6. Tener presente el uso de Swagger y SwaggerUI tanto para la estandarización y presentación de la documentación del API.

## Requisitos

- Buscar información en Internet y los recursos facilitados.

## Entrega

- Por el canal indicado en el aula virtual.
- Formato: PDF.
- Nombre del archivo: `producto4(FP065)_Apellido1Apellido2_Nombre`.
- Extensión recomendada: 3-5 páginas.
- Memoria del trabajo: trabajo realizado, dificultades, puntos no realizados, propuestas de mejora, autoevaluación, webgrafía.
- Vídeo o enlace de YouTube mostrando el funcionamiento de la aplicación.

## Rúbrica (máximo 25 puntos, aprobado con 12,5)

### ApiRestFull (8 puntos)

| Puntos | Nivel |
|---|---|
| **8** | Se ha generado una nueva ruta tipo `/api/` en la que existen al menos 2 endpoints no securizados a partir de la información y modelos almacenados en la base de datos. La información devuelta será JSON y se comprobará el correcto funcionamiento con herramientas tipo POSTMAN. |
| 6 | Al menos 2 endpoints no securizados a partir de la información y modelos almacenados en la base de datos. La información devuelta será JSON. |
| 4 | Al menos 1 endpoint no securizado a partir de la información y modelos almacenados en la base de datos. La información devuelta será JSON. |
| 2 | Al menos 1 endpoint no securizado a partir de la información y modelos almacenados en la base de datos. |

### Punto de acceso securizado (9 puntos)

| Puntos | Nivel |
|---|---|
| **9** | Se crean al menos 3 puntos de acceso securizados implementando seguridad mediante tokens o bien oAuth2. La información devuelta será JSON y se comprobará el correcto funcionamiento con herramientas tipo POSTMAN. |
| 6,75 | Al menos 3 puntos de acceso securizados con tokens u oAuth2. La información devuelta será JSON. |
| 4,5 | Al menos 2 puntos de acceso securizados con tokens u oAuth2. La información devuelta será JSON. |
| 2,25 | Al menos 2 puntos de acceso securizados con tokens u oAuth2. |

### Uso de Swagger y Swagger UI (8 puntos)

| Puntos | Nivel |
|---|---|
| **8** | Se incorpora Swagger y Swagger UI para estandarizar y presentar al menos 5 puntos de acceso creados. |
| 6 | Swagger y Swagger UI para al menos 3 puntos de acceso creados. |
| 4 | Swagger y Swagger UI para al menos 2 puntos de acceso creados. |
| 2 | Swagger para al menos 2 puntos de acceso creados. |

## Cómo cubrimos la nota máxima

| Criterio | Lo que pide el 100 % | Cómo lo cubrimos |
|---|---|---|
| ApiRestFull (8) | 2 endpoints sin seguridad, JSON y probados con Postman | Catálogo abierto: `GET /api/libros`, `/api/libros/{id}`, `/api/autores` y `/api/autores/{id}` (turno de Jacobo) |
| Puntos securizados (9) | 3 puntos con tokens u OAuth2, JSON y probados con Postman | Token JWT firmado que da `POST /api/auth/token` (Erick); `/api/admin/*` solo con token de ADMIN (Carles) y `/api/user/*` con token de USER (Antonio) |
| Swagger (8) | Swagger y Swagger UI con 5 puntos de acceso | springdoc-openapi: especificación OpenAPI en `/v3/api-docs` y Swagger UI en `/swagger-ui.html`, con todos los puntos documentados y el botón *Authorize* para el token |

La colección de Postman está en `postman/` y GitHub Actions la ejecuta con Newman en cada pull request.
