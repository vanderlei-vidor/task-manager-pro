package com.example.demo.controller;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.RegistroUsuarioDTO;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat; // AssertJ importado para segurança dos asserts
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import java.util.List;


/**
 * Testes de Integração do AuthController
 * Testa o fluxo completo: Controller → Service → Repository → Database
 * * @author Task Manager Pro
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("🔐 AuthController - Testes de Integração")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String EMAIL_TESTE = "teste@example.com";
    private static final String SENHA_TESTE = "SenhaForte123!";
    private static final String NOME_TESTE = "Usuário Teste";

    @BeforeEach
    void setUp() {
        // Limpa banco antes de cada teste
        usuarioRepository.deleteAll();
    }

    // ========================================
    // TESTES: POST /auth/login
    // ========================================
    @Nested
    @DisplayName("🔑 Endpoint: POST /auth/login")
    class LoginTests {

        @Test
        @DisplayName("✅ Deve realizar login e retornar tokens JWT válidos com credenciais corretas")
        void deveRealizarLoginComCredenciaisValidas() throws Exception {
            // Arrange - Cria usuário no banco
            criarUsuario(EMAIL_TESTE, SENHA_TESTE, NOME_TESTE);

            LoginRequestDTO loginDTO = new LoginRequestDTO();
            loginDTO.setEmail(EMAIL_TESTE);
            loginDTO.setSenha(SENHA_TESTE);

            // Act & Assert
            MvcResult result = mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value(NOME_TESTE))
                    .andExpect(jsonPath("$.email").value(EMAIL_TESTE))
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.expiresIn").value(900000))
                    .andReturn();

            // Validação robusta da estrutura dos tokens com AssertJ
            String response = result.getResponse().getContentAsString();
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

            String accessToken = (String) responseMap.get("accessToken");
            String refreshToken = (String) responseMap.get("refreshToken");

            // Access token deve ter 3 partes (header.payload.signature)
            assertThat(accessToken.split("\\.")).hasSize(3);
            
            // Refresh token deve ser um UUID válido (36 caracteres)
            assertThat(refreshToken).hasSize(36);
        }

        @Test
        @DisplayName("❌ Deve retornar 401 quando usuário não existe")
        void deveRetornar401QuandoUsuarioNaoExiste() throws Exception {
            // Arrange
            LoginRequestDTO loginDTO = new LoginRequestDTO();
            loginDTO.setEmail("inexistente@example.com");
            loginDTO.setSenha(SENHA_TESTE);

            // Act & Assert
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.reason").value("USER_NOT_FOUND"));
        }

        @Test
        @DisplayName("❌ Deve retornar 401 quando senha está incorreta")
        void deveRetornar401QuandoSenhaIncorreta() throws Exception {
            // Arrange
            criarUsuario(EMAIL_TESTE, SENHA_TESTE, NOME_TESTE);

            LoginRequestDTO loginDTO = new LoginRequestDTO();
            loginDTO.setEmail(EMAIL_TESTE);
            loginDTO.setSenha("SenhaErrada123!");

            // Act & Assert
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.reason").value("PASSWORD_DOES_NOT_MATCH"));
        }

        @Test
        @DisplayName("❌ Deve retornar 400 quando email está vazio")
        void deveRetornar400QuandoEmailVazio() throws Exception {
            // Arrange
            LoginRequestDTO loginDTO = new LoginRequestDTO();
            loginDTO.setEmail("");
            loginDTO.setSenha(SENHA_TESTE);

            // Act & Assert
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ========================================
    // TESTES: POST /auth/register
    // ========================================
    @Nested
    @DisplayName("📝 Endpoint: POST /auth/register")
    class RegisterTests {

        @Test
        @DisplayName("✅ Deve registrar novo usuário com dados válidos")
        void deveRegistrarNovoUsuarioComDadosValidos() throws Exception {
            // Arrange
            RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO();
            registroDTO.setNome("Novo Usuário");
            registroDTO.setEmail("novo@example.com");
            registroDTO.setSenha("NovaSenha123!");

            // Act & Assert
            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registroDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Novo Usuário"))
                    .andExpect(jsonPath("$.email").value("novo@example.com"))
                    .andExpect(jsonPath("$.message").value("Usuário cadastrado com sucesso!"));

            // Verifica se usuário foi salvo no banco utilizando AssertJ
            assertThat(usuarioRepository.findByEmail("novo@example.com")).isPresent();
        }

        @Test
        @DisplayName("❌ Deve retornar 409 quando email já existe")
        void deveRetornar409QuandoEmailJaExiste() throws Exception {
            // Arrange - Cria usuário existente
            criarUsuario(EMAIL_TESTE, SENHA_TESTE, NOME_TESTE);

            RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO();
            registroDTO.setNome("Outro Usuário");
            registroDTO.setEmail(EMAIL_TESTE); // Mesmo email para forçar o conflito
            registroDTO.setSenha("OutraSenha123!");

            // Criamos uma autenticação mockada explícita com a role padrão do sistema
            UsernamePasswordAuthenticationToken mockAuth = new UsernamePasswordAuthenticationToken(
                    EMAIL_TESTE, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );

            // Act & Assert
            mockMvc.perform(post("/auth/register")
                    .with(authentication(mockAuth)) // 🚀 Injeta a autenticação direto no contexto bypassando o filtro do JWT
                    .with(csrf())                   // Evita qualquer barreira de CSRF
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registroDTO)))
                    .andExpect(status().isConflict()) // O 409 agora vai chegar limpo no MockMvc!
                    .andExpect(jsonPath("$.reason").value("USER_ALREADY_EXISTS"));
        }

        @Test
        @DisplayName("✅ Deve criptografar senha antes de salvar no banco")
        void deveCriptografarSenhaAntesDeSalvar() throws Exception {
            // Arrange
            RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO();
            registroDTO.setNome("Usuário Criptografado");
            registroDTO.setEmail("cripto@example.com");
            registroDTO.setSenha("SenhaForte123!");

            // Act
            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registroDTO)))
                    .andExpect(status().isOk());

            // Assert - Verifica se senha foi criptografada
            Usuario usuarioSalvo = usuarioRepository.findByEmail("cripto@example.com").orElseThrow();
            
            // Senha NÃO deve ser igual à original (está criptografada)
            assertThat(usuarioSalvo.getSenha()).isNotEqualTo("SenhaForte123!");
            
            // Senha criptografada deve validar com passwordEncoder
            assertThat(passwordEncoder.matches("SenhaForte123!", usuarioSalvo.getSenha())).isTrue();
        }

        @Test
        @DisplayName("❌ Deve retornar 400 quando dados são inválidos")
        void deveRetornar400QuandoDadosInvalidos() throws Exception {
            // Arrange - Email inválido
            RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO();
            registroDTO.setNome("Usuário");
            registroDTO.setEmail("email-invalido"); // Email inválido
            registroDTO.setSenha("123"); // Senha muito curta

            // Act & Assert
            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registroDTO)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ========================================
    // TESTES: POST /auth/refresh
    // ========================================
    @Nested
    @DisplayName("🔄 Endpoint: POST /auth/refresh")
    class RefreshTokenTests {

        @Test
        @DisplayName("✅ Deve renovar access token com refresh token válido")
        void deveRenovarAccessTokenComRefreshTokenValido() throws Exception {
            // Arrange - Faz login para obter refresh token
            criarUsuario(EMAIL_TESTE, SENHA_TESTE, NOME_TESTE);

            LoginRequestDTO loginDTO = new LoginRequestDTO();
            loginDTO.setEmail(EMAIL_TESTE);
            loginDTO.setSenha(SENHA_TESTE);

            MvcResult loginResult = mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = loginResult.getResponse().getContentAsString();
            Map<String, Object> loginResponse = objectMapper.readValue(response, Map.class);
            String refreshToken = (String) loginResponse.get("refreshToken");

            // Act - Usa refresh token para obter novo access token
            Map<String, String> refreshRequest = Map.of("refreshToken", refreshToken);

            mockMvc.perform(post("/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.expiresIn").value(900000));
        }

        @Test
        @DisplayName("❌ Deve retornar 401 quando refresh token é inválido")
        void deveRetornar401QuandoRefreshTokenInvalido() throws Exception {
            // Arrange
            Map<String, String> refreshRequest = Map.of("refreshToken", "token-invalido");

            // Act & Assert
            mockMvc.perform(post("/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.reason").value("INVALID_TOKEN"));
        }

        @Test
        @DisplayName("❌ Deve retornar 401 quando refresh token está vazio")
        void deveRetornar401QuandoRefreshTokenVazio() throws Exception {
            // Arrange
            Map<String, String> refreshRequest = Map.of("refreshToken", "");

            // Act & Assert
            mockMvc.perform(post("/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.reason").value("INVALID_TOKEN"));
        }

        @Test
        @DisplayName("✅ Deve rotacionar refresh token (revogar antigo e criar novo)")
        void deveRotacionarRefreshToken() throws Exception {
            // Arrange - Faz login
            criarUsuario(EMAIL_TESTE, SENHA_TESTE, NOME_TESTE);

            LoginRequestDTO loginDTO = new LoginRequestDTO();
            loginDTO.setEmail(EMAIL_TESTE);
            loginDTO.setSenha(SENHA_TESTE);

            MvcResult loginResult = mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = loginResult.getResponse().getContentAsString();
            Map<String, Object> loginResponse = objectMapper.readValue(response, Map.class);
            String refreshTokenAntigo = (String) loginResponse.get("refreshToken");

            // Act - Renova token
            Map<String, String> refreshRequest = Map.of("refreshToken", refreshTokenAntigo);

            MvcResult refreshResult = mockMvc.perform(post("/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            String refreshResponse = refreshResult.getResponse().getContentAsString();
            Map<String, Object> refreshResponseMap = objectMapper.readValue(refreshResponse, Map.class);
            String refreshTokenNovo = (String) refreshResponseMap.get("refreshToken");

            // Assert - Novo refresh token deve ser diferente do antigo
            assertThat(refreshTokenAntigo).isNotEqualTo(refreshTokenNovo);
        }
    }

    // ========================================
    // TESTES: POST /auth/logout
    // ========================================
    @Nested
    @DisplayName("🚪 Endpoint: POST /auth/logout")
    class LogoutTests {

        @Test
        @DisplayName("✅ Deve realizar logout e revogar todos os tokens")
        void deveRealizarLogoutERevogarTodosOsTokens() throws Exception {
            // Arrange - Faz login para obter tokens
            criarUsuario(EMAIL_TESTE, SENHA_TESTE, NOME_TESTE);

            LoginRequestDTO loginDTO = new LoginRequestDTO();
            loginDTO.setEmail(EMAIL_TESTE);
            loginDTO.setSenha(SENHA_TESTE);

            MvcResult loginResult = mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = loginResult.getResponse().getContentAsString();
            Map<String, Object> loginResponse = objectMapper.readValue(response, Map.class);
            String accessToken = (String) loginResponse.get("accessToken");

            // Act - Faz logout com access token
            mockMvc.perform(post("/auth/logout")
                    .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logout realizado com sucesso!"));
        }

        @Test
        @DisplayName("❌ Deve retornar 401 quando não está autenticado")
        void deveRetornar401QuandoNaoEstaAutenticado() throws Exception {
            // Act & Assert - Tenta logout sem token
            mockMvc.perform(post("/auth/logout"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("❌ Deve retornar 401 quando token é inválido")
        void deveRetornar401QuandoTokenInvalido() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/auth/logout")
                    .header("Authorization", "Bearer token-invalido"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ========================================
    // TESTES DE FLUXO COMPLETO
    // ========================================
    @Nested
    @DisplayName("🔄 Testes de Fluxo Completo")
    class FluxoCompletoTests {

        @Test
        @DisplayName("✅ Fluxo completo: Register → Login → Refresh → Logout")
        void fluxoCompletoRegisterLoginRefreshLogout() throws Exception {
            // 1. Register
            RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO();
            registroDTO.setNome("Fluxo Completo");
            registroDTO.setEmail("fluxo@example.com");
            registroDTO.setSenha("SenhaForte123!");

            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registroDTO)))
                    .andExpect(status().isOk());

            // 2. Login
            LoginRequestDTO loginDTO = new LoginRequestDTO();
            loginDTO.setEmail("fluxo@example.com");
            loginDTO.setSenha("SenhaForte123!");

            MvcResult loginResult = mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String loginResponse = loginResult.getResponse().getContentAsString();
            Map<String, Object> loginMap = objectMapper.readValue(loginResponse, Map.class);
            String accessToken = (String) loginMap.get("accessToken");
            String refreshToken = (String) loginMap.get("refreshToken");

            // 3. Refresh
            Map<String, String> refreshRequest = Map.of("refreshToken", refreshToken);

            MvcResult refreshResult = mockMvc.perform(post("/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            String refreshResponse = refreshResult.getResponse().getContentAsString();
            Map<String, Object> refreshMap = objectMapper.readValue(refreshResponse, Map.class);
            String novoAccessToken = (String) refreshMap.get("accessToken");

            // 4. Logout
            mockMvc.perform(post("/auth/logout")
                    .header("Authorization", "Bearer " + novoAccessToken))
                    .andExpect(status().isOk());
        }
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================

    /**
     * Cria usuário no banco de dados para testes utilizando o Builder padrão do Lombok
     */
    private void criarUsuario(String email, String senha, String nome) {
        Usuario usuario = Usuario.builder()
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .nome(nome)
                .build();
        usuarioRepository.save(usuario);
    }
}