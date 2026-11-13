package edu.uoc.biblioteca.api;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.ResultActions;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** POST /api/auth/token y GET /api/cuenta: pedir un token y usarlo. */
class TokenApiTest extends ApiTest {

    @Autowired
    private SecretKey clave;

    private ResultActions pedirToken(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    @Test
    void conCredencialesCorrectasDevuelveUnToken() throws Exception {
        pedirToken(ADMIN, "Admin1234")
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.caducaEn").value(3600))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void elTokenSirveParaVerLaCuenta() throws Exception {
        String respuesta = pedirToken(LAURA, "Usuario1234").andReturn().getResponse().getContentAsString();
        String token = respuesta.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        mvc.perform(get("/api/cuenta").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value(LAURA))
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void conLaContrasenaMalDevuelve401EnJson() throws Exception {
        pedirToken(ADMIN, "otra")
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Email o contraseña incorrectos"));
    }

    @Test
    void unEmailQueNoExisteDaElMismoErrorQueUnaContrasenaMal() throws Exception {
        pedirToken("nadie@example.com", "Admin1234")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Email o contraseña incorrectos"));
    }

    @Test
    void cincoFallosSeguidosBloqueanElEmail() throws Exception {
        String email = "daniel.navarro@example.com";
        for (int i = 0; i < 4; i++) {
            pedirToken(email, "mal").andExpect(status().isUnauthorized());
        }
        pedirToken(email, "mal").andExpect(status().isTooManyRequests());
        // Bloqueado, ni con la contraseña buena
        pedirToken(email, "Usuario1234")
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.detail", containsString("5 minutos")));
    }

    @Test
    void sinEmailNiContrasenaDevuelve400ConLosCampos() throws Exception {
        mvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.email").value("Escribe el email"))
                .andExpect(jsonPath("$.errores.password").value("Escribe la contraseña"));
    }

    @Test
    void unJsonMalEscritoDevuelve400() throws Exception {
        mvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON).content("{email:"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El cuerpo de la petición no es un JSON válido"));
    }

    @Test
    void sinTokenDevuelve401ConLaCabeceraEstandar() throws Exception {
        mvc.perform(get("/api/cuenta"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", startsWith("Bearer")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail", containsString("POST /api/auth/token")));
    }

    @Test
    void unTokenInventadoDevuelve401() throws Exception {
        mvc.perform(get("/api/cuenta").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail", containsString("no es válido")));
    }

    @Test
    void unTokenFirmadoConOtraClaveDevuelve401() throws Exception {
        SecretKey otra = new SecretKeySpec("otra-clave-de-32-caracteres-o-mas!!".getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
        mvc.perform(get("/api/cuenta").header("Authorization",
                        "Bearer " + firmar(otra, TokenService.EMISOR, Instant.now().plusSeconds(600))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unTokenCaducadoDevuelve401() throws Exception {
        mvc.perform(get("/api/cuenta").header("Authorization",
                        "Bearer " + firmar(clave, TokenService.EMISOR, Instant.now().minusSeconds(600))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unTokenDeOtroEmisorDevuelve401() throws Exception {
        mvc.perform(get("/api/cuenta").header("Authorization",
                        "Bearer " + firmar(clave, "otra-aplicacion", Instant.now().plusSeconds(600))))
                .andExpect(status().isUnauthorized());
    }

    /** Token de admin (uid 7) hecho a mano: con la clave, el emisor y la caducidad que se le digan. */
    private static String firmar(SecretKey clave, String emisor, Instant caduca) {
        JwtClaimsSet datos = JwtClaimsSet.builder()
                .issuer(emisor)
                .issuedAt(caduca.minusSeconds(3600))
                .expiresAt(caduca)
                .subject(ADMIN)
                .claim("uid", 7)
                .claim("rol", "ADMIN")
                .build();
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave))
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), datos))
                .getTokenValue();
    }
}
