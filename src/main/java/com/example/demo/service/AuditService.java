package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Serviço de auditoria de eventos de segurança.
 *
 * Implementa (parcialmente) a propriedade órfã {@code application.audit.audit-login-attempts}
 * que existia no application.properties sem implementação.
 *
 * Por ora, emite logs estruturados (parseáveis por ELK/Loki/Datadog).
 * Deixa o gancho pronto para evoluir a uma tabela {@code audit_log} persistente.
 *
 * ⚠️ NUNCA logar senha, token, ou dado sensível — apenas email/ação/IP.
 */
@Slf4j
@Service
public class AuditService {

    private static final String AUDIT_PREFIX = "AUDIT";

    /**
     * Registra uma tentativa de login (sucesso ou falha).
     *
     * @param email   email usado na tentativa (pode ser inexistente — é importante auditar)
     * @param sucesso true se autenticou, false caso contrário
     * @param ip      IP do cliente
     */
    public void registrarLogin(String email, boolean sucesso, String ip) {
        // Log estruturado para fácil parsing por SIEM/observabilidade
        log.info("{} | LOGIN | email={} | sucesso={} | ip={} | ts={}",
                AUDIT_PREFIX,
                mask(email),
                sucesso ? "SIM" : "NAO",
                ip,
                Instant.now());
    }

    /**
     * Registra um logout.
     */
    public void registrarLogout(String email, String ip) {
        log.info("{} | LOGOUT | email={} | ip={} | ts={}",
                AUDIT_PREFIX, mask(email), ip, Instant.now());
    }

    /**
     * Registra revogação de tokens (ex.: refresh suspeito).
     */
    public void registrarRevogacaoTokens(String email, String motivo) {
        log.info("{} | TOKEN_REVOKED | email={} | motivo={} | ts={}",
                AUDIT_PREFIX, mask(email), motivo, Instant.now());
    }

    /**
     * Mascara o email para GDPR/LGPD em logs: "vanderlei@x.com" → "v***@x.com".
     * Mantém o domínio (útil para correlação) e oculta o nome do usuário.
     */
    private String mask(String email) {
        if (email == null || email.isBlank()) {
            return "unknown";
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***" + (at == 1 ? email.substring(1) : "");
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
