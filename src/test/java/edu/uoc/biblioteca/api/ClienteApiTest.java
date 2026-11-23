package edu.uoc.biblioteca.api;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.time.LocalDate;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/** /api/user/*: préstamos, reservas y favoritos de la cuenta del token. Nadie ve ni toca lo de otro. */
class ClienteApiTest extends ApiTest {
// --- Quién puede entrar -------------------------------------------------------------------
@Test
void sinTokenNoSeEntra() throws Exception {
    mvc.perform(get("/api/user/prestamos")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/user/reservas")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/user/favoritos")).andExpect(status().isUnauthorized());
}
    @Test
    void conTokenDeAdministracionNoSeEntra() throws Exception {
        mvc.perform(get("/api/user/prestamos").with(token(ADMIN))).andExpect(status().isForbidden());
        mvc.perform(put("/api/user/favoritos/4").with(token(ADMIN))).andExpect(status().isForbidden());
    }
    // --- Préstamos ----------------------------------------------------------------------------
    @Test
    void cadaUnoVeSoloSusPrestamos() throws Exception {
        mvc.perform(get("/api/user/prestamos").with(token(LAURA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", contains(7, 4, 1)))
                .andExpect(jsonPath("$[0].titulo").value("Cien años de soledad"))
                .andExpect(jsonPath("$[0].estado").value("ACTIVO"));
        mvc.perform(get("/api/user/prestamos").with(token(JAVIER)))
                .andExpect(jsonPath("$[*].id", contains(11, 6, 2)));
    }
    @Test
    void renuevaUnPrestamoUnaSolaVez() throws Exception {
        String nuevaFecha = LocalDate.now().plusDays(25).toString();
        mvc.perform(post("/api/user/prestamos/7/renovacion").with(token(LAURA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaDevolucionPrevista").value(nuevaFecha))
                .andExpect(jsonPath("$.renovado").value(true));
        mvc.perform(post("/api/user/prestamos/7/renovacion").with(token(LAURA)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("No se puede renovar «Cien años de soledad»: ya se renovó una vez"));
    }
    @Test
    void elPrestamoDeOtroNoExisteParaMi() throws Exception {
        mvc.perform(post("/api/user/prestamos/7/renovacion").with(token(JAVIER)))
                .andExpect(status().isNotFound());
    }
    @Test
    void unPrestamoVencidoNoSeRenueva() throws Exception {
        mvc.perform(post("/api/user/prestamos/6/renovacion").with(token(JAVIER)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail",
                        startsWith("No se puede renovar «El señor de los anillos»")));
    }
    // --- Reservas -----------------------------------------------------------------------------
    @Test
    void verMisReservasConSuPuestoEnLaCola() throws Exception {
        mvc.perform(get("/api/user/reservas").with(token(LAURA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("El capitán Alatriste"))
                .andExpect(jsonPath("$[0].estado").value("ACTIVA"))
                .andExpect(jsonPath("$[0].puestoEnCola").value(1));
    }
    @Test
    void reservaUnLibroSinEjemplaresLibres() throws Exception {
// El señor de los anillos: un ejemplar, prestado a Javier
        mvc.perform(post("/api/user/reservas").with(token(LAURA))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"libroId\": 10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("El señor de los anillos"))
                .andExpect(jsonPath("$.puestoEnCola").value(1));
        mvc.perform(get("/api/user/reservas").with(token(LAURA))).andExpect(jsonPath("$", hasSize(2)));
    }
    @Test
    void unLibroConEjemplaresLibresNoSeReserva() throws Exception {
        mvc.perform(post("/api/user/reservas").with(token(LAURA))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"libroId\": 6}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("«1984» tiene ejemplares disponibles: puedes pedirlo prestado"));
    }
    @Test
    void sinLibroDevuelve400YUnLibroQueNoExiste404() throws Exception {
        mvc.perform(post("/api/user/reservas").with(token(LAURA))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.libroId").value("Indica el libro que quieres reservar"));
        mvc.perform(post("/api/user/reservas").with(token(LAURA))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"libroId\": 999}"))
                .andExpect(status().isNotFound());
    }
    @Test
    void cancelaSuReservaYNoLaDeOtro() throws Exception {
        mvc.perform(delete("/api/user/reservas/1").with(token(JAVIER))).andExpect(status().isNotFound());
        mvc.perform(delete("/api/user/reservas/1").with(token(LAURA))).andExpect(status().isNoContent());
        mvc.perform(delete("/api/user/reservas/1").with(token(LAURA)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Esa reserva ya estaba cancelada"));
    }
    // --- Favoritos ----------------------------------------------------------------------------
    @Test
    void marcaYQuitaFavoritos() throws Exception {
        mvc.perform(get("/api/user/favoritos").with(token(LAURA)))
                .andExpect(jsonPath("$[*].titulo", contains("1984", "El hobbit")));
        mvc.perform(put("/api/user/favoritos/4").with(token(LAURA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("La sombra del viento"));
        mvc.perform(delete("/api/user/favoritos/6").with(token(LAURA)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/user/favoritos").with(token(LAURA)))
                .andExpect(jsonPath("$[*].titulo", contains("El hobbit", "La sombra del viento")));
    }
    @Test
    void losFavoritosDeOtroNoCambian() throws Exception {
        mvc.perform(put("/api/user/favoritos/4").with(token(LAURA)));
        mvc.perform(get("/api/user/favoritos").with(token(JAVIER)))
                .andExpect(jsonPath("$", hasSize(0)));
    }
    @Test
    void unLibroQueNoExisteNoSeMarca() throws Exception {
        mvc.perform(put("/api/user/favoritos/999").with(token(LAURA))).andExpect(status().isNotFound());
    }
    @Test
    void elClienteSaleEnSwagger() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/api/user/reservas'].post.tags[0]").value("Cliente"))
                .andExpect(jsonPath("$.paths['/api/user/favoritos/{libroId}'].put").exists())
                .andExpect(jsonPath("$.paths['/api/user/prestamos/{id}/renovacion'].post.responses['403']")
                        .exists());
    }
}