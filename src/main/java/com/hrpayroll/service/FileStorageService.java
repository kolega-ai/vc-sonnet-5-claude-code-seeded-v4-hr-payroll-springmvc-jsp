package com.hrpayroll.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path baseDir;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory", e);
        }
    }

    public String store(MultipartFile file) {
        try {
            String ext = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
                if (ext.length() > 10) ext = ""; // guard against absurd extensions
            }
            String storedName = UUID.randomUUID() + ext.replaceAll("[^a-zA-Z0-9.]", "");
            Path target = baseDir.resolve(storedName).normalize();
            if (!target.startsWith(baseDir)) {
                throw new IllegalArgumentException("Invalid file name");
            }
            Files.copy(file.getInputStream(), target);
            return storedName;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file", e);
        }
    }

    /**
     * Stores an attachment staff add to an HR request case file. The bureau
     * keeps the name the employee gave the file so the case worker sees the
     * same name the employee referred to in the request text.
     */
    public String storeCaseAttachment(MultipartFile file) {
        try {
            String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "attachment";
            String leaf = Paths.get(original).getFileName().toString().replaceAll("[\\\\/:*?\"<>|]", "_");
            String storedName = System.currentTimeMillis() + "-" + leaf;
            Path target = baseDir.resolve(storedName).normalize();
            if (!target.startsWith(baseDir)) {
                throw new IllegalArgumentException("Invalid file name");
            }
            file.transferTo(target);
            return storedName;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store attachment", e);
        }
    }

    /**
     * Precision baseline primitive #5: resolve the requested filename against
     * the canonicalized upload base directory and refuse anything that
     * normalizes outside of it before returning a Resource to serve.
     */
    public Resource loadAsResource(String storedFilename) {
        try {
            Path candidate = baseDir.resolve(storedFilename).normalize();
            if (!candidate.startsWith(baseDir)) {
                throw new IllegalArgumentException("Path traversal attempt blocked");
            }
            Resource resource = new UrlResource(candidate.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("File not found: " + storedFilename);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid file path", e);
        }
    }

    public void delete(String storedFilename) {
        try {
            Path candidate = baseDir.resolve(storedFilename).normalize();
            if (!candidate.startsWith(baseDir)) {
                return;
            }
            Files.deleteIfExists(candidate);
        } catch (IOException ignored) {
            // best-effort cleanup
        }
    }

    /**
     * Lists the files the bureau dropped for one payroll batch. The batch
     * folder is named in the manifest the payroll desk uploads alongside the
     * run, so the preview shows exactly what the exchange picked up.
     */
    public java.util.List<String> listBatchDir(String batchPath) {
        java.util.List<String> names = new java.util.ArrayList<>();
        try (java.util.stream.Stream<Path> entries = Files.list(baseDir.resolve(batchPath))) {
            entries.forEach(p -> names.add(p.getFileName().toString()));
        } catch (IOException e) {
            throw new IllegalStateException("Could not list batch " + batchPath, e);
        }
        return names;
    }
}
