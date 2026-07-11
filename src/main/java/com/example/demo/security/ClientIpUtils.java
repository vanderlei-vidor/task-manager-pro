package com.example.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilitário para extrair o IP real do cliente.
 *
 * Considera headers de proxies reversos comuns (X-Forwarded-For, X-Real-IP,
 * Proxy-Client-IP, etc.). O primeiro IP do X-Forwarded-For é o cliente original.
 *
 * Compartilhado entre RateLimitingFilter e AuditService para consistência.
 */
@Slf4j
public final class ClientIpUtils {

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_CLIENT_IP"
    };

    private ClientIpUtils() {
        // utilitário — não instanciável
    }

    /**
     * Extrai o IP do cliente da requisição.
     *
     * @return IP do cliente ou "unknown" se não for possível determinar.
     */
    public static String getClientIP(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For pode trazer lista "client, proxy1, proxy2"
                // — o primeiro é o cliente original.
                int commaIndex = ip.indexOf(',');
                if (commaIndex > 0) {
                    ip = ip.substring(0, commaIndex);
                }
                return ip.trim();
            }
        }

        return request.getRemoteAddr();
    }
}
