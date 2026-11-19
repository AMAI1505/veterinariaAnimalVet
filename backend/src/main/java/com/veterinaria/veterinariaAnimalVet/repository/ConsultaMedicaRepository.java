package com.veterinaria.veterinariaAnimalVet.repository;

import com.veterinaria.veterinariaAnimalVet.model.ConsultaMedica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultaMedicaRepository extends JpaRepository<ConsultaMedica, Long> {
    List<ConsultaMedica> findByVeterinario_Id(Long veterinarioId);
}
