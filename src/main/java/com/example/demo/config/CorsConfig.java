package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuração REAL de CORS.
 *
 * Antes existia apenas um lambda vazio {@code .cors(cors -> {})} no SecurityConfig,
 * e as propriedades {@code application.cors.*} ficavam órfãs (nunca eram lidas).
 * Além disso, o AuthController usava {@code @CrossOrigin(origins = "*")}, abrindo
 * exceção perigosa na rota mais sensível.
 *
 * Este bean concentra TODA a configuração de CORS lendo de ApplicationProperties,
 * de forma que mudar origins/methods/headers exige apenas editar application.properties.
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final ApplicationProperties properties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        ApplicationProperties.Cors corsProps = properties.getCors();

        CorsConfiguration config = new CorsConfiguration();

        // Origins confiáveis (NUNCA usar "*" quando allowCredentials = true)
        config.setAllowedOrigins(corsProps.getAllowedOrigins());

        // Métodos HTTP permitidos
        config.setAllowedMethods(List.of(corsProps.getAllowedMethods()));

        // Headers permitidos na requisição
        config.setAllowedHeaders(List.of(corsProps.getAllowedHeaders()));

        // Headers expostos ao cliente (ex.: Content-Disposition para downloads)
        config.setExposedHeaders(List.of(corsProps.getExposedHeaders()));

        // Permite envio de cookies/credentials
        config.setAllowCredentials(corsProps.getAllowCredentials());

        // Cache do preflight (OPTIONS) em segundos
        if (corsProps.getMaxAge() != null) {
            config.setMaxAge(corsProps.getMaxAge());
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
