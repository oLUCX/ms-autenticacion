package com.bodeganube.autenticacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeguridadConfig {

    /**
     * BCrypt guarda un hash con sal aleatoria y es lento a proposito, lo que encarece los ataques de
     * fuerza bruta. La contrasena en texto plano nunca se guarda ni se devuelve.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
