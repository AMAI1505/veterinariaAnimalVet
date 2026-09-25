package com.veterinaria.veterinariaAnimalVet.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "citas")
public class Cita {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "La fecha y hora son obligatorias")
    @Column(nullable = false, unique = true)
    private LocalDateTime fechaHora;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mascota_id", nullable = false)
    @NotNull(message = "La mascota es obligatoria")
    private Mascota mascota;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @NotNull(message = "El dueño es obligatorio")
    private Cliente cliente;
    
    @NotBlank(message = "El motivo es obligatorio")
    @Size(min=3, max = 100, message = "El motivo debe tener entre 3 y 100 caracteres")
    @Column(nullable = false, length=100)
    private String motivo;
    
    @NotBlank(message = "El estado de la cita es obligatorio")
    private String estado;

    public Cita() {
    }

    public Cita(Long id, @NotNull(message = "La fecha y hora son obligatorias") LocalDateTime fechaHora,
            @NotNull(message = "La mascota es obligatoria") Mascota mascota,
            @NotNull(message = "El dueño es obligatorio") Cliente cliente,
            @NotBlank(message = "El motivo es obligatorio") @Size(min = 3, max = 100, message = "El motivo debe tener entre 3 y 100 caracteres") String motivo,
            @NotBlank(message = "El estado de la cita es obligatorio") String estado) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.mascota = mascota;
        this.cliente = cliente;
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

    public Mascota getMascota() {
        return mascota;
    }

    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
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