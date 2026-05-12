package com.futureprograms.clients.controller.myikea;

import com.futureprograms.clients.dto.myikea.CustomerDto;
import com.futureprograms.clients.entity.myikea.Customer;
import com.futureprograms.clients.service.myikea.CustomerService;
import com.futureprograms.clients.util.ApiResponseBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para gestión de customers de MyIkea
 * Solo accesible por usuarios con rol ADMIN o PREMIUM (solo lectura para PREMIUM)
 */
@RestController
@RequestMapping("/api/myikea/customer")
@Slf4j
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Obtiene el total de customers registrados (requiere ADMIN o PREMIUM)
     */
    @GetMapping("/stats/total")
    @PreAuthorize("hasAnyRole('ADMIN', 'PREMIUM')")
    public ResponseEntity<?> getTotalCustomers() {
        try {
            long total = customerService.getTotalCustomers();
            log.info("✅ Total de customers: {}", total);
            return ApiResponseBuilder.success("Total de customers obtenido", Map.of("total", total));
        } catch (Exception e) {
            log.error("❌ Error al obtener total: {}", e.getMessage());
            return ApiResponseBuilder.internalServerError("Error al obtener total: " + e.getMessage());
        }
    }

    /**
     * Obtiene un customer por su email (requiere ADMIN o PREMIUM)
     */
    @GetMapping("/by-email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PREMIUM')")
    public ResponseEntity<?> getCustomerByEmail(@PathVariable String email) {
        try {
            log.info("🔍 Obteniendo customer con email: {}", email);
            
            Customer customer = customerService.getCustomerByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Customer no encontrado con email: " + email));

            CustomerDto customerDto = CustomerDto.fromEntity(customer);
            log.info("✅ Customer obtenido: {}", email);
            
            return ApiResponseBuilder.success("Customer obtenido exitosamente", Map.of("customer", customerDto));
        } catch (RuntimeException e) {
            log.error("❌ Error al obtener customer: {}", e.getMessage());
            return ApiResponseBuilder.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error interno al obtener customer: {}", e.getMessage());
            return ApiResponseBuilder.internalServerError("Error al obtener customer: " + e.getMessage());
        }
    }

    /**
     * Busca customers por nombre (requiere ADMIN o PREMIUM)
     */
    @GetMapping("/search/firstName/{firstName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PREMIUM')")
    public ResponseEntity<?> searchByFirstName(
            @PathVariable String firstName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            log.info("🔍 Buscando customers por nombre: {} (Página: {}, Tamaño: {})", firstName, page, size);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Customer> customersPage = customerService.searchByFirstName(firstName, pageable);
            
            List<CustomerDto> customerDtos = customersPage.getContent().stream()
                    .map(CustomerDto::fromEntity)
                    .collect(Collectors.toList());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("customers", customerDtos);
            responseData.put("pagination", Map.of(
                    "currentPage", customersPage.getNumber(),
                    "totalItems", customersPage.getTotalElements(),
                    "totalPages", customersPage.getTotalPages(),
                    "pageSize", customersPage.getSize(),
                    "hasNext", customersPage.hasNext(),
                    "hasPrevious", customersPage.hasPrevious()
            ));

            log.info("✅ {} customers encontrados", customerDtos.size());
            return ApiResponseBuilder.success("Búsqueda completada exitosamente", responseData);
        } catch (Exception e) {
            log.error("❌ Error al buscar customers: {}", e.getMessage());
            return ApiResponseBuilder.internalServerError("Error al buscar customers: " + e.getMessage());
        }
    }

    /**
     * Busca customers por apellido (requiere ADMIN o PREMIUM)
     */
    @GetMapping("/search/lastName/{lastName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PREMIUM')")
    public ResponseEntity<?> searchByLastName(
            @PathVariable String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            log.info("🔍 Buscando customers por apellido: {} (Página: {}, Tamaño: {})", lastName, page, size);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Customer> customersPage = customerService.searchByLastName(lastName, pageable);
            
            List<CustomerDto> customerDtos = customersPage.getContent().stream()
                    .map(CustomerDto::fromEntity)
                    .collect(Collectors.toList());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("customers", customerDtos);
            responseData.put("pagination", Map.of(
                    "currentPage", customersPage.getNumber(),
                    "totalItems", customersPage.getTotalElements(),
                    "totalPages", customersPage.getTotalPages(),
                    "pageSize", customersPage.getSize(),
                    "hasNext", customersPage.hasNext(),
                    "hasPrevious", customersPage.hasPrevious()
            ));

            log.info("✅ {} customers encontrados", customerDtos.size());
            return ApiResponseBuilder.success("Búsqueda completada exitosamente", responseData);
        } catch (Exception e) {
            log.error("❌ Error al buscar customers: {}", e.getMessage());
            return ApiResponseBuilder.internalServerError("Error al buscar customers: " + e.getMessage());
        }
    }

    /**
     * Obtiene la lista de todos los customers (requiere ADMIN o PREMIUM)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PREMIUM')")
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            log.info("📋 Obteniendo lista de customers con paginación - Página: {}, Tamaño: {}", page, size);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Customer> customersPage = customerService.getAllCustomersPaged(pageable);

            List<CustomerDto> customerDtos = customersPage.getContent()
                    .stream()
                    .map(CustomerDto::fromEntity)
                    .collect(Collectors.toList());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("customers", customerDtos);
            responseData.put("pagination", Map.of(
                    "currentPage", customersPage.getNumber(),
                    "totalItems", customersPage.getTotalElements(),
                    "totalPages", customersPage.getTotalPages(),
                    "pageSize", customersPage.getSize(),
                    "hasNext", customersPage.hasNext(),
                    "hasPrevious", customersPage.hasPrevious()
            ));

            log.info("✅ {} customers obtenidos", customerDtos.size());
            return ApiResponseBuilder.success("Lista de customers obtenida exitosamente", responseData);
        } catch (Exception e) {
            log.error("❌ Error al obtener lista de customers: {}", e.getMessage());
            return ApiResponseBuilder.internalServerError("Error al obtener lista de customers: " + e.getMessage());
        }
    }

    /**
     * Obtiene un customer específico por su ID (requiere ADMIN o PREMIUM)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PREMIUM')")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        try {
            log.info("🔍 Obteniendo customer con ID: {}", id);
            
            Customer customer = customerService.getCustomerById(id)
                    .orElseThrow(() -> new RuntimeException("Customer no encontrado con ID: " + id));

            CustomerDto customerDto = CustomerDto.fromEntity(customer);
            log.info("✅ Customer obtenido: {}", customer.getEmail());
            
            return ApiResponseBuilder.success("Customer obtenido exitosamente", Map.of("customer", customerDto));
        } catch (RuntimeException e) {
            log.error("❌ Error al obtener customer: {}", e.getMessage());
            return ApiResponseBuilder.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error interno al obtener customer: {}", e.getMessage());
            return ApiResponseBuilder.internalServerError("Error al obtener customer: " + e.getMessage());
        }
    }

    /**
     * Elimina un customer por su ID (requiere ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        try {
            log.info("🗑️ Eliminando customer con ID: {}", id);
            customerService.deleteCustomer(id);
            log.info("✅ Customer eliminado correctamente");
            return ApiResponseBuilder.success("Customer eliminado exitosamente");
        } catch (RuntimeException e) {
            log.error("❌ Error al eliminar customer: {}", e.getMessage());
            return ApiResponseBuilder.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error interno al eliminar customer: {}", e.getMessage());
            return ApiResponseBuilder.internalServerError("Error al eliminar customer: " + e.getMessage());
        }
    }

    /**
     * Actualiza un customer (requiere ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody CustomerDto customerDto) {
        try {
            log.info("📝 Actualizando customer con ID: {}", id);
            
            Customer customer = customerService.getCustomerById(id)
                    .orElseThrow(() -> new RuntimeException("Customer no encontrado con ID: " + id));
            
            // Actualizar campos
            if (customerDto.getFirstName() != null) customer.setFirstName(customerDto.getFirstName());
            if (customerDto.getLastName() != null) customer.setLastName(customerDto.getLastName());
            if (customerDto.getEmail() != null) customer.setEmail(customerDto.getEmail());
            if (customerDto.getTelefono() != null) customer.setTelefono(customerDto.getTelefono());
            if (customerDto.getFechaDeNacimiento() != null) customer.setFechaDeNacimiento(customerDto.getFechaDeNacimiento());
            
            Customer updatedCustomer = customerService.updateCustomer(customer);
            log.info("✅ Customer actualizado correctamente: {}", updatedCustomer.getEmail());
            
            return ApiResponseBuilder.success("Customer actualizado exitosamente", Map.of("customer", CustomerDto.fromEntity(updatedCustomer)));
        } catch (RuntimeException e) {
            log.error("❌ Error al actualizar customer: {}", e.getMessage());
            return ApiResponseBuilder.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error interno al actualizar customer: {}", e.getMessage());
            return ApiResponseBuilder.internalServerError("Error al actualizar customer: " + e.getMessage());
        }
    }
}
