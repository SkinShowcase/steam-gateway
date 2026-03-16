package com.skinsshowcase.steamgateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.stream.Collectors;

/**
 * Глобальная обработка исключений REST API (RFC 7807 Problem Details).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidSteamIdException.class)
    public ProblemDetail handleInvalidSteamId(InvalidSteamIdException ex, WebRequest request) {
        log.warn("Invalid Steam ID: {}", ex.getMessage());
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Invalid Steam ID");
        detail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        return detail;
    }

    @ExceptionHandler(SteamApiException.class)
    public ProblemDetail handleSteamApi(SteamApiException ex, WebRequest request) {
        log.warn("Steam API error: {}", ex.getMessage());
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "Steam API error");
        detail.setTitle("Steam API Error");
        detail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        return detail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        var errors = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", errors);
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors);
        detail.setTitle("Validation Failed");
        detail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", errors);
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors);
        detail.setTitle("Validation Failed");
        detail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleOther(Exception ex, WebRequest request) {
        log.error("Unhandled error", ex);
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        detail.setTitle("Internal Error");
        detail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        return detail;
    }
}
