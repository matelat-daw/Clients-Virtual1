package com.futureprograms.clients.controller;

import com.futureprograms.clients.config.JwtProvider;
import com.futureprograms.clients.dto.RegisterRequest;
import com.futureprograms.clients.dto.UserDto;
import com.futureprograms.clients.entity.User;
import com.futureprograms.clients.enums.Role;
import com.futureprograms.clients.repository.UserRepository;
import com.futureprograms.clients.service.ImageService;
import com.futureprograms.clients.service.UserService;
import com.futureprograms.clients.util.ApiConstants;
import com.futureprograms.clients.util.ValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controlador optimizado para gestión de usuarios
 * Maneja registro, consultas y operaciones administrativas
 */
@RestController
@RequestMapping(ApiConstants.USER_ENDPOINT)
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final ImageService imageService;

    /**
     * POST /api/user/register - Registro de usuario con imagen opcional
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam String nick,
            @RequestParam String name,
            @RequestParam String surname1,
            @RequestParam(required = false) String surname2,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String gender,
            @RequestParam(required = false) String bday,
            @RequestParam(required = false) MultipartFile profilePicture
    ) {
        log.info("Intento de registro para: {} ({})", email, nick);
        
        try {
            LocalDate birthDate = null;
            if (bday != null && !bday.isEmpty()) {
                try {
                    // Intentar ISO primero (YYYY-MM-DD)
                    birthDate = LocalDate.parse(bday, DateTimeFormatter.ISO_DATE);
                } catch (Exception e) {
                    try {
                        // Intentar formato común (DD/MM/YYYY)
                        birthDate = LocalDate.parse(bday, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception e2) {
                        log.warn("No se pudo parsear fecha de nacimiento: {}", bday);
                        throw new IllegalArgumentException("Formato de fecha inválido. Use YYYY-MM-DD o DD/MM/YYYY");
                    }
                }
            }

            RegisterRequest request = RegisterRequest.builder()
                    .nick(nick)
                    .name(name)
                    .surname1(surname1)
                    .surname2(surname2)
                    .email(email)
                    .phone(phone)
                    .password(password)
                    .gender(gender)
                    .bday(birthDate)
                    .build();

            User user = userService.registerUser(request);

            if (profilePicture != null && !profilePicture.isEmpty()) {
                try {
                    ValidationHelper.validateImageFile(profilePicture);
                    imageService.ensureUserImageDirectory(user.getId());
                    String fileName = imageService.saveProfileImage(profilePicture, user.getId());
                    user = userService.updateProfileImage(user.getId(), fileName);
                } catch (Exception e) {
                    log.warn("No se pudo guardar imagen de perfil post-registro para {}: {}", user.getEmail(), e.getMessage());
                }
            }

            String token = jwtProvider.generateToken(user.getEmail());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", ApiConstants.MSG_REGISTER_SUCCESS,
                    "token", token,
                    "data", UserDto.fromEntity(user)
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error de validación en registro: {}", e.getMessage());
            throw e; // GlobalExceptionHandler lo manejará como 400
        } catch (Exception e) {
            log.error("Error inesperado en registro de {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Error en el proceso de registro: " + e.getMessage());
        }
    }

    /**
     * GET /api/user - Lista usuarios (ADMIN y PREMIUM, excluye usuario logueado)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PREMIUM')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // Obtener usuario logueado
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("📌 [getAllUsers] Usuario logueado: {}", currentUserEmail);
        log.info("📌 [getAllUsers] Parámetros: page={}, size={}", page, size);
        
        try {
            // Intentar usar el custom query
            Pageable pageable = PageRequest.of(page, size);
            log.info("🔍 [getAllUsers] Ejecutando custom query findAllExcludingUser");
            
            Page<User> usersPage = userRepository.findAllExcludingUser(currentUserEmail, pageable);
            
            log.info("✅ [getAllUsers] Query exitosa. Usuarios encontrados: {}, Total: {}", 
                    usersPage.getContent().size(), usersPage.getTotalElements());
            
            List<UserDto> users = usersPage.getContent().stream()
                    .peek(u -> log.debug("👤 Usuario: id={}, nick={}, email={}, role={}", 
                            u.getId(), u.getNick(), u.getEmail(), u.getRole()))
                    .map(UserDto::fromEntity)
                    .toList();
            
            log.info("📤 [getAllUsers] Retornando {} usuarios formateados", users.size());
            
            return ResponseEntity.ok()
                    .body(Map.of(
                            "success", true,
                            "message", ApiConstants.MSG_USERS_FETCHED,
                            "users", users,
                            "pagination", Map.of(
                                    "currentPage", usersPage.getNumber(),
                                    "totalItems", usersPage.getTotalElements(),
                                    "totalPages", usersPage.getTotalPages(),
                                    "pageSize", size,
                                    "hasNext", usersPage.hasNext(),
                                    "hasPrevious", usersPage.hasPrevious()
                            )
                    ));
        } catch (Exception e) {
            // Fallback: obtener todos los usuarios, filtrar y paginar manualmente
            log.warn("⚠️ [getAllUsers] Custom query failed: {}, usando fallback", e.getMessage());
            log.debug("❌ Stack trace:", e);
            
            List<User> allUsers = userRepository.findAll();
            log.info("📊 [getAllUsers] Fallback: Total de usuarios en BD: {}", allUsers.size());
            
            // Filtrar el usuario logueado
            List<UserDto> filteredUsers = allUsers.stream()
                    .filter(user -> !user.getEmail().equals(currentUserEmail))
                    .peek(u -> log.debug("✅ Usuario incluido: id={}, nick={}, email={}", u.getId(), u.getNick(), u.getEmail()))
                    .map(UserDto::fromEntity)
                    .toList();
            
            log.info("📈 [getAllUsers] Fallback: Usuarios después de filterar: {}", filteredUsers.size());
            
            // Calcular paginación manualmente
            int totalItems = filteredUsers.size();
            int totalPages = (totalItems > 0) ? (int) Math.ceil((double) totalItems / size) : 0;
            int startIdx = page * size;
            int endIdx = Math.min(startIdx + size, totalItems);
            
            // Validar índices
            if (startIdx >= totalItems && totalItems > 0) {
                startIdx = Math.max(0, totalItems - size);
                endIdx = totalItems;
            }
            
            List<UserDto> paginatedUsers = (startIdx < totalItems) 
                    ? filteredUsers.subList(startIdx, endIdx)
                    : new ArrayList<>();
            
            log.info("📄 [getAllUsers] Fallback: Usuarios en página actual: {}", paginatedUsers.size());
            
            return ResponseEntity.ok()
                    .body(Map.of(
                            "success", true,
                            "message", ApiConstants.MSG_USERS_FETCHED,
                            "users", paginatedUsers,
                            "pagination", Map.of(
                                    "currentPage", page,
                                    "totalItems", totalItems,
                                    "totalPages", totalPages,
                                    "pageSize", size,
                                    "hasNext", page < (totalPages - 1),
                                    "hasPrevious", page > 0
                            )
                    ));
        }
    }

    /**
     * GET /api/user/{id} - Obtener usuario por ID (ADMIN y PREMIUM)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PREMIUM')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        String requesterEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("👤 [getUserById] Usuario {} solicita detalles de usuario ID: {}", requesterEmail, id);
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> {
                    log.error("❌ [getUserById] Usuario ID {} NO encontrado", id);
                    return new IllegalStateException(ApiConstants.ERR_USER_NOT_FOUND);
                });

        log.info("✅ [getUserById] Usuario encontrado: ID={}, Nick={}, Email={}, Role={}", 
                user.getId(), user.getNick(), user.getEmail(), user.getRole());

        UserDto userDto = UserDto.fromEntity(user);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", ApiConstants.MSG_PROFILE_FETCHED,
                "data", userDto
        ));
    }

    /**
     * PUT /api/user/{id}/role - Cambiar rol de usuario (ADMIN only)
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeUserRole(
            @PathVariable Long id,
            @RequestParam String newRole
    ) {
        User updatedUser = userService.changeUserRole(id, newRole.toUpperCase());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Rol actualizado exitosamente",
                "data", UserDto.fromEntity(updatedUser)
        ));
    }

    /**
     * DELETE /api/user/{id} - Eliminar usuario (ADMIN only, no permite eliminar ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("🗑️ [deleteUser] Admin {} solicitando eliminar usuario ID: {}", adminEmail, id);
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> {
                    log.error("❌ [deleteUser] Usuario ID {} no encontrado", id);
                    return new IllegalStateException(ApiConstants.ERR_USER_NOT_FOUND);
                });

        if (user.getRole() == Role.ADMIN) {
            log.error("⛔ [deleteUser] Intento de eliminar usuario ADMIN: {} por admin: {}", user.getEmail(), adminEmail);
            throw new IllegalStateException("No se puede eliminar usuarios con rol ADMIN");
        }

        log.info("📌 [deleteUser] Eliminando usuario: {} (ID: {})", user.getEmail(), id);
        userService.deleteUser(id);
        
        log.info("✅ [deleteUser] Usuario eliminado exitosamente: {} (ID: {}) por admin: {}", user.getEmail(), id, adminEmail);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Usuario eliminado exitosamente"
        ));
    }
}