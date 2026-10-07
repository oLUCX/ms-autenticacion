package com.bodeganube.autenticacion.dto;

import com.bodeganube.autenticacion.model.Rol;
import com.bodeganube.autenticacion.model.Usuario;

/** Lo que la API devuelve de un usuario. Nunca incluye la contrasena ni su hash. */
public record UsuarioResponse(
        Long id,
        String username,
        Rol rol,
        String comercioId
) {
    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(), usuario.getRol(), usuario.getComercioId());
    }
}
