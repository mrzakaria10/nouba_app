package com.nouba.app.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * AgencyService for handling file storage operations
 */
@Service
public class FileStorageService {
    private final String uploadDir = "uploads/";

    /**
     * Stores a file in the upload directory
     * @param file The file to store
     * @return The generated filename
     * @throws IOException if file operations fail
     */
    public String storeFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    /**
     * Deletes a file from the upload directory
     * @param fileName The name of the file to delete
     */
    public void deleteFile(String fileName) {
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file: " + fileName, e);
        }
    }
}
