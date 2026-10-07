package com.bodeganube.autenticacion.service;

import com.bodeganube.autenticacion.dto.LoginRequest;
import com.bodeganube.autenticacion.dto.LoginResponse;
import com.bodeganube.autenticacion.exception.CredencialesInvalidasException;
import com.bodeganube.autenticacion.model.Usuario;
import com.bodeganube.autenticacion.repository.UsuarioRepository;
import com.bodeganube.autenticacion.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(CredencialesInvalidasException::new);

        // Se compara la contrasena recibida contra el hash BCrypt guardado en la base de datos.
        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new CredencialesInvalidasException();
        }
        return new LoginResponse(jwtService.generarToken(usuario));
    }
}
