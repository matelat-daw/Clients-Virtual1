package com.futureprograms.project1.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/server")
public class ServerController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getServerInfo() throws UnknownHostException {
        Map<String, Object> serverInfo = new HashMap<>();

        // Información básica
        serverInfo.put("applicationName", applicationName);
        serverInfo.put("port", serverPort);
        serverInfo.put("hostname", InetAddress.getLocalHost().getHostName());
        serverInfo.put("ipAddress", InetAddress.getLocalHost().getHostAddress());

        // Información de la JVM
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvmInfo = new HashMap<>();
        jvmInfo.put("javaVersion", System.getProperty("java.version"));
        jvmInfo.put("javaVendor", System.getProperty("java.vendor"));
        jvmInfo.put("osName", System.getProperty("os.name"));
        jvmInfo.put("osVersion", System.getProperty("os.version"));
        jvmInfo.put("totalMemory", runtime.totalMemory() / (1024 * 1024) + " MB");
        jvmInfo.put("freeMemory", runtime.freeMemory() / (1024 * 1024) + " MB");
        jvmInfo.put("maxMemory", runtime.maxMemory() / (1024 * 1024) + " MB");
        serverInfo.put("jvm", jvmInfo);

        // Información de tiempo
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        serverInfo.put("timestamp", LocalDateTime.now().format(formatter));
        serverInfo.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime() + " ms");

        // Estado
        serverInfo.put("status", "OK");

        return ResponseEntity.ok(serverInfo);
    }
}
