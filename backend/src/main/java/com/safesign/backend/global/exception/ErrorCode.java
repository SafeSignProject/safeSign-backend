package com.safesign.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    AI_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 분석 중 오류가 발생했습니다."),
    CONTRACT_NOT_FOUND(HttpStatus.NOT_FOUND, "계약서를 찾을 수 없습니다."),
    CONTRACT_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "계약서 파일을 찾을 수 없습니다."),
    OCR_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "OCR 결과를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    EMPTY_UPLOAD_FILE(HttpStatus.BAD_REQUEST, "업로드 파일이 비어 있습니다."),
    INVALID_PDF_UPLOAD(HttpStatus.BAD_REQUEST, "PDF 파일만 업로드할 수 있습니다."),
    INVALID_IMAGE_UPLOAD(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드할 수 있습니다."),
    INVALID_PDF_FILE_COUNT(HttpStatus.BAD_REQUEST, "PDF 업로드는 파일 1개만 가능합니다."),
    PDF_PAGE_COUNT_READ_FAILED(HttpStatus.BAD_REQUEST, "PDF 페이지 수를 읽는 중 오류가 발생했습니다."),

    OCR_PROCESS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OCR 처리 중 오류가 발생했습니다."),
    FILE_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 중 오류가 발생했습니다."),

    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 올바르지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    ANALYSIS_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "분석 로그를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
