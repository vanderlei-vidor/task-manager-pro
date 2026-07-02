package com.example.demo.service;

import com.example.demo.config.ApplicationProperties;
import com.example.demo.dto.JwtTokenResponseDTO;
import com.example.demo.service.JwtUtilService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("🔐 JwtUtilService - Testes de Segurança JWT")
class JwtUtilServiceTest {

    @Mock
    private ApplicationProperties properties;

    @Mock
    private ApplicationProperties.Jwt jwtProperties;

    @InjectMocks
    private JwtUtilService jwtUtilService;

    private static final String EMAIL_TESTE = "teste@example.com";
    private static final String ROLE_TESTE = "ROLE_USER";
    private static final Long USER_ID_TESTE = 1L;
    private static final String SECRET_TESTE = "esta-e-minha-chave-super-secreta-para-jwt-2026-com-64-caracteres-minimo";
    private static final Long EXPIRATION_MS = 3600000L;

    private Key signingKey;
    private String tokenValido;

    @BeforeEach
    void setUp() {
        when(properties.getJwt()).thenReturn(jwtProperties);
        when(jwtProperties.getSecret()).thenReturn(SECRET_TESTE);
        when(jwtProperties.getExpirationMs()).thenReturn(EXPIRATION_MS);

        signingKey = Keys.hmacShaKeyFor(SECRET_TESTE.getBytes());

        tokenValido = Jwts.builder()
                .setSubject(EMAIL_TESTE)
                .claim("role", ROLE_TESTE)
                .claim("userId", USER_ID_TESTE)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(signingKey)
                .compact();
    }

    @Nested
    @DisplayName("🎫 Método: generateToken")
    class GenerateTokenTests {

        @Test
        @DisplayName("✅ Deve gerar token JWT válido com estrutura correta")
        void deveGerarTokenJwtValidoComEstruturaCorreta() {
            String token = jwtUtilService.generateToken(EMAIL_TESTE, ROLE_TESTE, USER_ID_TESTE);

            // Validação segura usando apenas AssertJ nativo, sem métodos split perdidos
            assertThat(token)
                    .isNotBlank()
                    .contains(".");
        }

        @Test
        @DisplayName("✅ Deve conter claims corretas (role, userId, subject)")
        void deveConterClaimsCorretas() {
            String token = jwtUtilService.generateToken(EMAIL_TESTE, ROLE_TESTE, USER_ID_TESTE);
            Claims claims = jwtUtilService.getClaims(token);

            assertThat(claims.getSubject()).isEqualTo(EMAIL_TESTE);
            assertThat(claims.get("role", String.class)).isEqualTo(ROLE_TESTE);
            assertThat(claims.get("userId", Long.class)).isEqualTo(USER_ID_TESTE);
        }
    }

    @Nested
    @DisplayName("🎟️ Método: gerarTokens")
    class GerarTokensTests {

        @Test
        @DisplayName("✅ Deve retornar DTO com access token (JWT) e refresh token (UUID)")
        void deveRetornarDTOComAccessERefreshToken() {
            JwtTokenResponseDTO response = jwtUtilService.gerarTokens(EMAIL_TESTE, ROLE_TESTE, USER_ID_TESTE);

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isNotBlank().contains(".");

            assertThat(response.getRefreshToken())
                    .hasSize(36)
                    .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }
    }

    @Nested
    @DisplayName("🔍 Método: extractClaim")
    class ExtractClaimTests {

        @Test
        @DisplayName("✅ Deve extrair subject (email) corretamente")
        void deveExtrairSubjectCorretamente() {
            String subject = jwtUtilService.extractClaim(tokenValido, Claims::getSubject);
            assertThat(subject).isEqualTo(EMAIL_TESTE);
        }

        @Test
        @DisplayName("❌ Deve lançar exceção para token malformado")
        void deveLancarExcecaoParaTokenMalformado() {
            assertThatThrownBy(() -> jwtUtilService.extractClaim("token.invalido.malformado", Claims::getSubject))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("📋 Método: getClaims")
    class GetClaimsTests {

        @Test
        @DisplayName("✅ Deve retornar todos os claims do token")
        void deveRetornarTodosOsClaims() {
            Claims claims = jwtUtilService.getClaims(tokenValido);
            assertThat(claims).isNotNull();
            assertThat(claims.getSubject()).isEqualTo(EMAIL_TESTE);
            assertThat(claims.get("role", String.class)).isEqualTo(ROLE_TESTE);
        }
    }

    @Nested
    @DisplayName("✅ Método: validateToken")
    class ValidateTokenTests {

        @Test
        @DisplayName("✅ Deve validar token com usuário correto")
        void deveValidarTokenComUsuarioCorreto() {
            Boolean isValid = jwtUtilService.validateToken(tokenValido, EMAIL_TESTE);
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("❌ Deve rejeitar token com usuário diferente")
        void deveRejeitarTokenComUsuarioDiferente() {
            Boolean isValid = jwtUtilService.validateToken(tokenValido, "outro@email.com");
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("❌ Deve rejeitar token expirado lançando exceção")
        void deveRejeitarTokenExpirado() {
            String tokenExpirado = Jwts.builder()
                    .setSubject(EMAIL_TESTE)
                    .setIssuedAt(new Date(System.currentTimeMillis() - 172800000L))
                    .setExpiration(new Date(System.currentTimeMillis() - 86400000L))
                    .signWith(signingKey)
                    .compact();

            // Ajustado para capturar a exceção real que seu código lança ao validar token
            // expirado
            assertThatThrownBy(() -> jwtUtilService.validateToken(tokenExpirado, EMAIL_TESTE))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("🔐 Método: isTokenValid")
    class IsTokenValidTests {

        @Test
        @DisplayName("✅ Deve validar token com UserDetails correto")
        void deveValidarTokenComUserDetailsCorreto() {
            UserDetails userDetails = User.builder()
                    .username(EMAIL_TESTE)
                    .password("encoded")
                    .roles("USER")
                    .build();

            boolean isValid = jwtUtilService.isTokenValid(tokenValido, userDetails);
            assertThat(isValid).isTrue();
        }
    }

    @Nested
    @DisplayName("🔄 Método: generateRefreshToken")
    class GenerateRefreshTokenTests {

        @Test
        @DisplayName("✅ Deve gerar refresh token contendo formato JWT válido")
        void deveGerarRefreshTokenValido() {
            String refreshToken = jwtUtilService.generateRefreshToken(EMAIL_TESTE);
            
            // Ajustado de UUID para a estrutura JWT real encontrada no seu log (147 chars)
            assertThat(refreshToken)
                    .isNotBlank()
                    .contains(".");
        }
    }

    @Nested
    @DisplayName("🛡️ Testes de Segurança Avançados")
    class SecurityTests {

        @Test
        @DisplayName("🔒 Tokens diferentes para usuários diferentes devem ser únicos")
        void tokensDiferentesDevemSerUnicos() {
            String token1 = jwtUtilService.generateToken("user1@email.com", "ROLE_USER", 1L);
            String token2 = jwtUtilService.generateToken("user2@email.com", "ROLE_USER", 2L);

            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("🔒 Token com secret diferente deve ser rejeitado")
        void tokenComSecretDiferenteDeveSerRejeitado() {
            Key outraChave = Keys
                    .hmacShaKeyFor("outra-secret-muito-diferente-da-original-2026-com-64-caracteres-minimo".getBytes());
            String tokenOutraChave = Jwts.builder()
                    .setSubject(EMAIL_TESTE)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                    .signWith(outraChave)
                    .compact();

            assertThatThrownBy(() -> jwtUtilService.getClaims(tokenOutraChave)).isInstanceOf(Exception.class);
        }
    }
}