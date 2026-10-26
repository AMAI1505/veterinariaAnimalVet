package com.veterinaria.veterinariaAnimalVet.repository;

import com.veterinaria.veterinariaAnimalVet.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByMascotaIdOrderByFechaDesc(Long mascotaId);
}
