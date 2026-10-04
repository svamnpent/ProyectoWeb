package com.utp.semana2.exception;

public class ProductoNoEncontradoException extends RuntimeException {
    public ProductoNoEncontradoException(Long id) {
        super("No existe un producto con id: " + id);
    }
}