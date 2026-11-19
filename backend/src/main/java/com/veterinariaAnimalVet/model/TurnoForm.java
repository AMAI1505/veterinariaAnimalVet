package com.veterinaria.veterinariaAnimalVet.model;

import java.time.LocalTime;

public class TurnoForm {
    private String diaSemana;
    private boolean activo;
    private LocalTime horaInicio = LocalTime.of(9, 0);
    private LocalTime horaFin = LocalTime.of(17, 0);
    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
}
