package com.veterinaria.veterinariaAnimalVet.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "turnos_laborales", uniqueConstraints = @UniqueConstraint(name = "uk_turno_vet_dia", columnNames = {"veterinario_id", "dia_semana"}))
public class TurnoLaboral {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Usuario veterinario;
    @Column(name = "dia_semana", nullable = false, length = 12)
    private String diaSemana;
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;
    @Column(name = "hora_descanso_inicio")
    private LocalTime horaDescansoInicio;
    @Column(name = "duracion_descanso_minutos")
    private Integer duracionDescansoMinutos;

    public Long getId() { return id; }
    public Usuario getVeterinario() { return veterinario; }
    public void setVeterinario(Usuario veterinario) { this.veterinario = veterinario; }
    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public LocalTime getHoraDescansoInicio() { return horaDescansoInicio; }
    public void setHoraDescansoInicio(LocalTime horaDescansoInicio) { this.horaDescansoInicio = horaDescansoInicio; }
    public Integer getDuracionDescansoMinutos() { return duracionDescansoMinutos; }
    public void setDuracionDescansoMinutos(Integer duracionDescansoMinutos) { this.duracionDescansoMinutos = duracionDescansoMinutos; }
}
