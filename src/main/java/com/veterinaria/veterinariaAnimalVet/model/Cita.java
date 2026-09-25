package com.veterinaria.veterinariaAnimalVet.model;

import java.time.LocalDateTime;

public class Cita {
    
    private Long id;
    private LocalDateTime fechaHora;
    private String motivo;
    private String estado;

    public Cita() {
    }

    public Cita(Long id, LocalDateTime fechaHora, String motivo, String estado) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
}