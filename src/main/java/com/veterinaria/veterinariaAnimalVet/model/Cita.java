package com.veterinaria.veterinariaAnimalVet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
public class Cita {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "La fecha y hora son obligatorias")
    @Column(nullable = false)
    private LocalDateTime fechaHora;
    
    // Relación ManyToOne: Muchas citas pertenecen a una Mascota
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mascota_id", nullable = false)
    @NotNull(message = "La mascota es obligatoria")
    private Mascota mascota;
    
    // Relación ManyToOne: Muchas citas pertenecen a un Cliente (Dueño)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @NotNull(message = "El dueño es obligatorio")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "veterinario_id")
    private Usuario veterinario;
    
    @NotBlank(message = "El motivo es obligatorio")
    @Size(min=3, max = 100, message = "El motivo debe tener entre 3 y 100 caracteres")
    @Column(nullable = false, length=100)
    private String motivo;
    
    @NotBlank(message = "El estado de la cita es obligatorio")
    private String estado;

    public Cita() {
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

    public Mascota getMascota() {
        return mascota;
    }

    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Usuario getVeterinario() { return veterinario; }
    public void setVeterinario(Usuario veterinario) { this.veterinario = veterinario; }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
}
