package edu.uoc.biblioteca.api;

import edu.uoc.biblioteca.api.dto.UsuarioResumen;
import edu.uoc.biblioteca.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Listado de usuarios reservado a ADMIN por ApiSecurityConfig. */
@RestController
@RequestMapping("/api/admin/usuarios")
@Tag(name = "Administración")
@SecurityRequirement(name = OpenApiConfig.TOKEN)
public class UsuarioAdminApiController {

    private final UsuarioService usuarioService;

    public UsuarioAdminApiController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar los usuarios",
            description = "Todos los usuarios ordenados por apellidos y nombre, con sus recuentos "
                    + "de préstamos en curso y vencidos. Nunca se devuelve la contraseña.")
    @ApiResponse(responseCode = "200", description = "Los usuarios")
    public List<UsuarioResumen> listarUsuarios() {
        return usuarioService.listarConPrestamos().stream().map(UsuarioResumen::de).toList();
    }
}
