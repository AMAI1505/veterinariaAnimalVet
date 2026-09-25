package com.veterinaria.veterinariaAnimalVet.config;

import com.veterinaria.veterinariaAnimalVet.model.Rol;
import com.veterinaria.veterinariaAnimalVet.model.Usuario;
import com.veterinaria.veterinariaAnimalVet.repository.RolRepository;
import com.veterinaria.veterinariaAnimalVet.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String initialAdminPassword;

    public DataInitializer(RolRepository rolRepository,
                           UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${animalvet.initial-admin-password:admin123}") String initialAdminPassword) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.initialAdminPassword = initialAdminPassword;
    }

    @Override
    public void run(String... args) throws Exception {
        Rol rolPropietario;
        if (!rolRepository.existsByNombre("PROPIETARIO")) {
            rolPropietario = new Rol(
                    "PROPIETARIO",
                    "Acceso total al sistema veterinario",
                    true,   // puedeVer
                    true,   // puedeCrear
                    true,   // puedeEditar
                    true    // puedeEliminar
            );
            rolPropietario = rolRepository.save(rolPropietario);
            System.out.println("Rol PROPIETARIO creado.");
        } else {
            rolPropietario = rolRepository.findByNombre("PROPIETARIO").orElseThrow();
        }

        if (!rolRepository.existsByNombre("USUARIO")) {
            rolRepository.save(new Rol(
                    "USUARIO",
                    "Acceso de usuario registrado: puede consultar y crear registros.",
                    true,
                    true,
                    false,
                    false
            ));
        }

        if (!usuarioRepository.findByUsername("propietario").isPresent()) {
            Usuario propietario = new Usuario();
            propietario.setUsername("propietario");
          
            propietario.setPassword(passwordEncoder.encode(initialAdminPassword));
            propietario.setRol(rolPropietario);
            propietario.setEnabled(true);
            usuarioRepository.save(propietario);
            System.out.println("Usuario administrador inicial creado.");
        }
    }
}