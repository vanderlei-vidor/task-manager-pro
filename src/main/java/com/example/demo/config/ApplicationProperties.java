package com.example.demo.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@ConfigurationProperties(prefix = "application")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationProperties {

    // --- JWT Configuration ---
    private String jwtSecret;
    private Long jwtExpirationMs;

    // --- CORS Configuration ---
    private List<String> allowedOrigins;
    private String[] allowedMethods;
    private String[] allowedHeaders;

    // --- SMTP Configuration (Email) ---
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private Boolean smtpEnableSsl;

    // --- Task Manager Specific ---
    private String managerName;
}