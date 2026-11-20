package edu.uoc.biblioteca.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Cuerpo de POST y PUT /api/admin/libros. Las reglas son las mismas que las del formulario de la web. */
@Schema(description = "Datos de un libro para darlo de alta o modificarlo")
public record LibroEntrada(
        @Schema(example = "978-84-9759-243-7")
        @NotBlank(message = "El ISBN es obligatorio")
        @Size(max = 17, message = "El ISBN no puede superar los 17 caracteres")
        String isbn,

        @Schema(example = "Eva Luna")
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String titulo,

        @Schema(example = "Debolsillo")
        @Size(max = 100, message = "La editorial no puede superar los 100 caracteres")
        String editorial,

        @Schema(example = "1987")
        @Positive(message = "El año de publicación debe ser un número positivo")
        Integer anioPublicacion,

        @Schema(example = "Novela")
        @Size(max = 50, message = "El género no puede superar los 50 caracteres")
        String genero,

        @Schema(example = "2")
        @NotNull(message = "El número de ejemplares es obligatorio")
        @Min(value = 1, message = "Debe haber al menos un ejemplar")
        Integer ejemplares,

        @Schema(description = "Ids de los autores", example = "[3]")
        @NotEmpty(message = "Indica al menos un autor")
        List<@NotNull(message = "Un autor no puede ser null") Long> autores) {
}
