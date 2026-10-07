package com.bodeganube.autenticacion.dto;

import com.bodeganube.autenticacion.model.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos para registrar un usuario. La contrasena llega en texto plano y se guarda como hash BCrypt. */
public record UsuarioRequest(
        @NotBlank(message = "El username es obligatorio")
        @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
        String username,

        // BCrypt solo considera los primeros 72 bytes, por eso el maximo.
        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio (OPERARIO o COMERCIO)")
        Rol rol,

        @Size(max = 50, message = "El comercioId no puede superar los 50 caracteres")
        String comercioId
) {
}
