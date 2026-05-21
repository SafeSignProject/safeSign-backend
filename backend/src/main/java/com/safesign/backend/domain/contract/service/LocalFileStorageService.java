package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.dto.response.StoredFileInfo;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Primary
@Profile("local")
public class LocalFileStorageService implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
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
            throw new CustomException(ErrorCode.FILE_STORAGE_FAILED);
        }
    }

    @Override
    public byte[] loadFile(String fileUrl) {
        try {
            return Files.readAllBytes(Path.of(fileUrl));
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_STORAGE_FAILED);
        }
    }

    private void validateFileExists(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_UPLOAD_FILE);
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