package edu.uoc.biblioteca.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Convierte los errores de los controladores de la API en JSON estándar (Problem Details, RFC 9457):
 * {"type", "title", "status", "detail", "instance"} y, si falla la validación, "errores" con cada campo.
 * Solo atiende a los controladores del paquete api: la web sigue mostrando sus páginas de error.
 */
@RestControllerAdvice(basePackageClasses = ApiErrores.class)
public class ApiErrores {

    /** 404 de los servicios («No existe el libro 99»), 401 y 429 del token, etc. */
    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail conEstado(ResponseStatusException error) {
        return ProblemDetail.forStatusAndDetail(error.getStatusCode(), error.getReason());
    }

    /** Una regla del negocio lo impide: el libro tiene préstamos, ya lo tienes reservado... */
    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail conflicto(IllegalStateException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, error.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail datoNoValido(IllegalArgumentException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, error.getMessage());
    }

    /** @Valid en un @RequestBody: un mensaje por cada campo que no cumple sus reglas. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail camposNoValidos(MethodArgumentNotValidException error) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError campo : error.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(campo.getField(), campo.getDefaultMessage());
        }
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Hay datos que no son válidos: revisa «errores»");
        problema.setProperty("errores", errores);
        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail jsonNoValido() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido");
    }

    /** /api/libros/abc cuando se espera un número. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail parametroNoValido(MethodArgumentTypeMismatchException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El valor «" + error.getValue() + "» no vale para el parámetro " + error.getName());
    }
}
