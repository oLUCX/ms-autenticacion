package com.bodeganube.autenticacion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bodeganube.autenticacion.dto.ActualizarUsuarioRequest;
import com.bodeganube.autenticacion.dto.UsuarioRequest;
import com.bodeganube.autenticacion.dto.UsuarioResponse;
import com.bodeganube.autenticacion.exception.ReglaNegocioException;
import com.bodeganube.autenticacion.model.Rol;
import com.bodeganube.autenticacion.model.Usuario;
import com.bodeganube.autenticacion.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    @Test
    void crearGuardaLaContrasenaComoHashBcrypt() {
        when(usuarioRepository.existsByUsername("tienda-sur")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        usuarioService.crear(new UsuarioRequest("tienda-sur", "clave-segura-123", Rol.COMERCIO, "comercio-123"));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        assertThat(guardado.getValue().getPassword()).isNotEqualTo("clave-segura-123").startsWith("$2a$");
        assertThat(passwordEncoder.matches("clave-segura-123", guardado.getValue().getPassword())).isTrue();
    }

    @Test
    void crearConUsernameRepetidoLanzaReglaNegocio() {
        when(usuarioRepository.existsByUsername("operario1")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crear(
                new UsuarioRequest("operario1", "clave-segura-123", Rol.OPERARIO, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya esta en uso");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crearComercioSinComercioIdLanzaReglaNegocio() {
        when(usuarioRepository.existsByUsername("tienda-sur")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.crear(
                new UsuarioRequest("tienda-sur", "clave-segura-123", Rol.COMERCIO, " ")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("comercioId");
    }

    @Test
    void crearOperarioDescartaElComercioId() {
        when(usuarioRepository.existsByUsername("operario1")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        UsuarioResponse creado = usuarioService.crear(
                new UsuarioRequest("operario1", "clave-segura-123", Rol.OPERARIO, "comercio-123"));

        assertThat(creado.comercioId()).isNull();
    }

    @Test
    void actualizarSinContrasenaMantieneLaAnterior() {
        Usuario existente = new Usuario();
        existente.setId(1L);
        existente.setUsername("operario1");
        existente.setPassword("hash-anterior");
        existente.setRol(Rol.OPERARIO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        usuarioService.actualizar(1L, new ActualizarUsuarioRequest(Rol.COMERCIO, "comercio-123", null));

        assertThat(existente.getPassword()).isEqualTo("hash-anterior");
        assertThat(existente.getRol()).isEqualTo(Rol.COMERCIO);
    }
}
