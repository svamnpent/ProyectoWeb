package com.phantom.api.exception;

import com.phantom.api.config.AppTime;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Slf4j
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

        // 4. Stock Insuficiente (400)
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

        // 5. Petición mal formada (JSON inválido, tipos incorrectos, parámetros faltantes) (400)
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex,
                        HttpServletRequest request) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                                "El cuerpo de la petición es inválido o está mal formado", request);
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex,
                        HttpServletRequest request) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                                "Falta el parámetro obligatorio: " + ex.getParameterName(), request);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                                "Valor inválido para el parámetro: " + ex.getName(), request);
        }

        // 6. Seguridad: sin autenticar (401) / sin permisos (403)
        //    Sin estos handlers, el catch-all de abajo convertiría errores de seguridad en 500.
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex,
                        HttpServletRequest request) {
                return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                        HttpServletRequest request) {
                return buildErrorResponse(HttpStatus.FORBIDDEN, "Forbidden",
                                "No tienes permisos para realizar esta acción", request);
        }

        // 7. Demasiados intentos de login (429)
        @ExceptionHandler(TooManyRequestsException.class)
        public ResponseEntity<ErrorResponse> handleTooManyRequests(TooManyRequestsException ex,
                        HttpServletRequest request) {
                return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", ex.getMessage(),
                                request);
        }

        // 8. Violación de integridad en BD: duplicados, registros con dependencias (409)
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                        HttpServletRequest request) {
                log.warn("Violación de integridad de datos en {}: {}", request.getRequestURI(),
                                ex.getMostSpecificCause().getMessage());
                return buildErrorResponse(HttpStatus.CONFLICT, "Conflict",
                                "La operación viola una restricción de integridad: el registro está duplicado "
                                                + "o tiene datos relacionados",
                                request);
        }

        // 9. Método no permitido (405) y ruta inexistente (404)
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                        HttpServletRequest request) {
                return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", ex.getMessage(),
                                request);
        }

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex,
                        HttpServletRequest request) {
                return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", "Recurso no encontrado", request);
        }

        // 10. Captura Global para Errores No Controlados (500)
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
                log.error("Error no controlado en {} {}", request.getMethod(), request.getRequestURI(), ex);
                return buildErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Internal Server Error",
                                "Ocurrió un error inesperado en el servidor",
                                request);
        }

        // Builder interno de DTO de respuesta de error
        private ResponseEntity<ErrorResponse> buildErrorResponse(
                        HttpStatus status,
                        String errorTitle,
                        String message,
                        HttpServletRequest request) {
                ErrorResponse error = ErrorResponse.builder()
                                .timestamp(OffsetDateTime.now(AppTime.ZONA))
                                .status(status.value())
                                .error(errorTitle)
                                .message(message)
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(error, status);
        }
}
