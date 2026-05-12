-- Script para crear la base de datos project1
-- Ejecuta este script en tu cliente MariaDB/MySQL

-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS project1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE project1;

-- Crear tabla de usuarios (ejemplo)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(200),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Crear tabla de roles (ejemplo)
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de permisos (ejemplo)
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de auditoría (ejemplo)
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Insertar algunos roles por defecto
INSERT IGNORE INTO roles (role_name, description) VALUES 
('ADMIN', 'Administrador del sistema'),
('USER', 'Usuario regular'),
('GUEST', 'Invitado');

-- Insertar algunos permisos por defecto
INSERT IGNORE INTO permissions (permission_name, description) VALUES 
('READ', 'Lectura de datos'),
('WRITE', 'Escritura de datos'),
('DELETE', 'Eliminación de datos'),
('ADMIN', 'Permisos de administrador');

-- Crear usuario de prueba (opcional)
-- El password es una cadena de ejemplo, deberías usar valores hasheados en producción
INSERT IGNORE INTO users (username, email, password, full_name) VALUES 
('admin', 'admin@project1.local', 'admin123', 'Administrador');

-- Mostrar la estructura de las tablas
SHOW TABLES;
DESCRIBE users;
DESCRIBE roles;
DESCRIBE permissions;
DESCRIBE audit_log;
