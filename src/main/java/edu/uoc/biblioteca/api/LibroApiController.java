package edu.uoc.biblioteca.api;

import edu.uoc.biblioteca.api.dto.LibroDetalle;
import edu.uoc.biblioteca.api.dto.LibroResumen;
import edu.uoc.biblioteca.model.Libro;
import edu.uoc.biblioteca.service.BusquedaService;
import edu.uoc.biblioteca.service.LibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Catálogo de libros abierto a cualquiera, sin token: la misma información que la página /libros. */
@RestController
@RequestMapping("/api/libros")
@Tag(name = "Catálogo")
public class LibroApiController {

    private final LibroService libroService;

    public LibroApiController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    @Operation(summary = "Listar y buscar libros",
            description = "Todos los libros ordenados por título. Con «q» busca en el título, "
                    + "el ISBN, los autores y el género, sin distinguir mayúsculas ni tildes. "
                    + "Con «genero» deja solo los de ese género.")
    @ApiResponse(responseCode = "200", description = "Los libros que cumplen la búsqueda (o ninguno)")
    public List<LibroResumen> listarLibros(
            @Parameter(description = "Texto que buscar, por ejemplo garcia")
            @RequestParam(required = false) String q,
            @Parameter(description = "Género, por ejemplo Fantasía")
            @RequestParam(required = false) String genero) {
        return libroService.buscar(q).stream()
                .filter(libro -> genero == null || genero.isBlank() || mismoGenero(libro, genero))
                .map(this::resumen)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver la ficha de un libro",
            description = "Datos del libro, sus autores y los ejemplares libres.")
    @ApiResponse(responseCode = "200", description = "La ficha del libro")
    @ApiResponse(responseCode = "400", description = "El id no es un número")
    @ApiResponse(responseCode = "404", description = "No existe ningún libro con ese id")
    public LibroDetalle fichaLibro(
            @Parameter(description = "Id del libro", example = "2") @PathVariable Long id) {
        Libro libro = libroService.buscarPorId(id);
        return LibroDetalle.de(libro, libroService.ejemplaresDisponibles(libro));
    }

    /** «fantasia» encuentra «Fantasía»: sin distinguir mayúsculas ni tildes, como en la web. */
    private static boolean mismoGenero(Libro libro, String genero) {
        return BusquedaService.normalizar(genero).equals(BusquedaService.normalizar(libro.getGenero()));
    }

    private LibroResumen resumen(Libro libro) {
        return LibroResumen.de(libro, libroService.ejemplaresDisponibles(libro));
    }
}
