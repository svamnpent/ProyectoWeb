package com.phantom.api.exception;

import java.util.List;

public class StockInsuficienteException extends RuntimeException {

    private final List<String> errores;

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
        this.errores = List.of(mensaje);
    }

    public StockInsuficienteException(List<String> errores) {
        super(String.join(" | ", errores));
        this.errores = errores;
    }

    public List<String> getErrores() {
        return errores;
    }
}