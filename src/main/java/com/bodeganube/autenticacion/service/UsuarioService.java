package com.bodeganube.autenticacion.service;

import com.bodeganube.autenticacion.dto.ActualizarUsuarioRequest;
import com.bodeganube.autenticacion.dto.UsuarioRequest;
import com.bodeganube.autenticacion.dto.UsuarioResponse;
import com.bodeganube.autenticacion.exception.RecursoNoEncontradoException;
import com.bodeganube.autenticacion.exception.ReglaNegocioException;
import com.bodeganube.autenticacion.model.Rol;
import com.bodeganube.autenticacion.model.Usuario;
import com.bodeganube.autenticacion.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Administracion de usuarios: alta, consulta, cambio de rol/contrasena y baja. */
@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponse::de)
                .toList();
    }

    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.de(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new ReglaNegocioException("El username " + request.username() + " ya esta en uso");
        }
        validarComercio(request.rol(), request.comercioId());

        Usuario usuario = new Usuario();
        usuario.setUsername(request.username());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(request.rol());
        usuario.setComercioId(comercioSegunRol(request.rol(), request.comercioId()));
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = buscar(id);
        validarComercio(request.rol(), request.comercioId());

        usuario.setRol(request.rol());
        usuario.setComercioId(comercioSegunRol(request.rol(), request.comercioId()));
        if (request.password() != null) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        usuarioRepository.delete(buscar(id));
    }

    /** RF-06: un usuario COMERCIO necesita su comercioId para ver solo sus propias ordenes. */
    private void validarComercio(Rol rol, String comercioId) {
        if (rol == Rol.COMERCIO && (comercioId == null || comercioId.isBlank())) {
            throw new ReglaNegocioException("Un usuario COMERCIO debe tener comercioId");
        }
    }

    /** El operario de bodega no pertenece a ningun comercio. */
    private String comercioSegunRol(Rol rol, String comercioId) {
        return rol == Rol.COMERCIO ? comercioId : null;
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario " + id));
    }
}
