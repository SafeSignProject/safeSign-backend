package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.dto.response.StoredFileInfo;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Profile("s3")
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.base-prefix}")
    private String basePrefix;

    @Override
    public StoredFileInfo storeFile(MultipartFile file, Long contractId) {

        try {
            String originalFileName = file.getOriginalFilename();
            String storedFileName = generateStoredFileName(originalFileName);

            String objectKey =
                    basePrefix + "/" + contractId + "/" + storedFileName;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(file.getBytes())
            );

            return StoredFileInfo.builder()
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .fileUrl(objectKey)
                    .mimeType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();

        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_STORAGE_FAILED);
        }
    }

    @Override
    public byte[] loadFile(String fileUrl) {

        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileUrl)
                    .build();

            ResponseBytes<?> response =
                    s3Client.getObjectAsBytes(request);

            return response.asByteArray();

        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_STORAGE_FAILED);
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