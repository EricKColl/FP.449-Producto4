package edu.uoc.biblioteca.api;

import edu.uoc.biblioteca.api.dto.TokenEmitido;
import edu.uoc.biblioteca.security.CuentaUsuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Crea los tokens JWT de la API. Un token es un texto firmado con tres partes (cabecera, datos y firma):
 * cualquiera puede leer los datos, pero nadie puede cambiarlos sin que la firma deje de cuadrar.
 * Datos que lleva: quién es (sub = email, uid = id), su rol, cuándo se emitió y cuándo caduca.
 */
@Service
public class TokenService {

    /** Quién emite los tokens: el lector solo acepta los que llevan este emisor. */
    public static final String EMISOR = "biblioteca-backlord";
    static final String CAMPO_ROL = "rol";
    static final String CAMPO_ID = "uid";

    private final JwtEncoder codificador;
    private final Duration duracion;

    public TokenService(JwtEncoder codificador, @Value("${biblioteca.api.minutos-token:60}") long minutos) {
        this.codificador = codificador;
        this.duracion = Duration.ofMinutes(minutos);
    }

    public TokenEmitido emitir(CuentaUsuario cuenta) {
        String rol = cuenta.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(autoridad -> autoridad.replaceFirst("^ROLE_", ""))
                .findFirst()
                .orElseThrow();
        Instant ahora = Instant.now();
        JwtClaimsSet datos = JwtClaimsSet.builder()
                .issuer(EMISOR)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(duracion))
                .subject(cuenta.getUsername())
                .claim(CAMPO_ID, cuenta.getId())
                .claim(CAMPO_ROL, rol)
                .claim("nombre", cuenta.getNombre())
                .build();
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = codificador.encode(JwtEncoderParameters.from(cabecera, datos)).getTokenValue();
        return new TokenEmitido(token, "Bearer", duracion.toSeconds(), rol);
    }

    /**
     * Id del usuario del token. En las rutas protegidas se usa con {@code @AuthenticationPrincipal Jwt token}:
     * el usuario sale siempre del token y nunca de la URL ni del cuerpo, para que nadie actúe en nombre de otro.
     */
    public static Long usuarioId(Jwt token) {
        Number id = token.getClaim(CAMPO_ID);
        return id.longValue();
    }
}
