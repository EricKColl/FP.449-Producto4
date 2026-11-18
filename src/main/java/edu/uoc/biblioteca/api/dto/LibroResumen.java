package edu.uoc.biblioteca.api.dto;

import edu.uoc.biblioteca.model.Autor;
import edu.uoc.biblioteca.model.Libro;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Un libro en un listado: lo justo para mostrarlo en una lista o en una tarjeta. */
@Schema(description = "Libro del catálogo, en un listado")
public record LibroResumen(
        @Schema(example = "2") Long id,
        @Schema(example = "978-84-9759-245-1") String isbn,
        @Schema(example = "Cien años de soledad") String titulo,
        @Schema(example = "[\"Gabriel García Márquez\"]") List<String> autores,
        @Schema(example = "Novela") String genero,
        @Schema(example = "1967") Integer anioPublicacion,
        @Schema(description = "Ejemplares que se pueden prestar ahora", example = "1") long disponibles) {

    public static LibroResumen de(Libro libro, long disponibles) {
        List<String> autores = libro.getAutores().stream().map(Autor::getNombreCompleto).toList();
        return new LibroResumen(libro.getId(), libro.getIsbn(), libro.getTitulo(), autores,
                libro.getGenero(), libro.getAnioPublicacion(), disponibles);
    }
}
