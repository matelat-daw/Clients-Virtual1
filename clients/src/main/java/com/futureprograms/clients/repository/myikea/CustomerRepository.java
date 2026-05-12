package com.futureprograms.clients.repository.myikea;

import com.futureprograms.clients.entity.myikea.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio para la entidad Customer (MyIkea)
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Busca un customer por email
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Verifica si existe un customer con el email especificado
     */
    boolean existsByEmail(String email);

    /**
     * Busca customers por nombre
     */
    org.springframework.data.domain.Page<Customer> findByFirstNameContainingIgnoreCase(String firstName, org.springframework.data.domain.Pageable pageable);

    /**
     * Busca customers por apellido
     */
    org.springframework.data.domain.Page<Customer> findByLastNameContainingIgnoreCase(String lastName, org.springframework.data.domain.Pageable pageable);

    /**
     * Busca customers por nombre o apellido
     */
    org.springframework.data.domain.Page<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName, org.springframework.data.domain.Pageable pageable);

    /**
     * Cuenta el total de customers
     */
    long count();
}
