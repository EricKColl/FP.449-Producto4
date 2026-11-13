package edu.uoc.biblioteca.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Respuesta de POST /api/auth/token. */
@Schema(description = "Token para las rutas protegidas: se envía en la cabecera «Authorization: Bearer <token>»")
public record TokenEmitido(
        @Schema(description = "Token JWT firmado", example = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJiaWJsaW90ZWNhLWJhY2tsb3JkIn0.firma")
        String token,

        @Schema(description = "Tipo de token: siempre Bearer", example = "Bearer")
        String tipo,

        @Schema(description = "Segundos que faltan para que caduque", example = "3600")
        long caducaEn,

        @Schema(description = "Rol de la cuenta", example = "ADMIN", allowableValues = {"ADMIN", "USER"})
        String rol) {
}
