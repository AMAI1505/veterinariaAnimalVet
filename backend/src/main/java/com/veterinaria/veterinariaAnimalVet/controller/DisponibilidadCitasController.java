package com.veterinaria.veterinariaAnimalVet.controller;

import com.veterinaria.veterinariaAnimalVet.model.Cita;
import com.veterinaria.veterinariaAnimalVet.repository.UsuarioRepository;
import com.veterinaria.veterinariaAnimalVet.repository.TurnoLaboralRepository;
import com.veterinaria.veterinariaAnimalVet.service.CitaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/citas")
public class DisponibilidadCitasController {
    private static final ZoneId ZONA_VETERINARIA = ZoneId.of("America/Lima");
    private final CitaService citas;
    private final UsuarioRepository usuarios;
    private final TurnoLaboralRepository turnos;

    public DisponibilidadCitasController(CitaService citas, UsuarioRepository usuarios, TurnoLaboralRepository turnos) {
        this.citas = citas;
        this.usuarios = usuarios;
        this.turnos = turnos;
    }

    @GetMapping("/disponibilidad")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('RECEPCION')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> disponibilidad(@RequestParam Long veterinarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Long excluirCitaId) {
        LocalDateTime limiteAnticipacion = LocalDateTime.now(ZONA_VETERINARIA).plusMinutes(30);
        if (fecha.isBefore(limiteAnticipacion.toLocalDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se permiten fechas pasadas.");
        }
        if (usuarios.findPersonalActivoParaCitas().stream().noneMatch(v -> v.getId().equals(veterinarioId))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario no encontrado.");
        }
        List<Cita> citasExistentes = citas.findCitasActivasDelDia(veterinarioId, fecha).stream()
                .filter(c -> excluirCitaId == null || !excluirCitaId.equals(c.getId())).toList();
        List<String> ocupados = citasExistentes.stream()
                .map(c -> c.getFechaHora().toLocalTime().toString().substring(0, 5)).toList();
        List<String> disponibles = new ArrayList<>();
        var turno = turnos.findByVeterinarioIdAndDiaSemana(veterinarioId, nombreDia(fecha.getDayOfWeek())).orElse(null);
        if (turno != null) {
            for (LocalTime hora = turno.getHoraInicio(); !hora.plusMinutes(30).isAfter(turno.getHoraFin()); hora = hora.plusMinutes(30)) {
                if (turno.getHoraDescansoInicio()!=null && hora.isBefore(turno.getHoraDescansoInicio().plusMinutes(turno.getDuracionDescansoMinutos())) && hora.plusMinutes(30).isAfter(turno.getHoraDescansoInicio())) continue;
                LocalDateTime inicio = LocalDateTime.of(fecha, hora);
                boolean yaOcupado = citasExistentes.stream().anyMatch(c -> {
                    LocalDateTime existenteInicio = c.getFechaHora();
                    LocalDateTime existenteFin = existenteInicio.plusMinutes(30);
                    return inicio.isBefore(existenteFin) && inicio.plusMinutes(30).isAfter(existenteInicio);
                });
                if (!yaOcupado && !inicio.isBefore(limiteAnticipacion)) {
                    disponibles.add(hora.toString().substring(0, 5));
                }
            }
        }
        return Map.of("ocupados", ocupados, "disponibles", disponibles,
                "turno", turno == null ? "Sin turno laboral" : turno.getHoraInicio() + "–" + turno.getHoraFin());
    }

    private String nombreDia(java.time.DayOfWeek dia) {
        return switch (dia) { case MONDAY -> "LUNES"; case TUESDAY -> "MARTES"; case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES"; case FRIDAY -> "VIERNES"; case SATURDAY -> "SABADO"; case SUNDAY -> "DOMINGO"; };
    }
}
