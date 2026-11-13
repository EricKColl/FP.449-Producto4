package edu.uoc.biblioteca.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** La clave con la que se firman los tokens: la de la configuración o, si no hay, una aleatoria. */
class ClaveDeLosTokensTest {

    private final ApiSecurityConfig config = new ApiSecurityConfig();

    @Test
    void sinClaveConfiguradaSeInventaUnaDistintaCadaVez() {
        assertThat(config.claveDeLosTokens("").getEncoded()).hasSize(32)
                .isNotEqualTo(config.claveDeLosTokens("").getEncoded());
    }

    @Test
    void usaLaClaveConfigurada() {
        String clave = "una-clave-de-prueba-con-mas-de-32-caracteres";
        assertThat(new String(config.claveDeLosTokens(clave).getEncoded())).isEqualTo(clave);
    }

    @Test
    void unaClaveCortaNoSeAcepta() {
        assertThatThrownBy(() -> config.claveDeLosTokens("corta"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 caracteres");
    }
}
