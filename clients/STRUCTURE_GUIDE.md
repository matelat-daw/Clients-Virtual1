# Clients API - Guía de Estructura Optimizada

## 📁 Estructura del Proyecto

```
src/main/java/com/futureprograms/clients/
├── config/                          # Configuración de Spring
│   ├── GlobalExceptionHandler.java  # ✅ Manejo centralizado de errores
│   ├── JwtProvider.java             # ✅ Generador de JWT
│   ├── SecurityConfig.java          # ✅ Configuración de seguridad
│   └── WebConfig.java               # ✅ Configuración web
│
├── controller/                       # REST Controllers (Optimizado)
│   ├── AuthController.java          # ✅ -220 líneas (login, logout, auth)
│   ├── ProfileController.java       # ✅ -220 líneas (perfil usuario)
│   ├── UserController.java          # Registro de usuarios
│   └── ImageController.java         # Servir imágenes
│
├── service/                         # Business Logic
│   ├── UserService.java             # Lógica de usuarios
│   ├── ImageService.java            # Gestión de imágenes
│   └── EmailService.java            # Envío de emails
│
├── entity/                          # JPA Entities
│   ├── User.java                    # Entidad Usuario
│   └── RoleEntity.java              # Entidad Rol
│
├── repository/                      # Data Access (Spring Data JPA)
│   ├── UserRepository.java
│   └── RoleRepository.java
│
├── dto/                             # Data Transfer Objects
│   ├── AuthResponse.java            # ❌ DEPRECADO (usar ApiResponse)
│   ├── LoginRequest.java
│   ├── UserDto.java
│   └── ... otros DTOs
│
├── enums/                           # Enumeraciones
│   ├── Role.java
│   └── Gender.java
│
└── util/                            # 🆕 UTILITIES (Nuevas)
    ├── ApiConstants.java            # 🆕 Constantes centralizadas
    ├── ApiResponse.java             # 🆕 Respuesta unificada
    ├── ApiResponseBuilder.java      # 🆕 Builder de respuestas
    ├── AuthenticationHelper.java    # 🆕 Validación de auth
    └── ValidationHelper.java        # 🆕 Validaciones comunes
```

---

## 🎯 CÓMO USAR LAS NUEVAS UTILITIES

### 1. Trabajar con Respuestas HTTP

```java
// ✅ Correcto (Nuevo)
@GetMapping("/profile")
public ResponseEntity<ApiResponse<UserDto>> getProfile() {
    User user = userService.getUser();
    return ApiResponseBuilder.success(
        ApiConstants.MSG_PROFILE_FETCHED, 
        UserDto.fromEntity(user)
    );
}

// ❌ Antiguo
@GetMapping("/profile")
public ResponseEntity<AuthResponse> getProfile() {
    // ... 50 líneas de boilerplate
}
```

### 2. Validar Entrada

```java
// ✅ Correcto
@PostMapping("/upload-image")
public ResponseEntity<?> upload(@RequestParam MultipartFile file) {
    ValidationHelper.validateImageFile(file);  // Lanza excepción si inválido
    // ... procesar imagen
}

// ❌ Antiguo
if (file == null || file.isEmpty() || !isValidType(file.getContentType())) {
    return ResponseEntity.badRequest().body(...);
}
```

### 3. Obtener Usuario Autenticado

```java
// ✅ Correcto
@PutMapping("/profile")
public ResponseEntity<ApiResponse<UserDto>> update(Authentication auth) {
    User user = authenticationHelper.requireAuthenticatedUser(auth);
    // ... usuario garantizado válido
}

// ❌ Antiguo
if (auth == null || !auth.isAuthenticated()) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(...);
}
String email = auth.getName();
User user = userService.getUserByEmail(email).orElseThrow(...);
```

### 4. Usar Constantes

```java
// ✅ Correcto
return ApiResponseBuilder.success(
    ApiConstants.MSG_LOGIN_SUCCESS,  
    data
);

// ❌ Antiguo
return ApiResponseBuilder.success(
    "Login exitoso",
    data
);
```

---

## 🔄 Migración desde Antiguo Código

### Endpoint Antiguo:
```java
@PostMapping("/register")
public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
    try {
        if (req.getEmail() == null || req.getEmail().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(AuthResponse.error("Email requerido"));
        }
        
        User user = userService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AuthResponse.success("Registro exitoso", null, UserDto.fromEntity(user)));
    } catch (Exception e) {
        log.error("Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(AuthResponse.error(e.getMessage()));
    }
}
```

### Endpoint Refactorizado:
```java
@PostMapping("/register")
public ResponseEntity<ApiResponse<UserDto>> register(
    @RequestBody RegisterRequest req) {
    
    // Validación delegada a UserService o ValidationHelper
    User user = userService.register(req);  // Lanza excepción si hay error
    
    return ApiResponseBuilder.created(
        ApiConstants.MSG_REGISTER_SUCCESS,
        UserDto.fromEntity(user)
    );
}
```

**Cambios:**
- ✅ 0 try-catch (GlobalExceptionHandler lo maneja)
- ✅ 0 validaciones manuales (UserService las hace)
- ✅ Respuesta consistente (ApiResponse)
- ✅ Constantes centralizadas (ApiConstants)
- ✅ -25 líneas por endpoint

---

## 🧪 Testing

### Test Antiguo:
```java
@Test
void testGetProfile() {
    ResponseEntity<AuthResponse> response = controller.getProfile(auth);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Perfil obtenido", response.getBody().getMessage());
}
```

### Test Nuevo (Más simple):
```java
@Test
void testGetProfile() {
    ResponseEntity<ApiResponse<UserDto>> response = controller.getProfile(auth);
    assertTrue(response.getBody().getSuccess());
    assertNotNull(response.getBody().getData());
    // Mensaje es irrelevante, testing solo lógica
}
```

---

## 📋 LISTA DE MIGRACIÓN

- [ ] Reemplazar todas las instancias `AuthResponse` con `ApiResponse<T>`
- [ ] Usar `ApiResponseBuilder` en todos los endpoints
- [ ] Reemplazar strings hardcoded con `ApiConstants`
- [ ] Eliminar `@Autowired` y usar `@RequiredArgsConstructor`
- [ ] Validar que no hay try-catch innecesarios en controllers
- [ ] Verificar que todos los endpoints usan `AuthenticationHelper` para auth
- [ ] Actualizar tests unitarios

---

## 🚨 ERRORES COMUNES

### ❌ Error 1: Olvidar @RequiredArgsConstructor
```java
@RestController
@Slf4j
// ❌ Falta @RequiredArgsConstructor
public class MyController {
    private final UserService userService;  // Error: no inicializado
}
```

### ✅ Solución:
```java
@RestController
@Slf4j
@RequiredArgsConstructor  // ✅ Genera constructor automáticamente
public class MyController {
    private final UserService userService;  // OK
}
```

---

### ❌ Error 2: Retornar AuthResponse en lugar de ApiResponse
```java
@GetMapping("/data")
public ResponseEntity<AuthResponse> getData() {  // ❌ Tipo antiguo
    return ResponseEntity.ok(DataResponse...);
}
```

### ✅ Solución:
```java
@GetMapping("/data")
public ResponseEntity<ApiResponse<MyDataDto>> getData() {  // ✅ Genérico
    return ApiResponseBuilder.success("Datos obtenidos", dataDto);
}
```

---

### ❌ Error 3: Validar manualmente en controller
```java
@PostMapping("/image")
public ResponseEntity<?> upload(MultipartFile file) {
    // ❌ Validación repetida en múltiples endpoints
    if (file == null) throw new BadRequestException("No image");
    if (!isImageType(file.getContentType())) throw new BadRequestException("Invalid type");
}
```

### ✅ Solución:
```java
@PostMapping("/image")
public ResponseEntity<?> upload(MultipartFile file) {
    ValidationHelper.validateImageFile(file);  // ✅ Una sola línea
}
```

---

## 📞 Soporte

Para questions sobre la nueva estructura:
1. Revisar `ApiConstants.java` para entender mensajes y endpoints
2. Revisar `ApiResponse.java` para modelo de respuesta
3. Revisar `AuthenticationHelper.java` para validación autenticación
4. Revisar `ValidationHelper.java` para validaciones comunes

