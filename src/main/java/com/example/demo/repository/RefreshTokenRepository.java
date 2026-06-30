
package com.example.demo.repository;


import com.example.demo.model.RefreshToken;
import com.example.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // ✅ IMPORTANTE

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUsuarioAndRevokedFalse(Usuario usuario);

    List<RefreshToken> findByExpiryDateBefore(Instant instant);

    /**
     * Revoga todos os tokens de um usuário (logout de todos os dispositivos)
     */
    @Transactional // ✅ Garante a transação no nível do repositório
    @Modifying(clearAutomatically = true) // ✅ Limpa o cache do Hibernate após o update
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.usuario = :usuario")
    void revokeAllUserTokens(@Param("usuario") Usuario usuario);

    /**
     * Revoga token específico
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.token = :token")
    void revokeByToken(@Param("token") String token);

    /**
     * Deleta tokens expirados (cleanup)
     * Lembra que no Service mudamos para retornar 'long'? 
     * Alterando o retorno para 'long' aqui, você sabe quantas linhas limpou!
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :instant")
    long deleteExpiredTokens(@Param("instant") Instant instant); // ✅ Alterado de void para long

    long countByUsuarioAndRevokedFalse(Usuario usuario);
}