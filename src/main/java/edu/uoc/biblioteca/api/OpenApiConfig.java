package edu.uoc.biblioteca.api;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Documentación de la API con OpenAPI 3 (springdoc). La especificación en JSON está en /v3/api-docs y
 * Swagger UI la presenta en /swagger-ui.html, donde cada ruta se puede probar con el botón «Try it out».
 */
@Configuration
@SecurityScheme(name = OpenApiConfig.TOKEN, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
        description = "Pide el token en POST /api/auth/token y pégalo aquí, sin la palabra Bearer.")
public class OpenApiConfig {

    /** Nombre del esquema de seguridad: @SecurityRequirement(name = OpenApiConfig.TOKEN) en cada ruta protegida. */
    public static final String TOKEN = "token";
    private static final String ERROR = "Error";

    @Bean
    OpenAPI documentacionDeLaApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de la Biblioteca BackLord")
                        .version("1.0")
                        .description("""
                                API REST de la biblioteca: lee y escribe en la misma base de datos que la aplicación web.

                                - **Catálogo**: libros y autores, abierto a cualquiera, sin token.
                                - **Token**: pide un token con tu email y contraseña y úsalo en las rutas protegidas.
                                - **Administración** (`/api/admin`): solo con un token de una cuenta ADMIN.
                                - **Cliente** (`/api/user`): préstamos, reservas y favoritos de la cuenta del token (rol USER).

                                Todas las respuestas son JSON. Los errores siguen el formato *Problem Details* (RFC 9457).""")
                        .contact(new Contact().name("Grupo BackLord · FP065 UOC")
                                .url("https://github.com/EricKColl/FP.449-Producto4")));
    }

    /** Secciones de Swagger UI, en este orden. Cada controlador elige la suya con @Tag(name = "..."). */
    private static final List<Tag> SECCIONES = List.of(
            new Tag().name("Catálogo").description("Libros y autores, sin token"),
            new Tag().name("Token").description("Pedir el token que abre las rutas protegidas"),
            new Tag().name("Administración").description("Gestión de la biblioteca con un token de ADMIN"),
            new Tag().name("Cliente").description("La cuenta del token: préstamos, reservas y favoritos"));

    /**
     * Completa la documentación de todas las rutas: los errores (4xx) se describen siempre con el esquema «Error»,
     * y cada ruta protegida avisa de que responde 401 sin token y 403 con un token de otro rol.
     * Las secciones salen en el orden de SECCIONES y solo las que ya tienen alguna ruta.
     */
    @Bean
    OpenApiCustomizer completarDocumentacion() {
        return api -> {
            // springdoc rehace los componentes con los DTO: el esquema de los errores se añade al final
            if (api.getComponents() == null) {
                api.setComponents(new Components());
            }
            api.getComponents().addSchemas(ERROR, esquemaDeError());
            Set<String> usadas = new HashSet<>();
            api.getPaths().values().forEach(metodos -> metodos.readOperations().forEach(operacion -> {
                if (operacion.getTags() != null) {
                    usadas.addAll(operacion.getTags());
                }
            }));
            api.setTags(SECCIONES.stream().filter(seccion -> usadas.contains(seccion.getName())).toList());
            api.getPaths().forEach((ruta, metodos) ->
                    metodos.readOperations().forEach(operacion -> documentarErrores(ruta, operacion)));
        };
    }

    private static void documentarErrores(String ruta, Operation operacion) {
        ApiResponses respuestas = operacion.getResponses();
        if (protegida(operacion)) {
            respuestas.putIfAbsent("401", new ApiResponse().description("Falta el token, no es válido o ha caducado"));
            if (ruta.startsWith("/api/admin") || ruta.startsWith("/api/user")) {
                respuestas.putIfAbsent("403", new ApiResponse().description("El token es de una cuenta de otro rol"));
            }
        }
        respuestas.forEach((codigo, respuesta) -> {
            if (codigo.startsWith("4")) {
                respuesta.setContent(new Content().addMediaType("application/problem+json",
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + ERROR))));
            }
        });
    }

    private static boolean protegida(Operation operacion) {
        return operacion.getSecurity() != null && !operacion.getSecurity().isEmpty();
    }

    @SuppressWarnings("rawtypes")
    private static Schema esquemaDeError() {
        Map<String, Schema> campos = new LinkedHashMap<>();
        campos.put("detail", new StringSchema().example("No existe el libro 99"));
        campos.put("instance", new StringSchema().example("/api/libros/99"));
        campos.put("status", new IntegerSchema().example(404));
        campos.put("title", new StringSchema().example("Not Found"));
        campos.put("errores", new ObjectSchema()
                .description("Solo en los 400 de validación: un mensaje por cada campo que falla")
                .additionalProperties(new StringSchema())
                .example(Map.of("titulo", "El título es obligatorio")));
        return new ObjectSchema().description("Error en formato Problem Details (RFC 9457)").properties(campos);
    }
}
