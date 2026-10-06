package com.bodeganube.autenticacion.service;

import com.bodeganube.autenticacion.dto.LoginRequest;
import com.bodeganube.autenticacion.dto.LoginResponse;
import com.bodeganube.autenticacion.exception.CredencialesInvalidasException;
import com.bodeganube.autenticacion.model.Usuario;
import com.bodeganube.autenticacion.repository.UsuarioRepository;
import com.bodeganube.autenticacion.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RF-01: autenticacion. Valida las credenciales y emite un JWT con el rol y el comercioId del usuario.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(CredencialesInvalidasException::new);

        // Pendiente: comparar contra un hash BCrypt en vez de texto plano.
        if (!usuario.getPassword().equals(request.password())) {
            throw new CredencialesInvalidasException();
        }
        return new LoginResponse(jwtService.generarToken(usuario));
    }
}
