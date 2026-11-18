package com.veterinaria.veterinariaAnimalVet.repository;

import com.veterinaria.veterinariaAnimalVet.model.TurnoLaboral;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TurnoLaboralRepository extends JpaRepository<TurnoLaboral, Long> {
    Optional<TurnoLaboral> findByVeterinarioIdAndDiaSemana(Long veterinarioId, String diaSemana);
}
