package com.veterinaria.veterinariaAnimalVet.controller;

import com.veterinaria.veterinariaAnimalVet.model.Usuario;
import com.veterinaria.veterinariaAnimalVet.security.JwtTokenService;
import com.veterinaria.veterinariaAnimalVet.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService tokens;
    private final UsuarioService usuarios;

    public AuthApiController(AuthenticationManager authenticationManager, JwtTokenService tokens, UsuarioService usuarios) {
        this.authenticationManager = authenticationManager;
        this.tokens = tokens;
        this.usuarios = usuarios;
    }

    @PostMapping("/login")
    public SessionResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username().trim().toLowerCase(java.util.Locale.ROOT), request.password()));
            Usuario usuario = (Usuario) authentication.getPrincipal();
            usuarios.resetearIntentosFallidos(usuario.getUsername());
            return responseFor(usuario, tokens.issue(usuario.getUsername(), usuario.getTokenVersion()));
        } catch (org.springframework.security.core.AuthenticationException ex) {
            usuarios.findByUsername(request.username().trim().toLowerCase(java.util.Locale.ROOT)).ifPresent(usuarios::aumentarIntentosFallidos);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos.");
        }
    }

    @GetMapping("/me")
    public SessionResponse me(@org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario) {
        return responseFor(usuario, null);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario) {
        usuario.invalidateTokens();
        usuarios.save(usuario);
    }

    private SessionResponse responseFor(Usuario usuario, String token) {
        return new SessionResponse(token, usuario.getUsername(), usuario.getNombreCompleto() == null ? usuario.getUsername() : usuario.getNombreCompleto(), usuario.getRol().getNombre(), usuario.getTipoPersonal());
    }

    public record LoginRequest(@NotBlank @Size(max=50) String username, @NotBlank @Size(max=100) String password) { }
    public record SessionResponse(String token, String username, String name, String role, String tipoPersonal) { }
}
