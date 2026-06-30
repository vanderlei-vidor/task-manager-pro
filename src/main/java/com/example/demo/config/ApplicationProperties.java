package com.example.demo.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configurações centralizadas da aplicação
 * Lê propriedades do application.properties com prefixo "application"
 */

@ConfigurationProperties(prefix = "application")
@Data
public class ApplicationProperties {

    // ========================================
    // JWT CONFIGURATION
    // ========================================
    private Jwt jwt = new Jwt();

    @Data
    public static class Jwt {
        /**
         * Chave secreta para assinar tokens JWT
         * ⚠️ NUNCA commite essa chave no Git!
         */
        private String secret;

        /**
         * Duração do Access Token em milissegundos (padrão: 15 minutos)
         */
        private Long expirationMs = 900000L;

        /**
         * Configurações específicas do Refresh Token
         */
        private RefreshToken refreshToken = new RefreshToken();

        @Data
        public static class RefreshToken {
            /**
             * Duração do Refresh Token em milissegundos (padrão: 7 dias)
             */
            private Long expirationMs = 604800000L;

            /**
             * Habilitar rotação automática de tokens
             * Quando true, cada uso do refresh token gera um novo
             */
            private Boolean rotationEnabled = true;

            /**
             * Habilitar revogação em família
             * Se um token for comprometido, revoga todos da mesma família
             */
            private Boolean familyRevocation = false;

            /**
             * Intervalo de cleanup de tokens expirados (em horas)
             */
            private Integer cleanupIntervalHours = 24;
        }
    }

    // ========================================
    // CORS CONFIGURATION
    // ========================================
    private Cors cors = new Cors();

    @Data
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:3000", "http://localhost:8081");
        private String[] allowedMethods = {"GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"};
        private String[] allowedHeaders = {"Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"};
        private String[] exposedHeaders = {"Authorization", "Content-Disposition"};
        private Boolean allowCredentials = true;
        private Long maxAge = 3600L;
    }

    // ========================================
    // SECURITY CONFIGURATION
    // ========================================
    private Security security = new Security();

    @Data
    public static class Security {
        private RateLimit rateLimit = new RateLimit();

        @Data
        public static class RateLimit {
            private Boolean enabled = true;
            private Integer requestsPerMinute = 60;
            private Integer requestsPerHour = 1000;
            private Integer loginAttemptsPerMinute = 5;
            private Integer lockoutMinutes = 15;
        }
        
        private BruteForce bruteForce = new BruteForce();

        @Data
        public static class BruteForce {
            private Boolean enabled = true;
            private Integer maxFailedAttempts = 5;
            private Integer lockoutDurationMinutes = 30;
            private Integer attemptWindowMinutes = 15;
        }
        
        private Headers headers = new Headers();

        @Data
        public static class Headers {
            private String contentSecurityPolicy = "default-src 'self'; script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; img-src 'self' data: https:; font-src 'self' https://cdn.jsdelivr.net;";
            private String strictTransportSecurity = "max-age=31536000; includeSubDomains";
            private String contentTypeOptions = "nosniff";
            private String frameOptions = "DENY";
            private String xssProtection = "1; mode=block";
            private String referrerPolicy = "strict-origin-when-cross-origin";
        }
    }

    // ========================================
    // AUDIT CONFIGURATION
    // ========================================
    private Audit audit = new Audit();

    @Data
    public static class Audit {
        private Boolean enabled = true;
        private Boolean auditLoginAttempts = true;
        private Boolean auditPasswordChanges = true;
        private Boolean auditDeletions = true;
        private Boolean auditPermissionChanges = true;
        private Integer retentionDays = 90;
    }

    // ========================================
    // SMTP CONFIGURATION (Email)
    // ========================================
    private Smtp smtp = new Smtp();

    @Data
    public static class Smtp {
        private String host = "localhost";
        private Integer port = 587;
        private String username;
        private String password;
        private Boolean enableSsl = true;
        private String fromEmail = "noreply@taskmanager.com";
        private String fromName = "Task Manager Pro";
    }

    // ========================================
    // APPLICATION INFO
    // ========================================
    private App app = new App();

    @Data
    public static class App {
        private String name = "Task Manager Pro";
        private String version = "1.0.0";
        private String environment = "dev";
        private String baseUrl = "http://localhost:8081";
        private String timezone = "America/Sao_Paulo";
    }
}