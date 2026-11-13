package edu.uoc.biblioteca.api.dto;

import edu.uoc.biblioteca.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/** La cuenta del token, sin la contraseña. */
@Schema(description = "Datos de la cuenta que ha pedido el token")
public record CuentaDto(
        @Schema(example = "7") Long id,
        @Schema(example = "Admin") String nombre,
        @Schema(example = "Biblioteca") String apellidos,
        @Schema(example = "admin@example.com") String email,
        @Schema(example = "ADMIN", allowableValues = {"ADMIN", "USER"}) String rol,
        @Schema(example = "2025-10-09") LocalDate fechaAlta) {

    public static CuentaDto de(Usuario usuario) {
        return new CuentaDto(usuario.getId(), usuario.getNombre(), usuario.getApellidos(), usuario.getEmail(),
                usuario.getRol().name(), usuario.getFechaAlta());
    }
}
