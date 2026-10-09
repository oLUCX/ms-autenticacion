package com.bodeganube.autenticacion.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Se lanza cuando la peticion esta bien formada pero choca con una regla del negocio
 * (username repetido, usuario COMERCIO sin comercioId...).
 * Se traduce a HTTP 409.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
