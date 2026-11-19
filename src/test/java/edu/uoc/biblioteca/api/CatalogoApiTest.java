package edu.uoc.biblioteca.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** /api/libros y /api/autores: el catálogo abierto, sin token, con los datos de 02-datos.sql. */
class CatalogoApiTest extends ApiTest {

    @Test
    void abreSoloLasConsultasDelCatalogoSinToken() throws Exception {
        mvc.perform(get("/api/libros"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(14)))
                .andExpect(jsonPath("$[0].titulo").value("1984"))
                .andExpect(jsonPath("$[0].autores[0]").value("George Orwell"));
        for (String ruta : new String[]{"/api/libros", "/api/libros/2", "/api/autores", "/api/autores/2"}) {
            for (HttpMethod metodo : new HttpMethod[]{HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE}) {
                mvc.perform(request(metodo, ruta))
                        .andExpect(status().isUnauthorized());
            }
        }
        mvc.perform(get("/api/libros/2/privado"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/autores/2/privado"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/libros").param("q", " ").param("genero", " "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(14)));
    }

    @Test
    void buscaSinDistinguirTildes() throws Exception {
        mvc.perform(get("/api/libros").param("q", " GARCIA "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].titulo",
                        contains("Cien años de soledad", "Crónica de una muerte anunciada")));
    }

    @Test
    void filtraPorGenero() throws Exception {
        mvc.perform(get("/api/libros").param("genero", "fantasia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[*].genero", everyItem(is("Fantasía"))));
        mvc.perform(get("/api/libros").param("q", "tolkien").param("genero", " FANTASIA "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].titulo", contains("El hobbit", "El señor de los anillos")));
        mvc.perform(get("/api/libros").param("q", "tolkien").param("genero", "Novela"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void unaBusquedaSinResultadosDevuelveUnaListaVacia() throws Exception {
        mvc.perform(get("/api/libros").param("q", "zzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void laFichaDeUnLibroTraeSusAutoresYLosEjemplaresLibres() throws Exception {
        // Cien años de soledad: 2 ejemplares y 1 prestado (préstamo 7)
        mvc.perform(get("/api/libros/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Cien años de soledad"))
                .andExpect(jsonPath("$.ejemplares").value(2))
                .andExpect(jsonPath("$.disponibles").value(1))
                .andExpect(jsonPath("$.autores[0].apellidos").value("García Márquez"));
    }

    @Test
    void noDevuelveLosPrestamosNiQuienLoTiene() throws Exception {
        mvc.perform(get("/api/libros/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prestamos").doesNotExist())
                .andExpect(jsonPath("$.marcadoPor").doesNotExist())
                .andExpect(jsonPath("$.reservas").doesNotExist())
                .andExpect(jsonPath("$.autores[0].libros").value(2));
        mvc.perform(get("/api/autores/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libros[0].prestamos").doesNotExist())
                .andExpect(jsonPath("$.libros[0].autores[0]").value("Gabriel García Márquez"));
    }

    @Test
    void unLibroQueNoExisteDevuelve404EnJson() throws Exception {
        mvc.perform(get("/api/libros/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("No existe el libro 999"));
    }

    @Test
    void unIdQueNoEsUnNumeroDevuelve400() throws Exception {
        for (String ruta : new String[]{"/api/libros/abc", "/api/autores/abc"}) {
            mvc.perform(get(ruta))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.detail").value("El valor «abc» no vale para el parámetro id"));
        }
    }

    @Test
    void listaLosAutoresConCuantosLibrosTienen() throws Exception {
        mvc.perform(get("/api/autores"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(11)))
                .andExpect(jsonPath("$[0].apellidos").value("Allende"))
                .andExpect(jsonPath("$[?(@.apellidos == 'García Márquez')].libros", hasItem(2)));
    }

    @Test
    void laFichaDeUnAutorTraeSusLibrosPorTitulo() throws Exception {
        mvc.perform(get("/api/autores/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Gabriel"))
                .andExpect(jsonPath("$.fechaNacimiento").value("1927-03-06"))
                .andExpect(jsonPath("$.libros[*].titulo",
                        contains("Cien años de soledad", "Crónica de una muerte anunciada")));
    }

    @Test
    void unAutorQueNoExisteDevuelve404() throws Exception {
        mvc.perform(get("/api/autores/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("No existe el autor 999"));
    }

    @Test
    void elCatalogoSaleEnSwagger() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/libros'].get.tags[0]").value("Catálogo"))
                .andExpect(jsonPath("$.paths['/api/libros/{id}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/autores'].get").exists())
                .andExpect(jsonPath("$.paths['/api/autores/{id}'].get.security").doesNotExist());
        var documentacion = mvc.perform(get("/v3/api-docs"));
        for (String ruta : new String[]{"/api/libros", "/api/libros/{id}", "/api/autores", "/api/autores/{id}"}) {
            documentacion
                    .andExpect(jsonPath("$.paths['" + ruta + "'].get.security").doesNotExist())
                    .andExpect(jsonPath("$.paths['" + ruta + "'].get.responses['200']").exists());
        }
    }
}
