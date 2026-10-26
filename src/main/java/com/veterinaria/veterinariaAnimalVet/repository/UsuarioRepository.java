package com.veterinaria.veterinariaAnimalVet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.veterinariaAnimalVet.model.Usuario;
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
}
