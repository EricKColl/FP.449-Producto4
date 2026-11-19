package edu.uoc.biblioteca.api.dto;

import edu.uoc.biblioteca.model.Usuario;
import edu.uoc.biblioteca.service.UsuarioService.UsuarioConPrestamos;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/** Datos del usuario para el listado de administración. */
@Schema(description = "Usuario de la biblioteca, sin contraseña")
public record UsuarioResumen(
        @Schema(example = "1") Long id,
        @Schema(example = "12345678Z") String dni,
        @Schema(example = "Laura") String nombre,
        @Schema(example = "Martínez Soler") String apellidos,
        @Schema(example = "laura.martinez@example.com") String email,
        @Schema(example = "612345678") String telefono,
        @Schema(example = "USER", allowableValues = {"ADMIN", "USER"}) String rol,
        @Schema(example = "2025-10-09") LocalDate fechaAlta,
        @Schema(description = "Préstamos sin devolver, incluidos los vencidos", example = "1")
        long prestamosEnCurso,
        @Schema(description = "Préstamos sin devolver y fuera de plazo", example = "0")
        long prestamosVencidos) {

    public static UsuarioResumen de(UsuarioConPrestamos fila) {
        Usuario usuario = fila.usuario();
        return new UsuarioResumen(usuario.getId(), usuario.getDni(), usuario.getNombre(), usuario.getApellidos(),
                usuario.getEmail(), usuario.getTelefono(), usuario.getRol().name(), usuario.getFechaAlta(),
                fila.enCurso(), fila.vencidos());
    }
}
