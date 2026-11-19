package edu.uoc.biblioteca.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PrestamoAdminApiTest extends ApiTest {

    private static final String PRESTAMOS = "/api/admin/prestamos";
    private static final String DEVOLUCION = PRESTAMOS + "/{id}/devolucion";

    @Test
    void sinTokenDevuelve401EnLasDosOperaciones() throws Exception {
        mvc.perform(get(PRESTAMOS)).andExpect(status().isUnauthorized());
        mvc.perform(post(DEVOLUCION, 6)).andExpect(status().isUnauthorized());
    }

    @Test
    void conTokenDeUsuarioDevuelve403EnLasDosOperaciones() throws Exception {
        mvc.perform(get(PRESTAMOS).with(token(LAURA))).andExpect(status().isForbidden());
        mvc.perform(post(DEVOLUCION, 6).with(token(LAURA))).andExpect(status().isForbidden());
    }

    @Test
    void listaLosPrestamosDelMasRecienteAlMasAntiguo() throws Exception {
        mvc.perform(get(PRESTAMOS).with(token(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[*].id", contains(11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1)))
                .andExpect(jsonPath("$[0].libroId").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Don Quijote de la Mancha"))
                .andExpect(jsonPath("$[0].usuarioId").value(2))
                .andExpect(jsonPath("$[0].usuario").value("Javier López Ferrer"))
                .andExpect(jsonPath("$[0].fechaPrestamo").isString())
                .andExpect(jsonPath("$[0].fechaDevolucionPrevista").isString())
                .andExpect(jsonPath("$[0].fechaDevolucion").value(nullValue()))
                .andExpect(jsonPath("$[0].estado").value("ACTIVO"))
                .andExpect(jsonPath("$[0].renovado").value(false))
                .andExpect(jsonPath("$[*].password").doesNotExist());
    }

    @ParameterizedTest
    @CsvSource({"ACTIVO, 5", "VENCIDO, 2", "DEVUELTO, 4"})
    void filtraPorCadaEstadoValido(String estado, int cantidad) throws Exception {
        mvc.perform(get(PRESTAMOS).param("estado", estado).with(token(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(cantidad)))
                .andExpect(jsonPath("$[*].estado", everyItem(is(estado))));
    }

    @Test
    void unEstadoDesconocidoDevuelve400() throws Exception {
        mvc.perform(get(PRESTAMOS).param("estado", "PERDIDO").with(token(ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail", endsWith("parámetro estado")));
    }

    @Test
    void registraLaDevolucionYActualizarElEstadoImpideRepetirla() throws Exception {
        mvc.perform(post(DEVOLUCION, 6).with(token(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.estado").value("DEVUELTO"))
                .andExpect(jsonPath("$.titulo").value("El señor de los anillos"))
                .andExpect(jsonPath("$.fechaDevolucion").value(LocalDate.now().toString()));

        mvc.perform(get(PRESTAMOS).param("estado", "VENCIDO").with(token(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", contains(5)));

        mvc.perform(post(DEVOLUCION, 6).with(token(ADMIN)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Ese préstamo ya estaba devuelto"));
    }

    @Test
    void devolverUnPrestamoQueNoExisteDevuelve404() throws Exception {
        mvc.perform(post(DEVOLUCION, 999).with(token(ADMIN)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe el préstamo 999"));
    }

    @Test
    void unIdNoNumericoDevuelve400() throws Exception {
        mvc.perform(post(DEVOLUCION, "abc").with(token(ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", endsWith("parámetro id")));
    }

    @Test
    void openApiDocumentaLasDosOperacionesProtegidas() throws Exception {
        String listado = "$.paths['" + PRESTAMOS + "'].get";
        String devolucion = "$.paths['" + DEVOLUCION + "'].post";
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(listado + ".tags[0]").value("Administración"))
                .andExpect(jsonPath(listado + ".parameters[0].name").value("estado"))
                .andExpect(jsonPath(listado + ".parameters[0].required").value(false))
                .andExpect(jsonPath(listado + ".parameters[0].schema.enum", contains("ACTIVO", "VENCIDO", "DEVUELTO")))
                .andExpect(jsonPath(listado + ".security[0].token").isArray())
                .andExpect(jsonPath(listado + ".responses['401']").exists())
                .andExpect(jsonPath(devolucion + ".security[0].token").isArray())
                .andExpect(jsonPath(devolucion + ".responses['403']").exists())
                .andExpect(jsonPath(devolucion + ".responses['409']").exists())
                .andExpect(jsonPath(devolucion + ".responses['200'].content['application/json'].schema['$ref']")
                        .value("#/components/schemas/PrestamoDto"));
    }
}
