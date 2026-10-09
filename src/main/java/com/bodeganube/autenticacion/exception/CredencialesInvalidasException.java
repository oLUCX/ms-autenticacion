package com.bodeganube.autenticacion.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Login fallido. Se usa el mismo mensaje si el usuario no existe o si la contrasena no coincide,
 * para no revelar que usernames existen. Se traduce a HTTP 401.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Usuario o contrasena incorrectos");
    }
}
