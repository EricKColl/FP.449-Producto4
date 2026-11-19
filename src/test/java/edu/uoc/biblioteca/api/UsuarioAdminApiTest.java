package edu.uoc.biblioteca.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UsuarioAdminApiTest extends ApiTest {

    private static final String USUARIOS = "/api/admin/usuarios";

    @Test
    void sinTokenDevuelve401() throws Exception {
        mvc.perform(get(USUARIOS))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void conTokenDeUsuarioDevuelve403() throws Exception {
        mvc.perform(get(USUARIOS).with(token(LAURA)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void listaLosUsuariosOrdenadosConSusRecuentosYLosCamposDelContrato() throws Exception {
        String json = mvc.perform(get(USUARIOS).with(token(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[*].id", contains(7, 5, 4, 2, 1, 6, 3)))
                .andExpect(jsonPath("$[0].rol").value("ADMIN"))
                .andExpect(jsonPath("$[0].prestamosEnCurso").value(0))
                .andExpect(jsonPath("$[0].prestamosVencidos").value(0))
                .andExpect(jsonPath("$[3].email").value(JAVIER))
                .andExpect(jsonPath("$[3].nombre").value("Javier"))
                .andExpect(jsonPath("$[3].apellidos").value("López Ferrer"))
                .andExpect(jsonPath("$[3].dni").value("23456781R"))
                .andExpect(jsonPath("$[3].telefono").value("623456789"))
                .andExpect(jsonPath("$[3].rol").value("USER"))
                .andExpect(jsonPath("$[3].fechaAlta").isString())
                .andExpect(jsonPath("$[3].prestamosEnCurso").value(2))
                .andExpect(jsonPath("$[3].prestamosVencidos").value(1))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

        Set<String> campos = Set.of("id", "dni", "nombre", "apellidos", "email", "telefono",
                "rol", "fechaAlta", "prestamosEnCurso", "prestamosVencidos");
        List<Map<String, Object>> usuarios = JsonPath.read(json, "$");
        assertThat(usuarios).allSatisfy(usuario -> assertThat(usuario.keySet()).isEqualTo(campos));
    }

    @Test
    void documentaElListadoComoOperacionProtegidaEnOpenApi() throws Exception {
        String operacion = "$.paths['" + USUARIOS + "'].get";
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(operacion + ".tags[0]").value("Administración"))
                .andExpect(jsonPath(operacion + ".security[0].token").isArray())
                .andExpect(jsonPath(operacion + ".responses['200'].content['application/json'].schema.items['$ref']")
                        .value("#/components/schemas/UsuarioResumen"))
                .andExpect(jsonPath(operacion + ".responses['401']").exists())
                .andExpect(jsonPath(operacion + ".responses['403']").exists());
    }
}
