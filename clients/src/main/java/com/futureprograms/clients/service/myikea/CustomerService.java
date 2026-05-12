package com.futureprograms.clients.service.myikea;

import com.futureprograms.clients.entity.myikea.Customer;
import com.futureprograms.clients.repository.myikea.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar operaciones de Customer (MyIkea)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Obtiene todos los customers con paginación
     */
    @Cacheable(value = "customers", key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Customer> getAllCustomersPaged(Pageable pageable) {
        try {
            return customerRepository.findAll(pageable);
        } catch (Exception e) {
            log.error("Error al obtener todos los customers (paginado): {}", e.getMessage());
            return Page.empty();
        }
    }

    /**
     * Obtiene un customer por su ID
     */
    @Cacheable(value = "customers", key = "#customerId")
    public Optional<Customer> getCustomerById(Long customerId) {
        try {
            return customerRepository.findById(customerId);
        } catch (Exception e) {
            log.error("Error al obtener customer por ID {}: {}", customerId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Obtiene un customer por su email
     */
    @Cacheable(value = "customers", key = "'email_' + #email")
    public Optional<Customer> getCustomerByEmail(String email) {
        try {
            return customerRepository.findByEmail(email);
        } catch (Exception e) {
            log.error("Error al obtener customer por email {}: {}", email, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Obtiene todos los customers
     */
    public List<Customer> getAllCustomers() {
        try {
            return customerRepository.findAll();
        } catch (Exception e) {
            log.error("Error al obtener todos los customers: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene customers por nombre (búsqueda parcial paginada)
     */
    public Page<Customer> searchByFirstName(String firstName, Pageable pageable) {
        try {
            return customerRepository.findByFirstNameContainingIgnoreCase(firstName, pageable);
        } catch (Exception e) {
            log.error("Error al buscar customers por nombre {}: {}", firstName, e.getMessage());
            return Page.empty();
        }
    }

    /**
     * Obtiene customers por apellido (búsqueda parcial paginada)
     */
    public Page<Customer> searchByLastName(String lastName, Pageable pageable) {
        try {
            return customerRepository.findByLastNameContainingIgnoreCase(lastName, pageable);
        } catch (Exception e) {
            log.error("Error al buscar customers por apellido {}: {}", lastName, e.getMessage());
            return Page.empty();
        }
    }

    /**
     * Verifica si existe un customer con el email
     */
    public boolean customerExistsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    /**
     * Elimina un customer por su ID
     */
    @Transactional
    @CacheEvict(value = "customers", allEntries = true)
    public void deleteCustomer(Long customerId) {
        try {
            if (!customerRepository.existsById(customerId)) {
                throw new RuntimeException("Customer no encontrado con ID: " + customerId);
            }
            customerRepository.deleteById(customerId);
            log.info("✅ Customer eliminado: ID {}", customerId);
        } catch (RuntimeException e) {
            log.error("❌ Error al eliminar customer: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Error interno al eliminar customer: {}", e.getMessage());
            throw new RuntimeException("Error al eliminar customer: " + e.getMessage());
        }
    }

    /**
     * Actualiza un customer
     */
    @Transactional
    @CacheEvict(value = "customers", allEntries = true)
    public Customer updateCustomer(Customer customer) {
        try {
            if (!customerRepository.existsById(customer.getCustomerId())) {
                throw new RuntimeException("Customer no encontrado con ID: " + customer.getCustomerId());
            }
            return customerRepository.save(customer);
        } catch (RuntimeException e) {
            log.error("❌ Error al actualizar customer: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Error interno al actualizar customer: {}", e.getMessage());
            throw new RuntimeException("Error al actualizar customer: " + e.getMessage());
        }
    }

    /**
     * Obtiene el total de customers registrados
     */
    public long getTotalCustomers() {
        return customerRepository.count();
    }
}
