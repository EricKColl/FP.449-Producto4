# Producto 4 · Administración de la biblioteca

**Aportación de Carles Miguel Millán · FP065**

## Objetivo y alcance

La aportación incorpora seis operaciones REST para administrar el catálogo de libros, consultar usuarios y gestionar
devoluciones. La API usa la misma base de datos y los mismos servicios que la aplicación web, de modo que ambos
canales aplican las mismas reglas de negocio.

## Arquitectura y decisiones

Se añadieron los controladores `LibroAdminApiController`, `UsuarioAdminApiController` y
`PrestamoAdminApiController`. Cada controlador adapta los parámetros y el cuerpo HTTP a las operaciones de los
servicios existentes. `LibroService` y `AutorService` gestionan los libros y sus autores,
`UsuarioService.listarConPrestamos()` aporta los recuentos por usuario y `PrestamoService` ordena y filtra los
préstamos y registra las devoluciones.

El DTO `LibroEntrada` fija el contrato del alta y de la edición. Bean Validation comprueba los campos obligatorios,
las longitudes, el número de ejemplares, el año de publicación cuando se envía y la presencia de autores. La capa
REST comprueba las referencias a autores y el ISBN. `UsuarioResumen` limita el listado a los campos administrativos
y excluye contraseñas y hashes. `PrestamoDto` incluye las fechas, el estado y el indicador de renovación, y
representa el libro y el usuario mediante sus identificadores y nombres. Para la respuesta de libros se reutiliza
`LibroDetalle`, procedente del catálogo de Jacobo.

La configuración JWT de Erick ya protege `/api/admin/**` con el rol `ADMIN`. La aplicación devuelve `401` ante
un token ausente o inválido y `403` cuando el token pertenece a otro rol. Se documentaron las seis operaciones con
las anotaciones de OpenAPI de la infraestructura existente. Los errores se representan como Problem Details.

## Operaciones y validaciones

- **Libros.** El alta devuelve `201` con la ficha y una cabecera `Location` que apunta al recurso público.
  La edición reemplaza los datos y devuelve `200`. El borrado devuelve `204` sin cuerpo. Un libro con historial
  de préstamos y un ISBN duplicado producen `409`. Si el libro de la URL no existe, la edición y el borrado
  devuelven `404`. Un cuerpo inválido o un autor inexistente produce `400`.
- **Usuarios.** El listado devuelve `200` con los usuarios ordenados por apellidos y nombre. El recuento de
  préstamos en curso incluye todos los que siguen abiertos. El de vencidos corresponde al subconjunto fuera de plazo.
- **Préstamos.** El listado devuelve `200` y admite el filtro opcional `estado` con `ACTIVO`, `VENCIDO` o
  `DEVUELTO`. Un valor desconocido produce `400`. El servicio reutiliza las fechas del préstamo para determinar
  el estado. La devolución devuelve `200` con el estado actualizado a `DEVUELTO`, `404` si el préstamo no existe
  y `409` si ya está devuelto. La operación conserva el mecanismo de avisos del servicio web.

## Verificación y resultados

Se añadieron 26 casos con MockMvc y H2 en `LibroAdminApiTest`, `UsuarioAdminApiTest` y
`PrestamoAdminApiTest`. Se comprobaron los roles, los contratos JSON, las validaciones, los conflictos,
los tres estados de préstamos y la especificación OpenAPI. `./mvnw -B verify` terminó con `BUILD SUCCESS`,
237 tests y cero fallos, errores u omitidos. JaCoCo midió un 95,40 % de cobertura de líneas.

En Postman se superaron 90 de 90 comprobaciones de la ejecución sin escrituras y 25 de 25 en el ciclo de
escritura contra MariaDB. El ciclo confirmó alta `201`, ISBN duplicado `409`, edición `200`, borrado `204`
y consulta posterior `404`.

## Dificultades y soluciones

El ciclo de Postman necesitaba identificar el libro de cada ejecución y poder eliminarlo después. Se generó un
ISBN de prueba por ejecución y se reutilizó el identificador devuelto por el alta para editar y eliminar el registro.
La consulta final confirmó que la ficha había desaparecido. Una devolución cambia el estado de un préstamo y haría
que una segunda ejecución obtuviera otro resultado. Por ello, las dos peticiones de devolución se dejaron
deshabilitadas en la colección y se verificaron el éxito y la repetición en H2, con rollback.

## Mejoras futuras

Sería útil probar solicitudes simultáneas con el mismo ISBN y devoluciones concurrentes para evaluar si los conflictos
mantienen el contrato `409` en esos casos. También se podría ampliar la cobertura de límites de validación y verificar
la publicación de avisos después de confirmar la transacción.
