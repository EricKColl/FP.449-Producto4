package edu.uoc.biblioteca.api.dto;

import edu.uoc.biblioteca.model.Libro;
import edu.uoc.biblioteca.model.Prestamo;
import edu.uoc.biblioteca.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/** Un préstamo con el libro y el usuario resumidos en su identificador y su nombre. */
@Schema(description = "Préstamo de un libro")
public record PrestamoDto(
        @Schema(example = "7") Long id,
        @Schema(example = "2") Long libroId,
        @Schema(example = "Cien años de soledad") String titulo,
        @Schema(example = "1") Long usuarioId,
        @Schema(example = "Laura Martínez Soler") String usuario,
        @Schema(example = "2026-11-08") LocalDate fechaPrestamo,
        @Schema(example = "2026-11-23") LocalDate fechaDevolucionPrevista,
        @Schema(description = "Fecha de devolución, nula mientras el préstamo siga abierto")
        LocalDate fechaDevolucion,
        @Schema(example = "ACTIVO", allowableValues = {"ACTIVO", "VENCIDO", "DEVUELTO"}) String estado,
        @Schema(description = "Indica si el préstamo ya se renovó") boolean renovado) {

    public static PrestamoDto de(Prestamo prestamo) {
        Libro libro = prestamo.getLibro();
        Usuario usuario = prestamo.getUsuario();
        return new PrestamoDto(prestamo.getId(), libro.getId(), libro.getTitulo(), usuario.getId(),
                usuario.getNombreCompleto(), prestamo.getFechaPrestamo(), prestamo.getFechaDevolucionPrevista(),
                prestamo.getFechaDevolucion(), prestamo.getEstado().name(), prestamo.isRenovado());
    }
}
