package com.veterinaria.veterinariaAnimalVet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

import com.veterinaria.veterinariaAnimalVet.model.Cita;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    @org.springframework.data.jpa.repository.Query("select c from Cita c where lower(c.cliente.correo) = lower(:correo) order by c.fechaHora")
    List<Cita> findByOwnerEmail(@org.springframework.data.repository.query.Param("correo") String correo);

    boolean existsByFechaHora(LocalDateTime fechaHora);
    boolean existsByFechaHoraAndIdNot(LocalDateTime fechaHora, Long id);
    List<Cita> findByMascota_IdOrderByFechaHoraDesc(Long mascotaId);
    List<Cita> findByVeterinario_Id(Long veterinarioId);

    @org.springframework.data.jpa.repository.Query("select c from Cita c where c.veterinario.id = :veterinarioId " +
            "and c.fechaHora >= :inicio and c.fechaHora < :fin " +
            "and upper(c.estado) in ('PROGRAMADA','PENDIENTE','CONFIRMADA','EN ESPERA') order by c.fechaHora")
    List<com.veterinaria.veterinariaAnimalVet.model.Cita> findCitasActivasDelDia(
            @org.springframework.data.repository.query.Param("veterinarioId") Long veterinarioId,
            @org.springframework.data.repository.query.Param("inicio") LocalDateTime inicio,
            @org.springframework.data.repository.query.Param("fin") LocalDateTime fin);

    @org.springframework.data.jpa.repository.Query("select count(c) > 0 from Cita c where c.veterinario.id = :veterinarioId " +
            "and (:citaId is null or c.id <> :citaId) and c.fechaHora < :fin and c.fechaHora > :inicio " +
            "and upper(c.estado) in ('PROGRAMADA','PENDIENTE','CONFIRMADA','EN ESPERA')")
    boolean existsConflictingCita(@org.springframework.data.repository.query.Param("veterinarioId") Long veterinarioId,
                                  @org.springframework.data.repository.query.Param("citaId") Long citaId,
                                  @org.springframework.data.repository.query.Param("inicio") LocalDateTime inicio,
                                  @org.springframework.data.repository.query.Param("fin") LocalDateTime fin);
}
