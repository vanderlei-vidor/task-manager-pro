package com.example.demo.service;

import com.example.demo.exception.InvalidTokenException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.Usuario;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UsuarioRepository;

import com.example.demo.service.RefreshTokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do RefreshTokenService
 * 
 * @author Task Manager Pro
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("🔐 RefreshTokenService - Testes de Segurança")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private Usuario usuarioTeste;
    private static final String EMAIL_TESTE = "teste@example.com";
    private static final long DURACAO_TOKEN_MS = 604800000L; // 7 dias

    @BeforeEach
    void setUp() {
        // Configura usuário de teste
        usuarioTeste = Usuario.builder()
                .id(1L)
                .email(EMAIL_TESTE)
                .nome("Usuário Teste")
                .senha("senha-criptografada")
                .build();

        // 🔥 Jeito Sênior e Nativo do Spring para injetar @Value em testes Mockito
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", DURACAO_TOKEN_MS);
    }

    // ========================================
    // TESTES: createRefreshToken
    // ========================================
    @Nested
    @DisplayName("📝 Método: createRefreshToken")
    class CreateRefreshTokenTests {

        @Test
        @DisplayName("✅ Deve criar refresh token válido para usuário existente")
        void deveCriarRefreshTokenValidoParaUsuarioExistente() {
            // Arrange
            when(usuarioRepository.findByEmail(EMAIL_TESTE))
                    .thenReturn(Optional.of(usuarioTeste));
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> {
                        RefreshToken token = invocation.getArgument(0);
                        token.setId(100L);
                        return token;
                    });

            // Act
            RefreshToken resultado = refreshTokenService.createRefreshToken(EMAIL_TESTE);

            // Assert
            assertThat(resultado)
                    .isNotNull()
                    .extracting(
                            RefreshToken::getUsuario,
                            RefreshToken::isRevoked,
                            RefreshToken::getToken)
                    .containsExactly(usuarioTeste, false, resultado.getToken());

            assertThat(resultado.getToken())
                    .isNotBlank()
                    .hasSize(36); // UUID format

            assertThat(resultado.getExpiryDate())
                    .isAfter(Instant.now());

            verify(usuarioRepository, times(1)).findByEmail(EMAIL_TESTE);
            verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("❌ Deve lançar NotFoundException quando usuário não existe")
        void deveLancarNotFoundExceptionQuandoUsuarioNaoExiste() {
            // Arrange
            when(usuarioRepository.findByEmail(EMAIL_TESTE))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> refreshTokenService.createRefreshToken(EMAIL_TESTE))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Usuário não encontrado");

            verify(usuarioRepository, times(1)).findByEmail(EMAIL_TESTE);
            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("⏰ Deve configurar data de expiração corretamente")
        void deveConfigurarDataExpiracaoCorretamente() {
            // Arrange
            Instant antes = Instant.now();
            when(usuarioRepository.findByEmail(EMAIL_TESTE))
                    .thenReturn(Optional.of(usuarioTeste));
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            RefreshToken resultado = refreshTokenService.createRefreshToken(EMAIL_TESTE);
            Instant depois = Instant.now();

            // Assert
            Instant expiracaoEsperadaMin = antes.plusMillis(DURACAO_TOKEN_MS);
            Instant expiracaoEsperadaMax = depois.plusMillis(DURACAO_TOKEN_MS);

            assertThat(resultado.getExpiryDate())
                    .isBetween(expiracaoEsperadaMin, expiracaoEsperadaMax);
        }
    }

    // ========================================
    // TESTES: verifyExpiration
    // ========================================
    @Nested
    @DisplayName("🔍 Método: verifyExpiration")
    class VerifyExpirationTests {

        @Test
        @DisplayName("✅ Deve retornar token quando válido e não revogado")
        void deveRetornarTokenQuandoValidoENaoRevogado() {
            // Arrange
            RefreshToken tokenValido = RefreshToken.builder()
                    .id(1L)
                    .token(UUID.randomUUID().toString())
                    .usuario(usuarioTeste)
                    .expiryDate(Instant.now().plusSeconds(3600)) // 1 hora no futuro
                    .revoked(false)
                    .build();

            // Act
            RefreshToken resultado = refreshTokenService.verifyExpiration(tokenValido);

            // Assert
            assertThat(resultado)
                    .isNotNull()
                    .isEqualTo(tokenValido);

            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("⏰ Deve lançar InvalidTokenException quando token expirado")
        void deveLancarInvalidTokenExceptionQuandoTokenExpirado() {
            // Arrange
            RefreshToken tokenExpirado = RefreshToken.builder()
                    .id(2L)
                    .token("token-expirado")
                    .usuario(usuarioTeste)
                    .expiryDate(Instant.now().minusSeconds(3600)) // 1 hora no passado
                    .revoked(false)
                    .build();

            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act & Assert
            assertThatThrownBy(() -> refreshTokenService.verifyExpiration(tokenExpirado))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("expirado");

            // Verifica se token foi marcado como revogado
            assertThat(tokenExpirado.isRevoked()).isTrue();
            verify(refreshTokenRepository, times(1)).save(tokenExpirado);
        }

        @Test
        @DisplayName("🚫 Deve lançar InvalidTokenException quando token revogado")
        void deveLancarInvalidTokenExceptionQuandoTokenRevogado() {
            // Arrange
            RefreshToken tokenRevogado = RefreshToken.builder()
                    .id(3L)
                    .token("token-revogado")
                    .usuario(usuarioTeste)
                    .expiryDate(Instant.now().plusSeconds(3600))
                    .revoked(true) // Já está revogado
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> refreshTokenService.verifyExpiration(tokenRevogado))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("revogado");

            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("🔒 Deve revogar token expirado antes de lançar exceção")
        void deveRevogarTokenExpiradoAntesDeLancarExcecao() {
            // Arrange
            RefreshToken tokenExpirado = RefreshToken.builder()
                    .id(4L)
                    .token("token-expirado-2")
                    .usuario(usuarioTeste)
                    .expiryDate(Instant.now().minusSeconds(1))
                    .revoked(false)
                    .build();

            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act & Assert
            assertThatThrownBy(() -> refreshTokenService.verifyExpiration(tokenExpirado))
                    .isInstanceOf(InvalidTokenException.class);

            assertThat(tokenExpirado.isRevoked()).isTrue();
        }
    }

    // ========================================
    // TESTES: findByToken
    // ========================================
    @Nested
    @DisplayName("🔎 Método: findByToken")
    class FindByTokenTests {

        @Test
        @DisplayName("✅ Deve retornar token quando encontrado")
        void deveRetornarTokenQuandoEncontrado() {
            // Arrange
            String tokenString = "token-encontrado";
            RefreshToken token = RefreshToken.builder()
                    .id(5L)
                    .token(tokenString)
                    .usuario(usuarioTeste)
                    .build();

            when(refreshTokenRepository.findByToken(tokenString))
                    .thenReturn(Optional.of(token));

            // Act
            RefreshToken resultado = refreshTokenService.findByToken(tokenString);

            // Assert
            assertThat(resultado).isEqualTo(token);
            verify(refreshTokenRepository, times(1)).findByToken(tokenString);
        }

        @Test
        @DisplayName("❌ Deve lançar NotFoundException quando token não existe")
        void deveLancarNotFoundExceptionQuandoTokenNaoExiste() {
            // Arrange
            String tokenInexistente = "token-inexistente";
            when(refreshTokenRepository.findByToken(tokenInexistente))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> refreshTokenService.findByToken(tokenInexistente))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    // ========================================
    // TESTES: rotateToken
    // ========================================
    @Nested
    @DisplayName("🔄 Método: rotateToken")
    class RotateTokenTests {

        @Test
        @DisplayName("✅ Deve revogar token antigo e criar novo")
        void deveRevogarTokenAntigoECriarNovo() {
            // Arrange
            RefreshToken tokenAntigo = RefreshToken.builder()
                    .id(6L)
                    .token("token-antigo")
                    .usuario(usuarioTeste)
                    .expiryDate(Instant.now().plusSeconds(3600))
                    .revoked(false)
                    .build();

            when(usuarioRepository.findById(1L))
                    .thenReturn(Optional.of(usuarioTeste));
            when(usuarioRepository.findByEmail(EMAIL_TESTE))
                    .thenReturn(Optional.of(usuarioTeste));
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                    .thenAnswer(invocation -> {
                        RefreshToken token = invocation.getArgument(0);
                        if (token.getId() == null)
                            token.setId(101L);
                        return token;
                    });

            // Act
            RefreshToken novoToken = refreshTokenService.rotateToken(tokenAntigo);

            // Assert
            assertThat(tokenAntigo.isRevoked()).isTrue();
            assertThat(novoToken)
                    .isNotNull()
                    .isNotEqualTo(tokenAntigo);
            assertThat(novoToken.getToken()).isNotBlank();

            verify(refreshTokenRepository, atLeastOnce()).save(any());
            verify(usuarioRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("🔐 Deve buscar usuário novamente para evitar LazyInitializationException")
        void deveBuscarUsuarioNovamenteParaEvitarLazyException() {
            // Arrange
            RefreshToken tokenAntigo = RefreshToken.builder()
                    .id(7L)
                    .token("token-antigo-2")
                    .usuario(usuarioTeste)
                    .revoked(false)
                    .expiryDate(Instant.now().plusSeconds(3600))
                    .build();

            when(usuarioRepository.findById(1L))
                    .thenReturn(Optional.of(usuarioTeste));
            when(usuarioRepository.findByEmail(EMAIL_TESTE))
                    .thenReturn(Optional.of(usuarioTeste));
            when(refreshTokenRepository.save(any()))
                    .thenAnswer(inv -> {
                        RefreshToken t = inv.getArgument(0);
                        if (t.getId() == null)
                            t.setId(102L);
                        return t;
                    });

            // Act
            refreshTokenService.rotateToken(tokenAntigo);

            // Assert - Verifica que findById foi chamado (evita proxy lazy)
            verify(usuarioRepository, times(1)).findById(1L);
        }
    }

    // ========================================
    // TESTES: revokeAllUserTokens
    // ========================================
    @Nested
    @DisplayName("🚫 Método: revokeAllUserTokens")
    class RevokeAllUserTokensTests {

        @Test
        @DisplayName("✅ Deve revogar todos os tokens do usuário")
        void deveRevogarTodosOsTokensDoUsuario() {
            // Arrange
            when(usuarioRepository.findByEmail(EMAIL_TESTE))
                    .thenReturn(Optional.of(usuarioTeste));

            // Act
            refreshTokenService.revokeAllUserTokens(EMAIL_TESTE);

            // Assert
            verify(usuarioRepository, times(1)).findByEmail(EMAIL_TESTE);
            verify(refreshTokenRepository, times(1)).revokeAllUserTokens(usuarioTeste);
        }

        @Test
        @DisplayName("❌ Deve lançar NotFoundException se usuário não existe")
        void deveLancarNotFoundExceptionSeUsuarioNaoExiste() {
            // Arrange
            when(usuarioRepository.findByEmail(EMAIL_TESTE))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> refreshTokenService.revokeAllUserTokens(EMAIL_TESTE))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Usuário não encontrado");

            verify(refreshTokenRepository, never()).revokeAllUserTokens(any());
        }
    }

    // ========================================
    // TESTES: revokeToken
    // ========================================
    @Nested
    @DisplayName("🔒 Método: revokeToken")
    class RevokeTokenTests {

        @Test
        @DisplayName("✅ Deve revogar token específico")
        void deveRevogarTokenEspecifico() {
            // Arrange
            String tokenString = "token-especifico";

            // Act
            refreshTokenService.revokeToken(tokenString);

            // Assert
            verify(refreshTokenRepository, times(1)).revokeByToken(tokenString);
        }
    }

    // ========================================
    // TESTES: cleanupExpiredTokens
    // ========================================
    @Nested
    @DisplayName("🧹 Método: cleanupExpiredTokens")
    class CleanupExpiredTokensTests {

        @Test
        @DisplayName("✅ Deve deletar tokens expirados do banco")
        void deveDeletarTokensExpiradosDoBanco() {
            // Arrange
            when(refreshTokenRepository.deleteExpiredTokens(any(Instant.class)))
                    .thenReturn(5L);

            // Act
            refreshTokenService.cleanupExpiredTokens();

            // Assert
            verify(refreshTokenRepository, times(1))
                    .deleteExpiredTokens(any(Instant.class));
        }

        @Test
        @DisplayName("📊 Deve logar quantidade de tokens deletados")
        void deveLogarQuantidadeDeTokensDeletados() {
            // Arrange
            when(refreshTokenRepository.deleteExpiredTokens(any()))
                    .thenReturn(10L);

            // Act
            refreshTokenService.cleanupExpiredTokens();

            // Assert - Verifica que o método foi chamado
            verify(refreshTokenRepository, times(1))
                    .deleteExpiredTokens(any(Instant.class));
        }
    }

    // ========================================
    // TESTES: countActiveTokens
    // ========================================
    @Nested
    @DisplayName("🔢 Método: countActiveTokens")
    class CountActiveTokensTests {

        @Test
        @DisplayName("✅ Deve retornar quantidade de tokens ativos")
        void deveRetornarQuantidadeDeTokensAtivos() {
            // Arrange
            when(usuarioRepository.findByEmail(EMAIL_TESTE))
                    .thenReturn(Optional.of(usuarioTeste));
            when(refreshTokenRepository.countByUsuarioAndRevokedFalse(usuarioTeste))
                    .thenReturn(3L);

            // Act
            long quantidade = refreshTokenService.countActiveTokens(EMAIL_TESTE);

            // Assert
            assertThat(quantidade).isEqualTo(3L);
            verify(refreshTokenRepository, times(1))
                    .countByUsuarioAndRevokedFalse(usuarioTeste);
        }

        @Test
        @DisplayName("✅ Deve retornar zero quando não há tokens ativos")
        void deveRetornarZeroQuandoNaoHaTokensAtivos() {
            // Arrange
            when(usuarioRepository.findByEmail(EMAIL_TESTE))
                    .thenReturn(Optional.of(usuarioTeste));
            when(refreshTokenRepository.countByUsuarioAndRevokedFalse(usuarioTeste))
                    .thenReturn(0L);

            // Act
            long quantidade = refreshTokenService.countActiveTokens(EMAIL_TESTE);

            // Assert
            assertThat(quantidade).isZero();
        }

        @Test
        @DisplayName("❌ Deve lançar NotFoundException se usuário não existe")
        void deveLancarNotFoundExceptionSeUsuarioNaoExiste() {
            // Arrange
            when(usuarioRepository.findByEmail(EMAIL_TESTE))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> refreshTokenService.countActiveTokens(EMAIL_TESTE))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Usuário não encontrado");
        }
    }
}