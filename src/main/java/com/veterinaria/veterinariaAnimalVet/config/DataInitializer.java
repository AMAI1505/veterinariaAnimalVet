package com.veterinaria.veterinariaAnimalVet.config;

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

    }
}