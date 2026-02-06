package com.example.demo.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les erreurs de validation (@Valid)
     * HTTP 400 - BAD REQUEST
     * @Param MethodArgumentNotValidException
     * @Param HttpServletRequest
     * @Return ValidationErrorResponse
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotFoundException(MethodArgumentNotValidException ex,
                                                                                  HttpServletRequest request) {
        log.error("MethodArgumentNotValidException: Validation failed");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach((fieldError) -> {
            String fieldName = ((FieldError) fieldError).getField();
            String errorMessage = fieldError.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse errorResponse = ValidationErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Erreur(s) de validation")
                .path(request.getRequestURI())
                .fieldErrors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Gère les exceptions ResourceNotFoundException
     * HTTP 404 - NOT FOUND
     * @Param ResourceNotFoundException
     * @Param HttpServletRequest
     * @Return ErrorResponse
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex,
                                                                  HttpServletRequest request) {
        log.error("ResourceNotFoundException: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Gère les exceptions ResourceAlreadyExistsException
     * HTTP 409 - CONFLICT
     * @Param ResourceAlreadyExistsException
     * @Param HttpServletRequest
     * @Return ErrorResponse
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex,
                                                                       HttpServletRequest request) {
        log.error("ResourceAlreadyExistsException: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Gère les exceptions InvalidReservationException
     * HTTP 409 - BAD REQUEST
     * @Param InvalidReservationException
     * @Param HttpServletRequest
     * @Return ErrorResponse
     */
    @ExceptionHandler(InvalidReservationException.class)
    ResponseEntity<ErrorResponse> handleInvalidReservationException(InvalidReservationException ex,
                                                                    HttpServletRequest request) {
        log.error("InvalidReservationException: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Gère les exceptions BusinessException
     * HTTP 400 - BAD REQUEST
     * @Param BusinessException
     * @Param HttpServletRequest
     * @Return ErrorResponse
     */
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex,
                                                          HttpServletRequest request) {
        log.error("BusinessException: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Gère les IllegalArgumentException
     * HTTP 400 - BAD REQUEST
     * @Param IllegalArgumentException
     * @Param HttpServletRequest
     * @Return ErrorResponse
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.error("IllegalArgumentException: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Gère les erreurs de conversion de type
     * HTTP 400 - BAD REQUEST
     * @Param MethodArgumentTypeMismatchException
     * @Param HttpServletRequest
     * @Return ErrorResponse
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.error("MethodArgumentTypeMismatchException: {}", ex.getMessage());

        String message = String.format(
                "Le paramètre '%s' doit être de type '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "inconnu"
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gère toutes les autres exceptions non gérées
     * HTTP 500 - INTERNAL SERVER ERROR
     * @Param Exception
     * @Param HttpServletRequest
     * @Return ErrorResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("Exception: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Une erreur interne est survenue. Veuillez réessayer plus tard.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Gère les NullPointerException
     * HTTP 500 - INTERNAL SERVER ERROR
     * @Param Exception
     * @Param NullPointerException
     * @Return ErrorResponse
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException ex, HttpServletRequest request) {
        log.error("NullPointerException: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Une erreur inattendue est survenue (référence nulle)")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
