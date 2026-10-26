package com.veterinaria.veterinariaAnimalVet.security;

import com.veterinaria.veterinariaAnimalVet.model.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("usuarioPermiso")
public class UsuarioPermiso {

    public boolean puedeCrear(Authentication authentication) {
        return tienePermiso(authentication, "CREAR");
    }

    public boolean puedeEditar(Authentication authentication) {
        return tienePermiso(authentication, "EDITAR");
    }

    public boolean puedeEliminar(Authentication authentication) {
        return tienePermiso(authentication, "ELIMINAR");
    }

    public boolean puedeVer(Authentication authentication) {
        return tienePermiso(authentication, "VER");
    }

    private boolean tienePermiso(Authentication authentication, String permiso) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        if (!(authentication.getPrincipal() instanceof Usuario usuario)) return false;
        if (usuario.getRol() == null) return false;

        if ("PROPIETARIO".equals(usuario.getRol().getNombre())) return true;

        return switch (permiso) {
            case "VER"      -> usuario.getRol().isPuedeVer();
            case "CREAR"    -> usuario.getRol().isPuedeCrear();
            case "EDITAR"   -> usuario.getRol().isPuedeEditar();
            case "ELIMINAR" -> usuario.getRol().isPuedeEliminar();
            default         -> false;
        };
    }
}
