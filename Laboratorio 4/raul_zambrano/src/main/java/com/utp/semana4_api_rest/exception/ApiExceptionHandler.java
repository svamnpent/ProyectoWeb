package com.utp.semana4_api_rest.exception;

import com.utp.semana4_api_rest.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ProductoNoEncontradoException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensaje = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), mensaje, request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
