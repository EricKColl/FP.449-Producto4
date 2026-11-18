package edu.uoc.biblioteca.api.dto;

import edu.uoc.biblioteca.model.Autor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * La ficha de un autor con sus libros. Los libros los prepara el controlador, que es quien sabe
 * cuántos ejemplares quedan libres.
 */
@Schema(description = "Ficha de un autor con sus libros")
public record AutorDetalle(
        @Schema(example = "2") Long id,
        @Schema(example = "Gabriel") String nombre,
        @Schema(example = "García Márquez") String apellidos,
        @Schema(example = "Colombiana") String nacionalidad,
        @Schema(example = "1927-03-06") LocalDate fechaNacimiento,
        List<LibroResumen> libros) {

    public static AutorDetalle de(Autor autor, List<LibroResumen> libros) {
        return new AutorDetalle(autor.getId(), autor.getNombre(), autor.getApellidos(),
                autor.getNacionalidad(), autor.getFechaNacimiento(), libros);
    }
}
