package com.veterinaria.veterinariaAnimalVet.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

    @Test
    void unaCuentaSinFechaDeDesbloqueoEstaDisponible() {
        Usuario usuario = new Usuario();

        assertTrue(usuario.isAccountNonLocked());
    }

    @Test
    void unaCuentaBloqueadaSeLiberaCuandoPasaLaFechaDeDesbloqueo() {
        Usuario usuario = new Usuario();
        usuario.setFechaDesbloqueo(LocalDateTime.now().minusSeconds(1));

        assertTrue(usuario.isAccountNonLocked());
    }

    @Test
    void unaCuentaSigueBloqueadaAntesDeSuFechaDeDesbloqueo() {
        Usuario usuario = new Usuario();
        usuario.setFechaDesbloqueo(LocalDateTime.now().plusMinutes(10));

        assertFalse(usuario.isAccountNonLocked());
    }
}
