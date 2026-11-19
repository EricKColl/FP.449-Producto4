package edu.uoc.biblioteca.api;

import edu.uoc.biblioteca.api.dto.PrestamoDto;
import edu.uoc.biblioteca.model.EstadoPrestamo;
import edu.uoc.biblioteca.service.PrestamoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Listado de préstamos y devoluciones reservados a ADMIN por ApiSecurityConfig. */
@RestController
@RequestMapping("/api/admin/prestamos")
@Tag(name = "Administración")
@SecurityRequirement(name = OpenApiConfig.TOKEN)
public class PrestamoAdminApiController {

    private final PrestamoService prestamoService;

    public PrestamoAdminApiController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    @Operation(summary = "Listar los préstamos",
            description = "Del más reciente al más antiguo. Con «estado», solo los ACTIVO (dentro de plazo), "
                    + "VENCIDO (fuera de plazo y sin devolver) o DEVUELTO.")
    @ApiResponse(responseCode = "200", description = "Los préstamos")
    @ApiResponse(responseCode = "400", description = "El estado no es ACTIVO, VENCIDO ni DEVUELTO")
    public List<PrestamoDto> listarPrestamos(
            @Parameter(description = "Estado por el que filtrar")
            @RequestParam(required = false) EstadoPrestamo estado) {
        return prestamoService.listar(estado).stream().map(PrestamoDto::de).toList();
    }

    @PostMapping("/{id}/devolucion")
    @Operation(summary = "Registrar una devolución",
            description = "Marca el préstamo como devuelto hoy y actualiza los avisos de la biblioteca.")
    @ApiResponse(responseCode = "200", description = "Préstamo devuelto")
    @ApiResponse(responseCode = "400", description = "El id no es un número")
    @ApiResponse(responseCode = "404", description = "No existe ningún préstamo con ese id")
    @ApiResponse(responseCode = "409", description = "El préstamo ya estaba devuelto")
    public PrestamoDto registrarDevolucion(
            @Parameter(description = "Id del préstamo", example = "6") @PathVariable Long id) {
        return PrestamoDto.de(prestamoService.registrarDevolucion(id));
    }
}
