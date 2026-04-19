package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.dto.response.StoredFileInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public StoredFileInfo storeFile(MultipartFile file, Long contractId) {
        validateFileExists(file);

        try {
            String originalFileName = file.getOriginalFilename();
            String storedFileName = generateStoredFileName(originalFileName);

            Path contractDir = Paths.get(uploadDir, String.valueOf(contractId));
            Files.createDirectories(contractDir);

            Path targetPath = contractDir.resolve(storedFileName);
            file.transferTo(targetPath.toFile());

            return StoredFileInfo.builder()
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .fileUrl(targetPath.toString())
                    .mimeType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    private void validateFileExists(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드 파일이 비어 있습니다.");
        }
    }

    private String generateStoredFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString();

        if (originalFileName == null || !originalFileName.contains(".")) {
            return uuid;
        }

        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return uuid + extension;
    }
}