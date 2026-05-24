package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.dto.response.StoredFileInfo;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFileInfo storeFile(MultipartFile file, Long contractId);

    byte[] loadFile(String fileUrl);

    void deleteFile(String fileUrl);
}