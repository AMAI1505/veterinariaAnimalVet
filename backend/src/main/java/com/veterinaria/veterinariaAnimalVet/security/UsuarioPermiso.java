package com.veterinaria.veterinariaAnimalVet.security;

import com.veterinaria.veterinariaAnimalVet.model.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("usuarioPermiso")
public class UsuarioPermiso {

    public boolean puedeVer(Authentication authentication) {
        String rol = rol(authentication);
        return "ADMINISTRADOR".equals(rol) || "RECEPCION".equals(rol) || "PERSONAL_VETERINARIA".equals(rol);
    }

    public boolean puedeVerMascotas(Authentication authentication) {
        String role = rol(authentication);
        if ("ADMINISTRADOR".equals(role) || "RECEPCION".equals(role)) return true;
        return authentication != null && authentication.getPrincipal() instanceof Usuario usuario
                && "PERSONAL_VETERINARIA".equals(role) && "VETERINARIO".equals(usuario.getTipoPersonal());
    }

    public boolean puedeCrear(Authentication authentication) {
        return puedeGestionarAgenda(authentication);
    }

    public boolean puedeEditar(Authentication authentication) {
        return "ADMINISTRADOR".equals(rol(authentication));
    }

    public boolean puedeEliminar(Authentication authentication) {
        return "ADMINISTRADOR".equals(rol(authentication));
    }

    public boolean esVeterinario(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Usuario usuario
                && "PERSONAL_VETERINARIA".equals(rol(authentication))
                && "VETERINARIO".equals(usuario.getTipoPersonal());
    }

    public boolean esGroomi(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Usuario usuario
                && "PERSONAL_VETERINARIA".equals(rol(authentication))
                && "ESTILISTA".equals(usuario.getTipoPersonal());
    }

    private boolean puedeGestionarAgenda(Authentication authentication) {
        String rol = rol(authentication);
        return "ADMINISTRADOR".equals(rol) || "RECEPCION".equals(rol);
    }

    private String rol(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            return "";
        }
        return usuario.getRol() == null ? "" : usuario.getRol().getNombre();
    }
}
