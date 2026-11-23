package edu.uoc.biblioteca.api;
import edu.uoc.biblioteca.api.dto.LibroResumen;
import edu.uoc.biblioteca.model.Libro;
import edu.uoc.biblioteca.service.FavoritoService;
import edu.uoc.biblioteca.service.LibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
/** Los libros favoritos de la cuenta del token. Solo con un token de USER. */
@RestController
@RequestMapping("/api/user/favoritos")
@Tag(name = "Cliente")
@SecurityRequirement(name = OpenApiConfig.TOKEN)
public class MisFavoritosApiController {
    private final FavoritoService favoritoService;
    private final LibroService libroService;
    public MisFavoritosApiController(FavoritoService favoritoService, LibroService libroService) {
        this.favoritoService = favoritoService;
        this.libroService = libroService;
    }
    @GetMapping
    @Operation(summary = "Ver mis favoritos",
            description = "Los libros favoritos de la cuenta del token.")
    @ApiResponse(responseCode = "200", description = "Mis favoritos, por título")
    public List<LibroResumen> misFavoritos(@AuthenticationPrincipal Jwt token) {
        Long usuarioId = TokenService.usuarioId(token);
        return favoritoService.listar(usuarioId).stream().map(this::resumen).toList();
    }
    @PutMapping("/{libroId}")
    @Operation(summary = "Marcar un libro como favorito",
            description = "Si ya lo era, no cambia nada y no da error.")
    @ApiResponse(responseCode = "200", description = "El libro, ya en mis favoritos")
    @ApiResponse(responseCode = "404", description = "No existe el libro")
    public LibroResumen marcarFavorito(
            @AuthenticationPrincipal Jwt token,
            @Parameter(description = "Id del libro", example = "4") @PathVariable Long libroId) {
        return resumen(favoritoService.marcar(TokenService.usuarioId(token), libroId));
    }
    @DeleteMapping("/{libroId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Quitar un libro de mis favoritos",
            description = "Si no lo era, no cambia nada y no da error.")
    @ApiResponse(responseCode = "204", description = "El libro ya no está en mis favoritos")
    @ApiResponse(responseCode = "404", description = "No existe el libro")
    public void quitarFavorito(
            @AuthenticationPrincipal Jwt token,
            @Parameter(description = "Id del libro", example = "4") @PathVariable Long libroId) {
        favoritoService.quitar(TokenService.usuarioId(token), libroId);
    }
    private LibroResumen resumen(Libro libro) {
        return LibroResumen.de(libro, libroService.ejemplaresDisponibles(libro));
    }
}