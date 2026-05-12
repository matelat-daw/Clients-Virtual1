package com.futureprograms.clients.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuración de Spring Web para servir archivos estáticos desde directorios externos
 * Sirve tanto imágenes de usuario como imágenes por defecto desde la carpeta de uploads
 */
@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Obtener la ruta absoluta del directorio de carga
        Path uploadPath;
        try {
            uploadPath = Paths.get(uploadDir).toAbsolutePath();
            // Intentar crear el directorio si no existe
            if (!Files.exists(uploadPath)) {
                log.info("📁 Creando directorio de imágenes: {}", uploadPath);
                Files.createDirectories(uploadPath);
            }
        } catch (Exception e) {
            log.warn("⚠️ No se pudo usar el directorio {}, usando ./uploads local. Error: {}", uploadDir, e.getMessage());
            uploadPath = Paths.get("./uploads").toAbsolutePath();
            try {
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            } catch (Exception ex) {
                log.error("❌ Error crítico: no se pudo crear ni el directorio local de imágenes.");
            }
        }
        
        log.info("📁 Directorio de imágenes final: {}", uploadPath);
        
        // Crear subdirectorio de imágenes por defecto si no existe
        try {
            Path defaultPath = uploadPath.resolve("default");
            if (!Files.exists(defaultPath)) {
                Files.createDirectories(defaultPath);
            }
        } catch (Exception e) {
            log.error("❌ Error al crear subdirectorio 'default': {}", e.getMessage());
        }

        // Mapear /images/** a la carpeta de carga externa
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath.toString().replace("\\", "/") + "/");
        
        log.info("✅ Mapeando /images/** -> file:{}/ ", uploadPath);
    }
}
