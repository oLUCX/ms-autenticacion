package com.bodeganube.autenticacion.dto;

import com.bodeganube.autenticacion.model.Rol;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cambia el rol, el comercio y, si viene, la contrasena. El username no se modifica. */
public record ActualizarUsuarioRequest(
        @NotNull(message = "El rol es obligatorio (OPERARIO o COMERCIO)")
        Rol rol,

        @Size(max = 50, message = "El comercioId no puede superar los 50 caracteres")
        String comercioId,

        // Opcional: si no se envia, se mantiene la contrasena actual.
        @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres")
        String password
) {
}
