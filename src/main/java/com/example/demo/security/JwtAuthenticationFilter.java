package com.example.demo.security;

import com.example.demo.service.JwtUtilService;
import com.example.demo.service.AutenticacaoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtilService jwtUtilService;
    private final AutenticacaoService autenticacaoService;

    public JwtAuthenticationFilter(JwtUtilService jwtUtilService, 
                                   AutenticacaoService autenticacaoService) {
        this.jwtUtilService = jwtUtilService;
        this.autenticacaoService = autenticacaoService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        // Verifica se tem token e começa com "Bearer "
        if (StringUtils.hasText(authorizationHeader) && 
            authorizationHeader.startsWith(BEARER_PREFIX)) {
            
            String jwtToken = authorizationHeader.substring(BEARER_PREFIX.length());

            try {
                // 1. Extrai o email do token
                String email = jwtUtilService.getClaims(jwtToken).getSubject();

                // 2. Verifica se ainda não está autenticado
                if (email != null && 
                    SecurityContextHolder.getContext().getAuthentication() == null) {
                    
                    // 3. ✅ BUSCA O USUÁRIO NO BANCO (CRÍTICO!)
                    UserDetails userDetails = autenticacaoService.loadUserByUsername(email);

                    // 4. ✅ VALIDA O TOKEN COM OS DADOS DO USUÁRIO
                    if (jwtUtilService.isTokenValid(jwtToken, userDetails)) {
                        
                        // 5. ✅ CRIA AUTENTICAÇÃO COM AS AUTHORITIES CORRETAS!
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails,           // ✅ UserDetails completo
                                null,                  // credenciais (não precisa)
                                userDetails.getAuthorities()  // ✅ ROLES AQUI!
                            );

                        authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        // 6. Seta no contexto de segurança
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        log.debug("Usuário {} autenticado com sucesso via JWT", email);
                    }
                }

            } catch (Exception e) {
                log.warn("Token JWT inválido: {}", e.getMessage());
                // Não faz nada - mantém o contexto limpo
            }
        }

        // Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    }
}