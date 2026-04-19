package com.safesign.backend.domain.contract.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoredFileInfo {

    private String originalFileName;
    private String storedFileName;
    private String fileUrl;
    private String mimeType;
    private Long fileSize;
}
