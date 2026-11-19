package com.veterinaria.veterinariaAnimalVet.security;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTests {
    private JwtTokenService service() {
        JwtTokenService service = new JwtTokenService(new ObjectMapper(), "unit-test-key-that-is-at-least-32-bytes-long", false);
        service.initialize();
        return service;
    }

    @Test void emiteYValidaUnTokenFirmado() {
        JwtTokenService service = service();
        String token = service.issue("tutor@example.com", 3);
        var identity = service.validate(token);
        assertEquals("tutor@example.com", identity.username());
        assertEquals(3, identity.version());
    }

    @Test void rechazaTokensAlteradosYMalformados() {
        JwtTokenService service = service();
        String token = service.issue("tutor@example.com", 0);
        String[] parts = token.split("\\.");
        parts[2] = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("invalid-signature".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String altered = String.join(".", parts);
        assertNull(service.validate(altered));
        assertNull(service.validate("no.es.jwt"));
        assertNull(service.validate(""));
    }

    @Test void produccionRechazaUnaClaveAusente() {
        JwtTokenService service = new JwtTokenService(new ObjectMapper(), "", true);
        assertThrows(IllegalStateException.class, service::initialize);
    }
}
