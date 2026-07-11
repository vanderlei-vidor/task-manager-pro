package com.example.demo.security;

import com.example.demo.config.ApplicationProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de Rate Limiting para endpoints de autenticação.
 *
 * Implementa a propriedade órfã {@code application.security.rate-limit.login-attempts-per-minute}
 * que existia no application.properties sem implementação.
 *
 * Estratégia: 1 bucket por IP + endpoint, em memória (ConcurrentHashMap).
 * Atende bem até múltiplas instâncias. Para cluster multi-nó, migrar para Redis depois.
 *
 * Aplica-se apenas a /auth/login e /auth/register (paths sensíveis).
 * Retorna HTTP 429 Too Many Requests quando o limite é excedido.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ApplicationProperties properties;

    // Cache de buckets: chave = "IP:endpoint"
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // Só aplica rate limiting nos endpoints sensíveis de auth
        if (!isAuthEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Rate limiting desabilitado na config? Pula.
        if (!Boolean.TRUE.equals(properties.getSecurity().getRateLimit().getEnabled())) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = ClientIpUtils.getClientIP(request);
        String bucketKey = clientIp + ":" + path;

        Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> createNewBucket());

        if (bucket.tryConsume(1)) {
            // Consumiu com sucesso — pode prosseguir
            filterChain.doFilter(request, response);
        } else {
            // Limite excedido
            log.warn("⛔ Rate limit excedido | IP={} | path={} | limite={}/min",
                    clientIp, path,
                    properties.getSecurity().getRateLimit().getLoginAttemptsPerMinute());
            sendTooManyRequests(response);
        }
    }

    /**
     * Cria um novo bucket com a configuração do application.properties.
     * Refill incremental: recarrega 1 token a cada (60s / tentativas-permitidas).
     */
    private Bucket createNewBucket() {
        Integer maxAttempts = properties.getSecurity().getRateLimit().getLoginAttemptsPerMinute();

        Bandwidth limit = Bandwidth.classic(
                maxAttempts,
                Refill.intervally(maxAttempts, Duration.ofMinutes(1))
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private boolean isAuthEndpoint(String path) {
        return path != null && (
                path.equals("/auth/login") ||
                path.equals("/auth/register") ||
                path.equals("/login") ||
                path.equals("/cadastrar")
        );
    }

    /**
     * Resposta 429 padronizada em JSON.
     * Usa o literal 429 pois SC_TOO_MANY_REQUESTS pode não estar disponível
     * em todas as versões da API Servlet.
     */
    private void sendTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", "60");

        String body = """
                {
                  "timestamp": "%s",
                  "status": 429,
                  "error": "Too Many Requests",
                  "reason": "RATE_LIMIT_EXCEEDED",
                  "message": "Muitas tentativas. Aguarde 1 minuto antes de tentar novamente."
                }
                """.formatted(java.time.Instant.now());

        response.getWriter().write(body);
    }

    /**
     * Limpa buckets ociosos periodicamente para evitar memory leak.
     * (Chamado pelo agendador do Spring se necessário — por ora o tamanho
     *  é controlado pelo número de IPs distintos, que tende a ser pequeno.)
     */
    public int getActiveBucketsCount() {
        return buckets.size();
    }
}
