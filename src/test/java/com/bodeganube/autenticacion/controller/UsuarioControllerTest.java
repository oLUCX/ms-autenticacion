package com.bodeganube.autenticacion.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bodeganube.autenticacion.dto.UsuarioRequest;
import com.bodeganube.autenticacion.dto.UsuarioResponse;
import com.bodeganube.autenticacion.exception.RecursoNoEncontradoException;
import com.bodeganube.autenticacion.model.Rol;
import com.bodeganube.autenticacion.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    void crearUsuarioDevuelve201SinExponerLaContrasena() throws Exception {
        when(usuarioService.crear(any(UsuarioRequest.class)))
                .thenReturn(new UsuarioResponse(1L, "tienda-sur", Rol.COMERCIO, "comercio-123"));

        mockMvc.perform(post("/api/auth/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tienda-sur\",\"password\":\"clave-segura-123\","
                                + "\"rol\":\"COMERCIO\",\"comercioId\":\"comercio-123\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/auth/usuarios/1")))
                .andExpect(jsonPath("$.username").value("tienda-sur"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void crearUsuarioConContrasenaCortaDevuelve400() throws Exception {
        mockMvc.perform(post("/api/auth/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tienda-sur\",\"password\":\"123\",\"rol\":\"COMERCIO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.password").exists());
    }

    @Test
    void crearUsuarioConRolDesconocidoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/auth/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tienda-sur\",\"password\":\"clave-segura-123\",\"rol\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerUsuarioInexistenteDevuelve404() throws Exception {
        when(usuarioService.obtener(99L)).thenThrow(new RecursoNoEncontradoException("No existe el usuario 99"));

        mockMvc.perform(get("/api/auth/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el usuario 99"));
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/auth/usuarios/1"))
                .andExpect(status().isNoContent());

        verify(usuarioService).eliminar(1L);
    }
}
