package edu.uoc.biblioteca.api;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;

/**
 * Seguridad de la API REST (/api/**). Es una cadena aparte de la web: no hay sesión ni cookies, así que tampoco
 * hace falta CSRF. Cada petición protegida trae su token JWT en la cabecera «Authorization: Bearer ...».
 * Va antes que la cadena de la web (@Order(1)) y solo atiende las rutas que empiezan por /api/.
 */
@Configuration
public class ApiSecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain filtroDeLaApi(HttpSecurity http, RespuestasDeSeguridad respuestas) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        // Pedir un token tiene que poder hacerse sin token
                        .requestMatchers(HttpMethod.POST, "/api/auth/token").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").hasRole("USER")
                        // El resto de la API pide un token válido de cualquier rol
                        .anyRequest().authenticated())
                .oauth2ResourceServer(servidor -> servidor
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(rolesDelToken()))
                        .authenticationEntryPoint(respuestas)
                        .accessDeniedHandler(respuestas))
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(respuestas)
                        .accessDeniedHandler(respuestas));
        return http.build();
    }

    /** El rol va en el campo «rol» del token (ADMIN o USER) y se convierte en ROLE_ADMIN o ROLE_USER. */
    static JwtAuthenticationConverter rolesDelToken() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(TokenService.CAMPO_ROL);
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(roles);
        return conversor;
    }

    /**
     * Clave con la que se firman y comprueban los tokens (HMAC con SHA-256). Sale de la variable
     * BIBLIOTECA_API_CLAVE; si no se da, se inventa una al arrancar y los tokens dejan de valer al reiniciar.
     */
    @Bean
    SecretKey claveDeLosTokens(@Value("${biblioteca.api.clave:}") String clave) {
        byte[] bytes;
        if (clave.isBlank()) {
            bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
        } else {
            bytes = clave.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                throw new IllegalStateException("biblioteca.api.clave debe tener al menos 32 caracteres");
            }
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder codificadorDeTokens(SecretKey clave) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave));
    }

    /** Solo acepta tokens firmados con nuestra clave, emitidos por esta aplicación y sin caducar. */
    @Bean
    JwtDecoder lectorDeTokens(SecretKey clave) {
        NimbusJwtDecoder lector = NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
        lector.setJwtValidator(JwtValidators.createDefaultWithIssuer(TokenService.EMISOR));
        return lector;
    }

    /**
     * CORS: una página web de otro dominio puede llamar a la API desde el navegador. Es seguro porque la API
     * no usa cookies: sin el token no se llega a nada protegido. Solo afecta a /api/**, no a la web.
     * Spring Security lo busca por este nombre exacto: corsConfigurationSource.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of("*"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setExposedHeaders(List.of("Location"));
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource origenes = new UrlBasedCorsConfigurationSource();
        origenes.registerCorsConfiguration("/api/**", cors);
        return origenes;
    }
}
