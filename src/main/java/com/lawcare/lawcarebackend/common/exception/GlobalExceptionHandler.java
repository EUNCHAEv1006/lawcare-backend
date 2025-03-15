package com.lawcare.lawcarebackend.common.exception;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 예측하지 못한 모든 예외 처리 (500)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception ex, HttpServletRequest request) {
        log.error("알 수 없는 오류 발생 (URI: {}): {}", request.getRequestURI(), ex.getMessage(), ex);

        return new ErrorResponse(
            LocalDateTime.now(),
            500,
            "서버 내부 오류 발생",
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    /**
     * @Valid 검증 실패 시 발생 (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("검증 실패 (URI: {}): {}", request.getRequestURI(), ex.getMessage());

        return new ErrorResponse(
            LocalDateTime.now(),
            400,
            "잘못된 요청",
            "검증 오류: " + ex.getBindingResult(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArg(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("유효하지 않은 요청 데이터 (URI: {}): {}", request.getRequestURI(), ex.getMessage());

        return new ErrorResponse(
            LocalDateTime.now(),
            400,
            "잘못된 요청",
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    /**
     * 통일된 에러 응답 구조
     */
    public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
    ) {
    }
}
