package com.bodeganube.autenticacion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bodeganube.autenticacion.dto.LoginRequest;
import com.bodeganube.autenticacion.dto.LoginResponse;
import com.bodeganube.autenticacion.exception.CredencialesInvalidasException;
import com.bodeganube.autenticacion.model.Rol;
import com.bodeganube.autenticacion.model.Usuario;
import com.bodeganube.autenticacion.repository.UsuarioRepository;
import com.bodeganube.autenticacion.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Login con BCrypt real; el repositorio y la emision del JWT se simulan con Mockito. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, jwtService, passwordEncoder);
    }

    @Test
    void loginConCredencialesCorrectasDevuelveTokenBearer() {
        Usuario operario = usuario("operario1", "clave-segura-123");
        when(usuarioRepository.findByUsername("operario1")).thenReturn(Optional.of(operario));
        when(jwtService.generarToken(operario)).thenReturn("token-de-prueba");

        LoginResponse respuesta = authService.login(new LoginRequest("operario1", "clave-segura-123"));

        assertThat(respuesta.token()).isEqualTo("token-de-prueba");
        assertThat(respuesta.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void loginConContrasenaIncorrectaLanzaCredencialesInvalidas() {
        when(usuarioRepository.findByUsername("operario1"))
                .thenReturn(Optional.of(usuario("operario1", "clave-segura-123")));

        assertThatThrownBy(() -> authService.login(new LoginRequest("operario1", "otra-clave")))
                .isInstanceOf(CredencialesInvalidasException.class);
        verify(jwtService, never()).generarToken(any());
    }

    @Test
    void loginConUsuarioInexistenteDaElMismoErrorQueContrasenaIncorrecta() {
        when(usuarioRepository.findByUsername("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nadie", "clave-segura-123")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Usuario o contrasena incorrectos");
    }

    private Usuario usuario(String username, String passwordPlano) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(passwordPlano));
        usuario.setRol(Rol.OPERARIO);
        return usuario;
    }
}
