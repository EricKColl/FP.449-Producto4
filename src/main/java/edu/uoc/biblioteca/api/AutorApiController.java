package edu.uoc.biblioteca.api;

import edu.uoc.biblioteca.api.dto.AutorDetalle;
import edu.uoc.biblioteca.api.dto.AutorResumen;
import edu.uoc.biblioteca.api.dto.LibroResumen;
import edu.uoc.biblioteca.model.Autor;
import edu.uoc.biblioteca.model.Libro;
import edu.uoc.biblioteca.service.AutorService;
import edu.uoc.biblioteca.service.LibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

/** Autores del catálogo, abiertos a cualquiera, sin token. */
@RestController
@RequestMapping("/api/autores")
@Tag(name = "Catálogo")
public class AutorApiController {

    private final AutorService autorService;
    private final LibroService libroService;

    public AutorApiController(AutorService autorService, LibroService libroService) {
        this.autorService = autorService;
        this.libroService = libroService;
    }

    @GetMapping
    @Operation(summary = "Listar los autores",
            description = "Todos los autores, ordenados por apellidos, con cuántos libros tienen.")
    @ApiResponse(responseCode = "200", description = "Los autores")
    public List<AutorResumen> listarAutores() {
        return autorService.listar().stream().map(AutorResumen::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver la ficha de un autor",
            description = "Datos del autor y sus libros, ordenados por título.")
    @ApiResponse(responseCode = "200", description = "La ficha del autor")
    @ApiResponse(responseCode = "400", description = "El id no es un número")
    @ApiResponse(responseCode = "404", description = "No existe ningún autor con ese id")
    public AutorDetalle fichaAutor(
            @Parameter(description = "Id del autor", example = "2") @PathVariable Long id) {
        Autor autor = autorService.buscarPorId(id);
        List<LibroResumen> libros = autor.getLibros().stream()
                .sorted(Comparator.comparing(Libro::getTitulo))
                .map(libro -> LibroResumen.de(libro, libroService.ejemplaresDisponibles(libro)))
                .toList();
        return AutorDetalle.de(autor, libros);
    }
}
