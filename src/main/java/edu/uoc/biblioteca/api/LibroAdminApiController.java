package edu.uoc.biblioteca.api;

import edu.uoc.biblioteca.api.dto.LibroDetalle;
import edu.uoc.biblioteca.api.dto.LibroEntrada;
import edu.uoc.biblioteca.model.Autor;
import edu.uoc.biblioteca.model.Libro;
import edu.uoc.biblioteca.service.AutorService;
import edu.uoc.biblioteca.service.LibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

/** Alta, edición y baja de libros, reservadas a ADMIN por ApiSecurityConfig. */
@RestController
@RequestMapping("/api/admin/libros")
@Tag(name = "Administración")
@SecurityRequirement(name = OpenApiConfig.TOKEN)
public class LibroAdminApiController {

    private final LibroService libroService;
    private final AutorService autorService;

    public LibroAdminApiController(LibroService libroService, AutorService autorService) {
        this.libroService = libroService;
        this.autorService = autorService;
    }

    @PostMapping
    @Operation(summary = "Dar de alta un libro",
            description = "Crea el libro y devuelve su ficha y la dirección donde consultarla.")
    @ApiResponse(responseCode = "201", description = "Libro creado",
            headers = @Header(name = "Location", description = "Ruta de la ficha pública"))
    @ApiResponse(responseCode = "400", description = "Datos que no son válidos o un autor que no existe")
    @ApiResponse(responseCode = "409", description = "Ya hay otro libro con ese ISBN")
    public ResponseEntity<LibroDetalle> crearLibro(@Valid @RequestBody LibroEntrada datos) {
        Libro guardado = libroService.crear(aLibro(datos, null));
        return ResponseEntity.created(URI.create("/api/libros/" + guardado.getId())).body(detalle(guardado));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar un libro",
            description = "Cambia todos los datos del libro por los que se envían.")
    @ApiResponse(responseCode = "200", description = "Libro modificado")
    @ApiResponse(responseCode = "400", description = "Datos que no son válidos o un autor que no existe")
    @ApiResponse(responseCode = "404", description = "No existe ningún libro con ese id")
    @ApiResponse(responseCode = "409", description = "Ya hay otro libro con ese ISBN")
    public LibroDetalle modificarLibro(
            @Parameter(description = "Id del libro", example = "3") @PathVariable Long id,
            @Valid @RequestBody LibroEntrada datos) {
        libroService.buscarPorId(id);
        return detalle(libroService.actualizar(id, aLibro(datos, id)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un libro",
            description = "Solo si nunca se ha prestado: el historial de préstamos se conserva.")
    @ApiResponse(responseCode = "204", description = "Libro eliminado")
    @ApiResponse(responseCode = "400", description = "El id no es un número")
    @ApiResponse(responseCode = "404", description = "No existe ningún libro con ese id")
    @ApiResponse(responseCode = "409", description = "El libro tiene préstamos registrados")
    public void eliminarLibro(
            @Parameter(description = "Id del libro", example = "3") @PathVariable Long id) {
        libroService.eliminar(id);
    }

    private Libro aLibro(LibroEntrada datos, Long id) {
        Libro libro = new Libro(datos.isbn().strip(), datos.titulo().strip());
        libro.setId(id);
        libro.setEditorial(datos.editorial());
        libro.setAnioPublicacion(datos.anioPublicacion());
        libro.setGenero(datos.genero());
        libro.setEjemplares(datos.ejemplares());
        for (Long autorId : datos.autores()) {
            libro.getAutores().add(buscarAutor(autorId));
        }
        if (libroService.isbnRepetido(libro)) {
            throw new IllegalStateException("Ya existe otro libro con el ISBN " + libro.getIsbn());
        }
        return libro;
    }

    private Autor buscarAutor(Long id) {
        try {
            return autorService.buscarPorId(id);
        } catch (ResponseStatusException error) {
            if (error.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new IllegalArgumentException(error.getReason(), error);
            }
            throw error;
        }
    }

    private LibroDetalle detalle(Libro libro) {
        return LibroDetalle.de(libro, libroService.ejemplaresDisponibles(libro));
    }
}
