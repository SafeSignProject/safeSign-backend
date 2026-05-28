package com.safesign.backend.global.exception;

import com.safesign.backend.global.util.KstTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
                                .message(e.getMessage())
                                .timestamp(KstTime.now())
                                .build()
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {

        e.printStackTrace();

        return ResponseEntity
                .internalServerError()
                .body(
                        ErrorResponse.builder()
                                .status(500)
                                .error("INTERNAL_SERVER_ERROR")
                                .message(e.getMessage())
                                .timestamp(KstTime.now())
                                .build()
                );
    }
}
