package com.futureprograms.clients.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private Boolean success;
    private String message;
    private String token;
    private UserDto user;

    /**
     * Método para crear respuestas exitosas de autenticación
     */
    public static AuthResponse success(String message, String token, UserDto user) {
        return AuthResponse.builder()
                .success(true)
                .message(message)
                .token(token)
                .user(user)
                .build();
    }

    /**
     * Método para crear respuestas de error
     */
    public static AuthResponse error(String message) {
        return AuthResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}