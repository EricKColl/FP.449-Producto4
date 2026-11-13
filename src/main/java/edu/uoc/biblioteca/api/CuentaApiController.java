package edu.uoc.biblioteca.api;

import edu.uoc.biblioteca.api.dto.CuentaDto;
import edu.uoc.biblioteca.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Primer punto de acceso protegido: los datos de la cuenta del token. Sirve de ejemplo para /api/admin y
 * /api/user: el usuario sale del token con @AuthenticationPrincipal y TokenService.usuarioId.
 */
@RestController
@RequestMapping("/api/cuenta")
@Tag(name = "Token")
@SecurityRequirement(name = OpenApiConfig.TOKEN)
public class CuentaApiController {

    private final UsuarioService usuarioService;

    public CuentaApiController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Ver mi cuenta", description = "Datos de la cuenta a la que pertenece el token, de cualquier rol.")
    @ApiResponse(responseCode = "200", description = "La cuenta del token")
    public CuentaDto miCuenta(@AuthenticationPrincipal Jwt token) {
        return CuentaDto.de(usuarioService.buscarPorId(TokenService.usuarioId(token)));
    }
}
