package com.veterinaria.veterinariaAnimalVet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.veterinariaAnimalVet.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByDni(String dni);
    boolean existsByDniAndIdNot(String dni, Long id);
}



