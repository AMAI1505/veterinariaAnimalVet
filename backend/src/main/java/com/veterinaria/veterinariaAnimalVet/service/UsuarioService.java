package com.veterinaria.veterinariaAnimalVet.service;

import com.veterinaria.veterinariaAnimalVet.model.Usuario;
import com.veterinaria.veterinariaAnimalVet.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> findAll() {
        return repo.findAll();
    }

    public Optional<Usuario> findById(Long id) {
        return repo.findById(id);
    }

    public Optional<Usuario> findByUsername(String username) {
        return repo.findByUsername(username);
    }

    @Override
    public Usuario loadUserByUsername(String username) throws UsernameNotFoundException {
        return repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public Usuario registrarUsuario(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return repo.save(usuario);
    }

    public Usuario save(Usuario usuario) { return repo.save(usuario); }

    public String encodePassword(String password) { return passwordEncoder.encode(password); }

    public boolean existsByUsername(String username) {
        return repo.findByUsername(username).isPresent();
    }

    public static final int MAX_INTENTOS_FALLIDOS = 3;
    public static final int TIEMPO_BLOQUEO_MINUTOS = 15;

    public void aumentarIntentosFallidos(Usuario usuario) {
        if (usuario.getFechaDesbloqueo() != null && !usuario.isAccountNonLocked()) {
            return;
        }
        if (usuario.getFechaDesbloqueo() != null) {
            usuario.setIntentosFallidos(0);
            usuario.setFechaDesbloqueo(null);
        }
        int nuevosIntentos = usuario.getIntentosFallidos() + 1;
        usuario.setIntentosFallidos(nuevosIntentos);
        if (nuevosIntentos >= MAX_INTENTOS_FALLIDOS) {
            bloquearCuenta(usuario);
        }
        repo.save(usuario);
    }

    public void resetearIntentosFallidos(String username) {
        repo.findByUsername(username).ifPresent(usuario -> {
            if (usuario.getIntentosFallidos() > 0 || usuario.getFechaDesbloqueo() != null) {
                usuario.setIntentosFallidos(0);
                usuario.setFechaDesbloqueo(null);
                repo.save(usuario);
            }
        });
    }

    private void bloquearCuenta(Usuario usuario) {
        usuario.setFechaDesbloqueo(java.time.LocalDateTime.now().plusMinutes(TIEMPO_BLOQUEO_MINUTOS));
    }
}
