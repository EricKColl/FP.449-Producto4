package edu.uoc.biblioteca.api.dto;

import edu.uoc.biblioteca.model.Autor;
import io.swagger.v3.oas.annotations.media.Schema;

/** Un autor en un listado o dentro de la ficha de un libro. */
@Schema(description = "Autor del catálogo")
public record AutorResumen(
        @Schema(example = "2") Long id,
        @Schema(example = "Gabriel") String nombre,
        @Schema(example = "García Márquez") String apellidos,
        @Schema(example = "Colombiana") String nacionalidad,
        @Schema(description = "Libros suyos en el catálogo", example = "2") int libros) {

    public static AutorResumen de(Autor autor) {
        return new AutorResumen(autor.getId(), autor.getNombre(), autor.getApellidos(),
                autor.getNacionalidad(), autor.getLibros().size());
    }
}
