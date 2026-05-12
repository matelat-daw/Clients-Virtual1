package com.futureprograms.clients.repository;

import com.futureprograms.clients.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByNick(String nick);

    Optional<User> findByVerificationToken(String token);

    boolean existsByEmail(String email);

    boolean existsByNick(String nick);

    /**
     * Obtiene todos los usuarios excepto el usuario especificado por email
     * @param email Email del usuario a excluir
     * @param pageable Información de paginación
     * @return Page con usuarios (sin incluir el usuario especificado)
     */
    @Query("SELECT u FROM User u WHERE u.email != :email ORDER BY u.id DESC")
    Page<User> findAllExcludingUser(@Param("email") String email, Pageable pageable);
}