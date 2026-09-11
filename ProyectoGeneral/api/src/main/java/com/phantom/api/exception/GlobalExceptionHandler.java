package com.phantom.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        // 1. Recurso No Encontrado (404)
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
                        HttpServletRequest request) {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
        }

        // 2. Argumento o Negocio Inválido (400)
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                        HttpServletRequest request) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
        }

        // 3. Errores de Validación de DTOs con @Valid (400)
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                String mensajeErrores = ex.getBindingResult().getFieldErrors()
                                .stream()
                                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                                .collect(Collectors.joining(", "));

                return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validación Fallida", mensajeErrores, request);
        }

        // 4. Captura Global para Errores No Controlados (500)
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
                // En producción es recomendable registrar el log técnico del error real
                return buildErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Internal Server Error",
                                "Ocurrió un error inesperado en el servidor",
                                request);
        }

        // 5. Captura Global para Errores de Stock Insuficiente (400)
        @ExceptionHandler(StockInsuficienteException.class)
        public ResponseEntity<ErrorResponse> handleStockInsuficiente(StockInsuficienteException ex,
                        HttpServletRequest request) {
                String detalle = (ex.getErrores() != null && !ex.getErrores().isEmpty())
                                ? String.join(" | ", ex.getErrores())
                                : ex.getMessage();

                return buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                "Stock Insuficiente",
                                detalle,
                                request);
        }

        // Builder interno de DTO de respuesta de error
        private ResponseEntity<ErrorResponse> buildErrorResponse(
                        HttpStatus status,
                        String errorTitle,
                        String message,
                        HttpServletRequest request) {
                ErrorResponse error = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(status.value())
                                .error(errorTitle)
                                .message(message)
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(error, status);
        }
}