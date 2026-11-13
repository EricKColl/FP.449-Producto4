package edu.uoc.biblioteca.api;

import edu.uoc.biblioteca.api.dto.Credenciales;
import edu.uoc.biblioteca.api.dto.TokenEmitido;
import edu.uoc.biblioteca.security.CuentaDetailsService;
import edu.uoc.biblioteca.security.CuentaUsuario;
import edu.uoc.biblioteca.security.IntentosDeAcceso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Entrega de tokens. Comprueba el email y la contraseña igual que el login de la web (misma cuenta, BCrypt y
 * bloqueo tras cinco fallos seguidos) y, si son correctos, devuelve un token JWT firmado.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Token", description = "Pedir el token que abre las rutas protegidas")
public class AuthApiController {

    private final DaoAuthenticationProvider comprobador;
    private final IntentosDeAcceso intentos;
    private final TokenService tokens;

    public AuthApiController(CuentaDetailsService cuentas, PasswordEncoder cifrado, IntentosDeAcceso intentos,
                             TokenService tokens) {
        this.comprobador = new DaoAuthenticationProvider(cuentas);
        this.comprobador.setPasswordEncoder(cifrado);
        this.intentos = intentos;
        this.tokens = tokens;
    }

    @PostMapping("/token")
    @Operation(summary = "Pedir un token",
            description = "Con el email y la contraseña de una cuenta devuelve un token JWT que caduca en 60 minutos. "
                    + "Copia el token, pulsa «Authorize» arriba a la derecha y pégalo para probar las rutas protegidas. "
                    + "Cuentas de prueba: admin@example.com / Admin1234 (ADMIN) y laura.martinez@example.com / "
                    + "Usuario1234 (USER).")
    @ApiResponse(responseCode = "200", description = "Credenciales correctas: aquí está el token")
    @ApiResponse(responseCode = "400", description = "Falta el email o la contraseña, o el JSON no es válido")
    @ApiResponse(responseCode = "401", description = "Email o contraseña incorrectos")
    @ApiResponse(responseCode = "429", description = "Demasiados intentos fallidos: la cuenta queda bloqueada unos minutos")
    public TokenEmitido pedirToken(@Valid @RequestBody Credenciales credenciales) {
        String email = credenciales.email().strip();
        if (intentos.bloqueado(email)) {
            throw bloqueada();
        }
        try {
            Authentication cuenta = comprobador.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, credenciales.password()));
            intentos.registrarExito(email);
            return tokens.emitir((CuentaUsuario) cuenta.getPrincipal());
        } catch (LockedException error) {
            throw bloqueada();
        } catch (AuthenticationException error) {
            if (intentos.registrarFallo(email)) {
                throw bloqueada();
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
        }
    }

    private ResponseStatusException bloqueada() {
        return new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos fallidos con este email. "
                + "Vuelve a probar dentro de " + intentos.minutosDeBloqueo() + " minutos.");
    }
}
