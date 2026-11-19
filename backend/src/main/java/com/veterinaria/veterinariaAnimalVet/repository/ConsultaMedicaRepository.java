package com.veterinaria.veterinariaAnimalVet.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.veterinaria.veterinariaAnimalVet.model.ConsultaMedica;

public interface ConsultaMedicaRepository extends JpaRepository<ConsultaMedica, Long> {
    Optional<ConsultaMedica> findByCitaId(Long citaId);
    java.util.List<ConsultaMedica> findByVeterinario_Id(Long veterinarioId);
    java.util.List<ConsultaMedica> findByMascotaIdOrderByFechaAtencionDesc(Long mascotaId);
}
