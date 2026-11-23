package edu.uoc.biblioteca.api;
import edu.uoc.biblioteca.api.dto.PrestamoDto;
import edu.uoc.biblioteca.service.PrestamoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/** Los préstamos de la cuenta del token. Solo con un token de USER (ApiSecurityConfig: /api/user/**). */
@RestController
@RequestMapping("/api/user/prestamos")
@Tag(name = "Cliente")
@SecurityRequirement(name = OpenApiConfig.TOKEN)
public class MisPrestamosApiController {
    private final PrestamoService prestamoService;
    public MisPrestamosApiController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }
    @GetMapping
    @Operation(summary = "Ver mis préstamos",
            description = "Los préstamos de la cuenta del token, del más reciente al más antiguo.")
    @ApiResponse(responseCode = "200", description = "Mis préstamos")
    public List<PrestamoDto> misPrestamos(@AuthenticationPrincipal Jwt token) {
        Long usuarioId = TokenService.usuarioId(token);
        return prestamoService.listarDeUsuario(usuarioId).stream().map(PrestamoDto::de).toList();
    }
    @PostMapping("/{id}/renovacion")
    @Operation(summary = "Renovar un préstamo",
            description = "Alarga 15 días el plazo. Solo una vez, dentro de plazo y si nadie "
                    + "ha reservado el libro.")
    @ApiResponse(responseCode = "200", description = "Préstamo renovado, con la nueva fecha")
    @ApiResponse(responseCode = "404", description = "No existe el préstamo o es de otra persona")
    @ApiResponse(responseCode = "409", description = "Ya se renovó, está fuera de plazo o hay reservas")
    public PrestamoDto renovarPrestamo(
            @AuthenticationPrincipal Jwt token,
            @Parameter(description = "Id del préstamo", example = "7") @PathVariable Long id) {
        return PrestamoDto.de(prestamoService.renovar(id, TokenService.usuarioId(token)));
    }
}