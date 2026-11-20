package edu.uoc.biblioteca.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** /api/admin/libros: alta, edición y baja de libros con los tres perfiles (sin token, USER y ADMIN). */
class LibroAdminApiTest extends ApiTest {

    private static final String LIBROS = "/api/admin/libros";
    private static final String EVA_LUNA = """
            {"isbn": "978-84-9759-243-7", "titulo": "Eva Luna", "editorial": "Debolsillo",
             "anioPublicacion": 1987, "genero": "Novela", "ejemplares": 2, "autores": [3]}
            """;

    @Test
    void sinTokenDevuelve401EnLasTresOperaciones() throws Exception {
        mvc.perform(post(LIBROS).contentType(MediaType.APPLICATION_JSON).content(EVA_LUNA))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(put(LIBROS + "/13").contentType(MediaType.APPLICATION_JSON).content(EVA_LUNA))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete(LIBROS + "/13")).andExpect(status().isUnauthorized());
    }

    @Test
    void conTokenDeUsuarioDevuelve403EnLasTresOperaciones() throws Exception {
        mvc.perform(post(LIBROS).with(token(LAURA)).contentType(MediaType.APPLICATION_JSON).content(EVA_LUNA))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(put(LIBROS + "/13").with(token(LAURA))
                        .contentType(MediaType.APPLICATION_JSON).content(EVA_LUNA))
                .andExpect(status().isForbidden());
        mvc.perform(delete(LIBROS + "/13").with(token(LAURA))).andExpect(status().isForbidden());
    }

    @Test
    void creaUnLibroYDevuelveLaDireccionDeLaFichaPublica() throws Exception {
        String location = mvc.perform(post(LIBROS).with(token(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(EVA_LUNA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/libros/")))
                .andExpect(jsonPath("$.titulo").value("Eva Luna"))
                .andExpect(jsonPath("$.disponibles").value(2))
                .andExpect(jsonPath("$.autores[0].apellidos").value("Allende"))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("978-84-9759-243-7"));
    }

    @Test
    void modificaUnLibroConservandoSuIsbnYReemplazaSusDatos() throws Exception {
        mvc.perform(put(LIBROS + "/3").with(token(ADMIN)).contentType(MediaType.APPLICATION_JSON).content("""
                        {"isbn": "978-84-01-24214-4", "titulo": "La casa de los espíritus (edición especial)",
                         "ejemplares": 3, "autores": [3]}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.titulo").value("La casa de los espíritus (edición especial)"))
                .andExpect(jsonPath("$.ejemplares").value(3))
                .andExpect(jsonPath("$.autores[0].id").value(3));

        mvc.perform(get("/api/libros/3")).andExpect(status().isOk())
                .andExpect(jsonPath("$.ejemplares").value(3))
                .andExpect(jsonPath("$.editorial").doesNotExist());
    }

    @Test
    void eliminaUnLibroSinPrestamosYAlRepetirDevuelve404() throws Exception {
        mvc.perform(delete(LIBROS + "/13").with(token(ADMIN)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mvc.perform(get("/api/libros/13")).andExpect(status().isNotFound());
        mvc.perform(delete(LIBROS + "/13").with(token(ADMIN))).andExpect(status().isNotFound());
    }

    @Test
    void devuelve400ConLosCamposQueNoSonValidos() throws Exception {
        mvc.perform(post(LIBROS).with(token(ADMIN)).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn": "", "titulo": "", "ejemplares": 0, "autores": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errores.isbn").value("El ISBN es obligatorio"))
                .andExpect(jsonPath("$.errores.titulo").value("El título es obligatorio"))
                .andExpect(jsonPath("$.errores.ejemplares").value("Debe haber al menos un ejemplar"))
                .andExpect(jsonPath("$.errores.autores").value("Indica al menos un autor"));

        mvc.perform(put(LIBROS + "/3").with(token(ADMIN)).contentType(MediaType.APPLICATION_JSON)
                        .content(EVA_LUNA.replace("[3]", "[null]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores").isMap());
    }

    @Test
    void devuelve400SiElAutorNoExiste() throws Exception {
        mvc.perform(post(LIBROS).with(token(ADMIN)).contentType(MediaType.APPLICATION_JSON)
                        .content(EVA_LUNA.replace("[3]", "[99]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("No existe el autor 99"));
    }

    @Test
    void devuelve409SiOtroLibroTieneElIsbn() throws Exception {
        String duplicado = EVA_LUNA.replace("978-84-9759-243-7", "978-84-376-2405-1");
        mvc.perform(post(LIBROS).with(token(ADMIN)).contentType(MediaType.APPLICATION_JSON).content(duplicado))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Ya existe otro libro con el ISBN 978-84-376-2405-1"));
        mvc.perform(put(LIBROS + "/3").with(token(ADMIN)).contentType(MediaType.APPLICATION_JSON).content(duplicado))
                .andExpect(status().isConflict());
    }

    @Test
    void devuelve404AlModificarUnLibroQueNoExiste() throws Exception {
        mvc.perform(put(LIBROS + "/999").with(token(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(EVA_LUNA))
                .andExpect(status().isNotFound());
    }

    @Test
    void devuelve409AlEliminarUnLibroConPrestamos() throws Exception {
        mvc.perform(delete(LIBROS + "/1").with(token(ADMIN)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No se puede eliminar «Don Quijote de la Mancha» "
                        + "porque tiene préstamos registrados"));
        mvc.perform(get("/api/libros/1")).andExpect(status().isOk());
    }

    @Test
    void documentaLasTresOperacionesEnOpenApi() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/admin/libros'].post.tags[0]").value("Administración"))
                .andExpect(jsonPath("$.paths['/api/admin/libros'].post.responses['201'].headers.Location").exists())
                .andExpect(jsonPath("$.paths['/api/admin/libros'].post.security[0].token").exists())
                .andExpect(jsonPath("$.paths['/api/admin/libros/{id}'].put.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/admin/libros/{id}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.paths['/api/admin/libros/{id}'].delete.responses['403']").exists());
    }
}
