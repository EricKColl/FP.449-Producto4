package edu.uoc.biblioteca.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Documentación OpenAPI y Swagger UI: abiertas a cualquiera y con todas las rutas de la API. */
class SwaggerTest extends ApiTest {

    @Test
    void laEspecificacionDescribeLaApi() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi", containsString("3.")))
                .andExpect(jsonPath("$.info.title").value("API de la Biblioteca BackLord"))
                .andExpect(jsonPath("$.paths['/api/auth/token'].post").exists())
                .andExpect(jsonPath("$.paths['/api/cuenta'].get").exists());
    }

    @Test
    void lasRutasProtegidasPidenElTokenEnLaDocumentacion() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.components.securitySchemes.token.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.token.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.paths['/api/cuenta'].get.security[0].token").exists())
                .andExpect(jsonPath("$.paths['/api/cuenta'].get.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/token'].post.security").doesNotExist());
    }

    @Test
    void losErroresTienenSuEsquema() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.components.schemas.Error.properties.detail").exists())
                .andExpect(jsonPath("$.paths['/api/auth/token'].post.responses['401'].content['application/problem+json']")
                        .exists());
    }

    @Test
    void salenLasSeccionesConRutas() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.tags[*].name", hasItem("Token")));
    }

    @Test
    void lasPaginasDeLaWebNoSeDocumentan() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/libros']").doesNotExist());
    }

    @Test
    void swaggerUiSeAbreSinCuenta() throws Exception {
        mvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("swagger-ui")));
    }
}
