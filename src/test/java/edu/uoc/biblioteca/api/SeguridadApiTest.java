package edu.uoc.biblioteca.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** La cadena de seguridad de /api/**: roles, rutas que no existen, CORS y que la web sigue igual. */
class SeguridadApiTest extends ApiTest {

    @Test
    void unTokenDeUsuarioNoEntraEnAdministracion() throws Exception {
        mvc.perform(get("/api/admin/usuarios").with(token(LAURA)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void unTokenDeAdministracionNoEntraEnLasRutasDelCliente() throws Exception {
        mvc.perform(get("/api/user/prestamos").with(token(ADMIN)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unaRutaQueNoExisteDevuelve404EnJson() throws Exception {
        mvc.perform(get("/api/no-existe").with(token(ADMIN)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("No existe la ruta GET /api/no-existe"));
    }

    @Test
    void laApiNoPideTokenCsrfNiCreaSesion() throws Exception {
        mvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@example.com\",\"password\":\"Admin1234\"}"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    void otraWebPuedeLlamarALaApiDesdeElNavegador() throws Exception {
        mvc.perform(options("/api/cuenta")
                        .header("Origin", "https://otra-web.example")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("GET")));
    }

    @Test
    void laWebNoSeAbreAOtrosDominios() throws Exception {
        mvc.perform(get("/").header("Origin", "https://otra-web.example"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void laWebSigueUsandoElLoginYNoElToken() throws Exception {
        mvc.perform(get("/admin/usuarios").with(token(ADMIN)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
