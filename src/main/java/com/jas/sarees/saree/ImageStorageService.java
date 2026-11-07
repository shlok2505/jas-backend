package com.jas.sarees.saree;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final Path uploadDir;

    public ImageStorageService(@Value("${app.upload-dir}") String dir) throws IOException {
        this.uploadDir = Path.of(dir).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
    }

    public String store(MultipartFile file) throws IOException {
        String extension = EXTENSIONS.get(file.getContentType());
        if (extension == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Photo must be a JPEG, PNG or WebP image");
        }
        String filename = UUID.randomUUID() + extension;
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, uploadDir.resolve(filename));
        }
        return "/images/" + filename;
    }
}