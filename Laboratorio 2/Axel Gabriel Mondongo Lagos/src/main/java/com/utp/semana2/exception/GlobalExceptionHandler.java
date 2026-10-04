package com.utp.semana2.exception;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.utp.semana2.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ProductoNoEncontradoException.class)
 public ResponseEntity<ErrorResponse> manejarNoEncontrado(
 ProductoNoEncontradoException ex,
 HttpServletRequest request) {
 ErrorResponse error = new ErrorResponse(
 LocalDateTime.now(),
 HttpStatus.NOT_FOUND.value(),
 "Not Found",
 ex.getMessage(),
 request.getRequestURI()
 );
 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
 }
 @ExceptionHandler(IllegalArgumentException.class)
 public ResponseEntity<ErrorResponse> manejarSolicitudIncorrecta(
 IllegalArgumentException ex,
 HttpServletRequest request) {
 ErrorResponse error = new ErrorResponse(
 LocalDateTime.now(),
 HttpStatus.BAD_REQUEST.value(),
 "Bad Request",
 ex.getMessage(),
 request.getRequestURI()
 );
 return ResponseEntity.badRequest().body(error);
 }
}