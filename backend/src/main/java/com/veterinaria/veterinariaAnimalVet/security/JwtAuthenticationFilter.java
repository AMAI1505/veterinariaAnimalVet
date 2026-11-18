package com.veterinaria.veterinariaAnimalVet.security;

import com.veterinaria.veterinariaAnimalVet.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService tokens;
    private final UsuarioService usuarios;

    public JwtAuthenticationFilter(JwtTokenService tokens, UsuarioService usuarios) {
        this.tokens = tokens;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            JwtTokenService.TokenIdentity identity = tokens.validate(authorization.substring(7).trim());
            if (identity != null) {
                try {
                    var user = usuarios.loadUserByUsername(identity.username());
                    if (user.isEnabled() && user.isAccountNonLocked() && user.getTokenVersion() == identity.version()) {
                        var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ignored) { }
            }
        }
        chain.doFilter(request, response);
    }
}
