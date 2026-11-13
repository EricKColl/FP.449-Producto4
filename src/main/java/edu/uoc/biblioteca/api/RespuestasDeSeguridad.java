package edu.uoc.biblioteca.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Respuestas de la API cuando falta el token (401) o no da permiso (403). Se escriben en el mismo formato JSON
 * que el resto de errores de la API (Problem Details, RFC 9457) y con la cabecera estándar WWW-Authenticate.
 * Ocurren antes de llegar a un controlador, por eso no las puede atender ApiErrores.
 */
@Component
public class RespuestasDeSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final BearerTokenAuthenticationEntryPoint sinToken = new BearerTokenAuthenticationEntryPoint();
    private final BearerTokenAccessDeniedHandler sinPermiso = new BearerTokenAccessDeniedHandler();
    private final JsonMapper json;

    public RespuestasDeSeguridad(JsonMapper json) {
        this.json = json;
    }

    @Override
    public void commence(HttpServletRequest peticion, HttpServletResponse respuesta, AuthenticationException error)
            throws IOException {
        sinToken.commence(peticion, respuesta, error);
        String detalle = error instanceof InvalidBearerTokenException
                ? "El token no es válido o ha caducado. Pide uno nuevo en POST /api/auth/token."
                : "Esta ruta necesita un token. Pídelo en POST /api/auth/token y envíalo en la cabecera "
                + "«Authorization: Bearer <token>».";
        escribir(peticion, respuesta, HttpStatus.UNAUTHORIZED, detalle);
    }

    @Override
    public void handle(HttpServletRequest peticion, HttpServletResponse respuesta, AccessDeniedException error)
            throws IOException {
        sinPermiso.handle(peticion, respuesta, error);
        escribir(peticion, respuesta, HttpStatus.FORBIDDEN, "Tu cuenta no tiene permiso para usar esta ruta.");
    }

    private void escribir(HttpServletRequest peticion, HttpServletResponse respuesta, HttpStatus estado, String detalle)
            throws IOException {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        // Mismos campos y en el mismo orden que los ProblemDetail de ApiErrores
        cuerpo.put("detail", detalle);
        cuerpo.put("instance", peticion.getRequestURI());
        cuerpo.put("status", estado.value());
        cuerpo.put("title", estado.getReasonPhrase());
        respuesta.setStatus(estado.value());
        respuesta.setCharacterEncoding("UTF-8");
        respuesta.setContentType("application/problem+json");
        json.writeValue(respuesta.getOutputStream(), cuerpo);
    }
}
