package com.utp.semana4.exception;

 
import java.util.stream.Collectors; 

 
import org.springframework.http.HttpStatus; 
import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.MethodArgumentNotValidException; 
import org.springframework.web.bind.annotation.ExceptionHandler; 
import org.springframework.web.bind.annotation.RestControllerAdvice;

 
import com.utp.semana4.dto.ErrorResponse; 
 
import jakarta.servlet.http.HttpServletRequest; 
 
@RestControllerAdvice
public class ApiExceptionHandler {
     @ExceptionHandler(ProductoNoEncontradoException.class) 
    public ResponseEntity<ErrorResponse> manejarProductoNoEncontrado( 
            ProductoNoEncontradoException ex, 
            HttpServletRequest request) { 
        ErrorResponse error = new ErrorResponse( 
                HttpStatus.NOT_FOUND.value(), 
                ex.getMessage(), 
                request.getRequestURI() 
        ); 
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error); 
    } 
 
    @ExceptionHandler(MethodArgumentNotValidException.class) 
    public ResponseEntity<ErrorResponse> manejarValidaciones( 
            MethodArgumentNotValidException ex, 
            HttpServletRequest request) { 
 
        String mensaje = ex.getBindingResult() 
                .getFieldErrors() 
                .stream() 
                .map(error -> error.getField() + ": " + error.getDefaultMessage()) 
                .collect(Collectors.joining("; ")); 
 
        ErrorResponse error = new ErrorResponse( 
                HttpStatus.BAD_REQUEST.value(), 
                mensaje, 
                request.getRequestURI() 
        ); 
        return ResponseEntity.badRequest().body(error); 
    } 
}
