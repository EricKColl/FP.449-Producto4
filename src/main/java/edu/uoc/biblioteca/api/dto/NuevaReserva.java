package edu.uoc.biblioteca.api.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
/** Cuerpo de POST /api/user/reservas: solo el libro. El usuario sale del token. */
@Schema(description = "Libro que se quiere reservar")
public record NuevaReserva(
        @Schema(example = "10")
        @NotNull(message = "Indica el libro que quieres reservar")
        Long libroId) {
}