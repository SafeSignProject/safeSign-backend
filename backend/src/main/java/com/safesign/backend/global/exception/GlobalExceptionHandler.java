package com.safesign.backend.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(
                        ErrorResponse.builder()
                                .status(errorCode.getStatus().value())
                                .error(errorCode.getStatus().name())
                                .message(errorCode.getMessage())
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity
                .internalServerError()
                .body(
                        ErrorResponse.builder()
                                .status(500)
                                .error("INTERNAL_SERVER_ERROR")
                                .message("서버 내부 오류가 발생했습니다.")
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }
}