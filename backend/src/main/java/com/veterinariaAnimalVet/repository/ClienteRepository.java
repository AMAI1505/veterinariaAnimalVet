package com.veterinaria.veterinariaAnimalVet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.veterinariaAnimalVet.model.Cliente;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDni(String dni);
    Optional<Cliente> findByCorreoIgnoreCaseAndDni(String correo, String dni);
}



