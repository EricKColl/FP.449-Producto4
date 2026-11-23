package edu.uoc.biblioteca.api;
import edu.uoc.biblioteca.api.dto.NuevaReserva;
import edu.uoc.biblioteca.api.dto.ReservaDto;
import edu.uoc.biblioteca.model.Reserva;
import edu.uoc.biblioteca.service.PrestamoService;
import edu.uoc.biblioteca.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
/** Las reservas de la cuenta del token. Solo con un token de USER. */
@RestController
@RequestMapping("/api/user/reservas")
@Tag(name = "Cliente")
@SecurityRequirement(name = OpenApiConfig.TOKEN)
public class MisReservasApiController {
    private final ReservaService reservaService;
    private final PrestamoService prestamoService;
    public MisReservasApiController(ReservaService reservaService, PrestamoService prestamoService) {
        this.reservaService = reservaService;
        this.prestamoService = prestamoService;
    }
    @GetMapping
    @Operation(summary = "Ver mis reservas",
            description = "Las reservas de la cuenta del token, activas y canceladas, con el puesto de "
                    + "cada una en la cola de su libro.")
    @ApiResponse(responseCode = "200", description = "Mis reservas")
    public List<ReservaDto> misReservas(@AuthenticationPrincipal Jwt token) {
        Long usuarioId = TokenService.usuarioId(token);
        return reservaService.listarDeUsuario(usuarioId).stream().map(this::dto).toList();
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Reservar un libro",
            description = "Solo los libros que no tienen ejemplares libres: los demás se pueden pedir "
                    + "prestados. La reserva se pone a la cola del libro.")
    @ApiResponse(responseCode = "201", description = "Reserva hecha, con su puesto en la cola")
    @ApiResponse(responseCode = "400", description = "Falta el libro")
    @ApiResponse(responseCode = "404", description = "No existe el libro")
    @ApiResponse(responseCode = "409",
            description = "Tiene ejemplares libres, ya lo tienes o ya lo reservaste")
    public ReservaDto reservarLibro(@AuthenticationPrincipal Jwt token,
                                    @Valid @RequestBody NuevaReserva datos) {
        return dto(reservaService.reservar(TokenService.usuarioId(token), datos.libroId()));
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancelar una reserva",
            description = "Los que iban detrás en la cola suben un puesto.")
    @ApiResponse(responseCode = "204", description = "Reserva cancelada")
    @ApiResponse(responseCode = "404", description = "No existe la reserva o es de otra persona")
    @ApiResponse(responseCode = "409", description = "La reserva ya estaba cancelada")
    public void cancelarReserva(
            @AuthenticationPrincipal Jwt token,
            @Parameter(description = "Id de la reserva", example = "1") @PathVariable Long id) {
        reservaService.cancelar(id, TokenService.usuarioId(token));
    }
    private ReservaDto dto(Reserva reserva) {
        return ReservaDto.de(reserva, prestamoService.puestoEnCola(reserva));
    }
}