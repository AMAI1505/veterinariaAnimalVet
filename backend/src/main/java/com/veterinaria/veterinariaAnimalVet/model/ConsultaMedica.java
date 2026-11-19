package com.veterinaria.veterinariaAnimalVet.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "consultas_medicas")
public class ConsultaMedica {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false, unique = true)
    private Cita cita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente tutor;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "veterinario_id")
    private Usuario veterinario;

    @Column(name = "nombre_veterinario", length = 100)
    private String nombreVeterinario;

    @DecimalMin(value = "0.01", message = "El peso debe ser mayor que cero")
    @Column(precision = 7, scale = 2)
    private BigDecimal pesoKg;

    @Size(max = 4000)
    @Column(length = 4000)
    private String exploracionClinica;

    @NotBlank(message = "El diagnóstico es obligatorio")
    @Size(max = 4000)
    @Column(nullable = false, length = 4000)
    private String diagnostico;

    @NotBlank(message = "El tratamiento, con fármacos, dosis, frecuencia y duración, es obligatorio")
    @Size(max = 4000)
    @Column(nullable = false, length = 4000)
    private String tratamiento;

    @Size(max = 4000)
    @Column(length = 4000)
    private String indicacionesTutor;

    private LocalDate proximoControl;

    @Column(nullable = false)
    private LocalDateTime fechaAtencion = LocalDateTime.now();

    public Long getId() { return id; }
    public Cita getCita() { return cita; }
    public void setCita(Cita cita) { this.cita = cita; }
    public Mascota getMascota() { return mascota; }
    public void setMascota(Mascota mascota) { this.mascota = mascota; }
    public Cliente getTutor() { return tutor; }
    public void setTutor(Cliente tutor) { this.tutor = tutor; }
    public Usuario getVeterinario() { return veterinario; }
    public void setVeterinario(Usuario veterinario) { this.veterinario = veterinario; }
    public String getNombreVeterinario() { return nombreVeterinario; }
    public void setNombreVeterinario(String nombreVeterinario) { this.nombreVeterinario = nombreVeterinario; }
    public BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg = pesoKg; }
    public String getExploracionClinica() { return exploracionClinica; }
    public void setExploracionClinica(String exploracionClinica) { this.exploracionClinica = exploracionClinica; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    public String getIndicacionesTutor() { return indicacionesTutor; }
    public void setIndicacionesTutor(String indicacionesTutor) { this.indicacionesTutor = indicacionesTutor; }
    public LocalDate getProximoControl() { return proximoControl; }
    public void setProximoControl(LocalDate proximoControl) { this.proximoControl = proximoControl; }
    public LocalDateTime getFechaAtencion() { return fechaAtencion; }
}
