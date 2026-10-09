package com.bodeganube.autenticacion.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bodeganube.autenticacion.dto.LoginRequest;
import com.bodeganube.autenticacion.dto.LoginResponse;
import com.bodeganube.autenticacion.exception.CredencialesInvalidasException;
import com.bodeganube.autenticacion.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void loginCorrectoDevuelve200ConToken() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(new LoginResponse("token-de-prueba"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"operario1\",\"password\":\"clave-segura-123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-de-prueba"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginIncorrectoDevuelve401() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new CredencialesInvalidasException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"operario1\",\"password\":\"otra-clave\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contrasena incorrectos"));
    }

    @Test
    void loginSinContrasenaDevuelve400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"operario1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.password").exists());
    }
}
