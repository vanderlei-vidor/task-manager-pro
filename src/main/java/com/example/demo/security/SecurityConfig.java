package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
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
                .cors(cors -> {
                })
                .csrf(csrf -> csrf
                        // ❌ DESABILITA CSRF SÓ PARA /api/** (endpoints REST)
                        // ✅ MANTÉM CSRF PARA FORMULÁRIOS Thymeleaf (segurança!)
                        .ignoringRequestMatchers("/api/**", "/auth/**"))
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()) // Permite H2 Console
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
                // ✅ ADICIONA FILTRO JWT
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}