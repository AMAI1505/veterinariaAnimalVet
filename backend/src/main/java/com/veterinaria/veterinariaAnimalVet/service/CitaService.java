package com.veterinaria.veterinariaAnimalVet.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import com.veterinaria.veterinariaAnimalVet.repository.UsuarioRepository;
import com.veterinaria.veterinariaAnimalVet.repository.TurnoLaboralRepository;

import com.veterinaria.veterinariaAnimalVet.model.Cita;
import com.veterinaria.veterinariaAnimalVet.repository.CitaRepository;

@Service
public class CitaService {
    private static final ZoneId ZONA_VETERINARIA = ZoneId.of("America/Lima");

    private final CitaRepository repo;
    private final UsuarioRepository usuarios;
    private final TurnoLaboralRepository turnos;

    public CitaService(CitaRepository repo, UsuarioRepository usuarios, TurnoLaboralRepository turnos) {
        this.repo = repo;
        this.usuarios = usuarios;
        this.turnos = turnos;
    }

    public List<Cita> findAll() {
        return repo.findAll();
    }

    public List<Cita> findByOwnerEmail(String correo) { return repo.findByOwnerEmail(correo); }

    @Transactional
    public Cita save(Cita cita) {
        LocalDateTime limiteAnticipacion = LocalDateTime.now(ZONA_VETERINARIA).plusMinutes(30);
        if (cita.getFechaHora() == null || cita.getFechaHora().toLocalDate().isBefore(limiteAnticipacion.toLocalDate())) {
            throw new IllegalArgumentException("No se pueden agendar citas en fechas pasadas.");
        }
        if (cita.getFechaHora().isBefore(limiteAnticipacion)) {
            throw new IllegalArgumentException("La cita debe iniciar al menos 30 minutos después de la hora actual.");
        }
        if (cita.getVeterinario() == null || cita.getVeterinario().getId() == null) {
            throw new IllegalArgumentException("Selecciona un veterinario activo válido.");
        }
        {
            var veterinario = usuarios.findPersonalForUpdate(cita.getVeterinario().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Selecciona un veterinario activo válido."));
            cita.setVeterinario(veterinario);
            LocalDateTime inicio = cita.getFechaHora();
            LocalDateTime fin = inicio.plusMinutes(30);
            String dia = nombreDia(inicio.getDayOfWeek());
            var turno = turnos.findByVeterinarioIdAndDiaSemana(veterinario.getId(), dia).orElse(null);
            LocalTime hora = inicio.toLocalTime();
            if (turno == null || hora.isBefore(turno.getHoraInicio())
                    || hora.plusMinutes(30).isAfter(turno.getHoraFin())
                    || (turno.getHoraDescansoInicio()!=null && hora.isBefore(turno.getHoraDescansoInicio().plusMinutes(turno.getDuracionDescansoMinutos())) && hora.plusMinutes(30).isAfter(turno.getHoraDescansoInicio()))
                    || java.time.Duration.between(turno.getHoraInicio(), hora).toMinutes() % 30 != 0
                    || inicio.getSecond() != 0 || inicio.getNano() != 0) {
                throw new IllegalArgumentException("El horario seleccionado está fuera del turno laboral del veterinario o no respeta intervalos de 30 minutos.");
            }
            if (repo.existsConflictingCita(veterinario.getId(), cita.getId(), inicio, fin)) {
                throw new IllegalArgumentException("Ese horario acaba de ser reservado para el veterinario seleccionado. Elige otro horario.");
            }
        }
        return repo.save(cita);
    }

    private String nombreDia(java.time.DayOfWeek dia) {
        return switch (dia) { case MONDAY -> "LUNES"; case TUESDAY -> "MARTES"; case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES"; case FRIDAY -> "VIERNES"; case SATURDAY -> "SABADO"; case SUNDAY -> "DOMINGO"; };
    }

    public List<Cita> findCitasActivasDelDia(Long veterinarioId, LocalDate fecha) {
        return repo.findCitasActivasDelDia(veterinarioId, fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
    }

    public Optional<Cita> findById(Long id) {
        return repo.findById(id);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public boolean existsById(Long id) {
        return repo.existsById(id);
    }

    public boolean existsByFechaHora(java.time.LocalDateTime fechaHora) {
        return repo.existsByFechaHora(fechaHora);
    }

    public boolean existsByFechaHoraAndIdNot(java.time.LocalDateTime fechaHora, Long id) {
        return repo.existsByFechaHoraAndIdNot(fechaHora, id);
    }
}
