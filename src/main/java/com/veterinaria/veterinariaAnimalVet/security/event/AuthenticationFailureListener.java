package com.veterinaria.veterinariaAnimalVet.security.event;

import com.veterinaria.veterinariaAnimalVet.service.UsuarioService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final UsuarioService usuarioService;

    public AuthenticationFailureListener(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        if (username != null) {
            usuarioService.findByUsername(username).ifPresent(usuarioService::aumentarIntentosFallidos);
        }
    }
}
