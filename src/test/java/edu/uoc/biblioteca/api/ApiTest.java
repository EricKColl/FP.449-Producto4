package edu.uoc.biblioteca.api;

import edu.uoc.biblioteca.repository.UsuarioRepository;
import edu.uoc.biblioteca.security.CuentaUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base de los tests de la API: aplicación completa sobre H2 con los datos de 02-datos.sql, y cada test deshace
 * sus cambios al terminar. token(ADMIN) añade a la petición un token real de esa cuenta, por ejemplo:
 * mvc.perform(get("/api/cuenta").with(token(LAURA))).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
public abstract class ApiTest {

    /** Cuenta ADMIN (id 7). */
    protected static final String ADMIN = "admin@example.com";
    /** Socia con préstamos, una reserva y dos favoritos (id 1). */
    protected static final String LAURA = "laura.martinez@example.com";
    /** Otro socio, para comprobar que nadie ve lo de otro (id 2). */
    protected static final String JAVIER = "javier.lopez@example.com";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    private TokenService tokens;

    @Autowired
    private UsuarioRepository usuarios;

    /** Cabecera «Authorization: Bearer ...» con un token firmado de la cuenta de ese email. */
    protected RequestPostProcessor token(String email) {
        String token = tokens.emitir(new CuentaUsuario(usuarios.findByEmail(email).orElseThrow())).token();
        return peticion -> {
            peticion.addHeader("Authorization", "Bearer " + token);
            return peticion;
        };
    }
}
