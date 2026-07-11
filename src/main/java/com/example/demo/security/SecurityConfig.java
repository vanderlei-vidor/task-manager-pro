package com.example.demo.security;

import com.example.demo.config.ApplicationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final RateLimitingFilter rateLimitingFilter;
        private final CorsConfigurationSource corsConfigurationSource;
        private final ApplicationProperties properties;
        private final Environment environment;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                              RateLimitingFilter rateLimitingFilter,
                              CorsConfigurationSource corsConfigurationSource,
                              ApplicationProperties properties,
                              Environment environment) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.rateLimitingFilter = rateLimitingFilter;
                this.corsConfigurationSource = corsConfigurationSource;
                this.properties = properties;
                this.environment = environment;
        }

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
                return (web) -> web.ignoring().requestMatchers("/favicon.ico");
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf
                        // ❌ DESABILITA CSRF SÓ PARA /api/** (endpoints REST)
                        // ✅ MANTÉM CSRF PARA FORMULÁRIOS Thymeleaf (segurança!)
                        .ignoringRequestMatchers("/api/**", "/auth/**"))
                .headers(headers -> headers
                        // ✅ Content-Security-Policy (previne XSS, clickjacking, data exfil)
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                properties.getSecurity().getHeaders().getContentSecurityPolicy()))
                        // ✅ HSTS: força HTTPS por 1 ano, inclui subdomínios
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        // ✅ X-Content-Type-Options: nosniff (previne MIME sniffing)
                        .contentTypeOptions(contentType -> {})
                        // ✅ X-Frame-Options: em produção DENY (clickjacking).
                        //    Em dev (perfil dev) permite sameOrigin para o H2 Console.
                        .frameOptions(frame -> {
                                if (isDevProfile()) {
                                        frame.sameOrigin();
                                } else {
                                        frame.deny();
                                }
                        })
                        // ✅ Referrer-Policy
                        .referrerPolicy(referrer -> referrer.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        // ✅ X-XSS-Protection (legado, mas ainda útil em browsers antigos)
                        .xssProtection(xss -> xss.headerValue(
                                XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                )
                .authorizeHttpRequests(auth -> auth
                        // =========================================================================
                        // 🚀 CORRIGIDO: PERMITE DESPACHOS DE ERRO INTERNOS (Usa o pacote do Jakarta)
                        // =========================================================================
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                        
                        // ✅ PÁGINAS PÚBLICAS E INFRAESTRUTURA
                        .requestMatchers("/").authenticated() // Dashboard requer login
                        .requestMatchers("/login", "/cadastrar", "/error").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        // ✅ ACTUATOR: health liberado para k8s/LB; prometheus também
                        //    (em prod, restringir prometheus a ROLE_MONITOR se necessário)
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()
                        .requestMatchers("/actuator/info").permitAll()

                        // ✅ API REST (usa JWT)
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/**").authenticated()

                        // ✅ Qualquer outra rota requer autenticação
                        .anyRequest().authenticated())
                
                // =========================================================================
                // 🚀 TRATAMENTO DE EXCEÇÃO PARA REST
                // =========================================================================
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                (request, response, authException) -> {
                                    if (response.getStatus() < 400) {
                                        response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                                    }
                                },
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/auth/**")
                        )
                )

                // ✅ CONFIGURA FORM LOGIN (para Thymeleaf)
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll())
                // ✅ CONFIGURA LOGOUT
                .logout(logout -> logout
                        .logoutUrl("/logout") 
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                // ✅ MANTÉM STATELESS PARA API (JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                // ✅ ADICIONA FILTRO DE RATE LIMITING (antes de tudo, nos endpoints de auth)
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                // ✅ ADICIONA FILTRO JWT
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Verifica se o perfil "dev" está ativo.
     * Usado para relaxar X-Frame-Options (H2 Console) apenas em desenvolvimento.
     */
    private boolean isDevProfile() {
        return java.util.Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }

    /**
     * ✅ Previne auto-registro duplicado do RateLimitingFilter.
     *
     * Como RateLimitingFilter é @Component, o Spring Boot o registra
     * automaticamente no servlet container (rodaria ANTES do Spring Security).
     * Como nós o registramos explicitamente via addFilterBefore na chain do
     * Spring Security, desabilitamos o auto-registro para evitar execução dupla.
     */
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitFilterRegistration(
            RateLimitingFilter rateLimitingFilter) {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>(rateLimitingFilter);
        registration.setEnabled(false);
        return registration;
    }
}