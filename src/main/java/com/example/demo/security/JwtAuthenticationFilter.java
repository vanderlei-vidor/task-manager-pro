package com.example.demo.security;

import com.example.demo.service.JwtUtilService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtilService jwtUtilService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getServletPath();
        
        // ✅ PULAR ROTAS PÚBLICAS (não validar token)
        if (requestPath.startsWith("/auth/") || 
            requestPath.equals("/login") || 
            requestPath.equals("/cadastrar") ||
            requestPath.startsWith("/css/") ||
            requestPath.startsWith("/js/") ||
            requestPath.startsWith("/images/") ||
            requestPath.equals("/favicon.ico") ||
            requestPath.equals("/error")) {
            
            log.debug("🔓 Rota pública detectada: {} - pulando validação JWT", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Extrair token do header
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("🔓 Sem token no header - continuando sem autenticação");
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            // ✅ Validar token
            final String userEmail = jwtUtilService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtUtilService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );

                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("✅ Usuário autenticado via JWT: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ Token inválido ou expirado: {}", e.getMessage());
            // ✅ NÃO lançar exceção! Apenas não autenticar
            // O Spring Security vai decidir se a rota exige autenticação
        }

        filterChain.doFilter(request, response);
    }
}