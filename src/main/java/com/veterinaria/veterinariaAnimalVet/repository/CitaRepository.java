package com.veterinaria.veterinariaAnimalVet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.veterinaria.veterinariaAnimalVet.model.Cita;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    boolean existsByFechaHora(java.time.LocalDateTime fechaHora);
    boolean existsByFechaHoraAndIdNot(java.time.LocalDateTime fechaHora, Long id);
    boolean existsByFechaHoraAndVeterinarioId(java.time.LocalDateTime fechaHora, Long veterinarioId);
    boolean existsByFechaHoraAndVeterinarioIdAndIdNot(java.time.LocalDateTime fechaHora, Long veterinarioId, Long id);
}
