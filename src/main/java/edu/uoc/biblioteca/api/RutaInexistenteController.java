package edu.uoc.biblioteca.api;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Cualquier ruta /api/... que no exista responde 404 en JSON, como el resto de la API, y no con la página de
 * error de la web. Spring elige siempre la ruta más concreta, así que esta solo atiende lo que no atiende nadie.
 */
@Hidden
@RestController
public class RutaInexistenteController {

    @RequestMapping("/api/**")
    void noExiste(HttpServletRequest peticion) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No existe la ruta " + peticion.getMethod() + " " + peticion.getRequestURI());
    }
}
