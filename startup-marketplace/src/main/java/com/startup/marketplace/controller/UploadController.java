package com.startup.marketplace.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * POST /api/upload — recibe una imagen, la guarda en disco
 * y retorna la URL publica para guardar en stand.bannerUrl / stand.logoUrl.
 *
 * Requiere JWT — anyRequest().authenticated() en SecurityConfig lo cubre.
 */
@RestController
@RequestMapping("/api")
public class UploadController {

    @Value("${startup.upload.dir}")
    private String uploadDir;

    @Value("${server.port:8080}")
    private String serverPort;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file) throws IOException {

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Solo se permiten imagenes"));
        }

        String extension = getExtension(file.getOriginalFilename());
        if (!Set.of("jpg", "jpeg", "png", "webp").contains(extension)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Formato no soportado. Usa JPG, PNG o WebP"));
        }

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String filename = UUID.randomUUID() + "." + extension;
        Files.copy(file.getInputStream(), uploadPath.resolve(filename));

        String url = "http://localhost:" + serverPort + "/uploads/" + filename;
        return ResponseEntity.ok(Map.of("url", url));
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
