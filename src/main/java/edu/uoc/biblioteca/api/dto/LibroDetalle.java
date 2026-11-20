package edu.uoc.biblioteca.api.dto;

import edu.uoc.biblioteca.model.Libro;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** La ficha completa de un libro, con sus autores. */
@Schema(description = "Ficha de un libro")
public record LibroDetalle(
        @Schema(example = "2") Long id,
        @Schema(example = "978-84-9759-245-1") String isbn,
        @Schema(example = "Cien años de soledad") String titulo,
        @Schema(example = "Debolsillo") String editorial,
        @Schema(example = "1967") Integer anioPublicacion,
        @Schema(example = "Novela") String genero,
        @Schema(description = "Ejemplares que tiene la biblioteca", example = "2") int ejemplares,
        @Schema(description = "Ejemplares que se pueden prestar ahora", example = "1") long disponibles,
        List<AutorResumen> autores) {

    public static LibroDetalle de(Libro libro, long disponibles) {
        List<AutorResumen> autores = libro.getAutores().stream().map(AutorResumen::de).toList();
        return new LibroDetalle(libro.getId(), libro.getIsbn(), libro.getTitulo(), libro.getEditorial(),
                libro.getAnioPublicacion(), libro.getGenero(), libro.getEjemplares(), disponibles,
                autores);
    }
}
