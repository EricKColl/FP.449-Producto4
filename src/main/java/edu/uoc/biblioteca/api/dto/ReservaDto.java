package edu.uoc.biblioteca.api.dto;
import edu.uoc.biblioteca.model.Reserva;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
/** Una reserva del usuario del token, con su puesto en la cola del libro. */
@Schema(description = "Reserva de un libro sin ejemplares libres")
public record ReservaDto(
        @Schema(example = "1") Long id,
        @Schema(example = "8") Long libroId,
        @Schema(example = "El capitán Alatriste") String titulo,
        @Schema(example = "2026-11-12") LocalDate fechaReserva,
        @Schema(example = "ACTIVA", allowableValues = {"ACTIVA", "CANCELADA"}) String estado,
        @Schema(description = "1 = la siguiente en recibir el libro; 0 = ya no está en la cola",
                example = "1") int puestoEnCola) {
    public static ReservaDto de(Reserva reserva, int puestoEnCola) {
        return new ReservaDto(reserva.getId(), reserva.getLibro().getId(), reserva.getLibro().getTitulo(),
                reserva.getFechaReserva(), reserva.getEstado().name(), puestoEnCola);
    }
}