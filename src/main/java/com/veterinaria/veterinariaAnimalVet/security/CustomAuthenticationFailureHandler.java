package com.veterinaria.veterinariaAnimalVet.security;

import com.veterinaria.veterinariaAnimalVet.model.Usuario;
import com.veterinaria.veterinariaAnimalVet.service.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final UsuarioService usuarioService;

    public CustomAuthenticationFailureHandler(@Lazy UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        
        String username = request.getParameter("username");
        String errorMessage = "Usuario o contraseña incorrectos.";

        if (exception instanceof LockedException) {
            errorMessage = "Tu cuenta está bloqueada temporalmente por múltiples intentos fallidos. Inténtalo de nuevo más tarde.";
        } else if (username != null) {
            Optional<Usuario> userOpt = usuarioService.findByUsername(username);
            if (userOpt.isPresent()) {
                Usuario user = userOpt.get();
                if (user.getFechaDesbloqueo() != null && !user.isAccountNonLocked()) {
                    errorMessage = "Has superado el número máximo de intentos. Tu cuenta ha sido bloqueada por " + UsuarioService.TIEMPO_BLOQUEO_MINUTOS + " minutos.";
                } else if (user.getIntentosFallidos() > 0) {
                    int intentosRestantes = UsuarioService.MAX_INTENTOS_FALLIDOS - user.getIntentosFallidos();
                    if (intentosRestantes > 0) {
                        errorMessage = "Usuario o contraseña incorrectos. Te quedan " + intentosRestantes + " intentos.";
                    }
                }
            }
        }

        request.getSession().setAttribute("errorMessage", errorMessage);
        setDefaultFailureUrl("/login?error=true");
        super.onAuthenticationFailure(request, response, exception);
    }
}
