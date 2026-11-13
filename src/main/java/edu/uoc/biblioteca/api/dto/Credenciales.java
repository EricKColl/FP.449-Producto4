package edu.uoc.biblioteca.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Cuerpo de POST /api/auth/token: las mismas credenciales que el formulario de login. */
@Schema(description = "Email y contraseña de una cuenta de la biblioteca")
public record Credenciales(
        @Schema(example = "admin@example.com")
        @NotBlank(message = "Escribe el email")
        String email,

        @Schema(example = "Admin1234")
        @NotBlank(message = "Escribe la contraseña")
        String password) {
}
