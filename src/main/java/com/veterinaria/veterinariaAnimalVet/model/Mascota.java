package com.veterinaria.veterinariaAnimalVet.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "mascotas")
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la mascota es obligatorio")
    @Size(min=3, max = 40, message = "El nombre debe tener entre 3 y 40 caracteres")
    @Column(nullable = false, length = 40)
    private String nombre;

    @NotBlank(message = "La especie es obligatoria")
    @Size(min=3, max = 30, message = "La especie debe tener entre 3 y 30 caracteres")
    @Column(nullable = false, length = 30)
    private String especie;

    @NotBlank(message = "La raza es obligatoria")
    @Size(min=3, max = 40, message = "La raza debe tener entre 3 y 40 caracteres")
    @Column(nullable = false, length = 40)
    private String raza;

    @NotBlank(message = "El peso es obligatorio")
    @Size(max = 6, message = "El peso no puede exceder los 6 caracteres")
    @Column(nullable = false, length = 6)
    private String peso;

    @NotBlank(message = "El color es obligatorio")
    @Size(min=3, max = 30, message = "El color debe tener entre 3 y 30 caracteres")
    @Column(nullable = false, length = 30)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @NotNull(message = "Debes seleccionar un dueño para la mascota")
    private Cliente cliente;

    @OneToMany(mappedBy = "mascota", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cita> citas;
    
    public Mascota() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<Cita> getCitas() {
        return citas;
    }

    public void setCitas(List<Cita> citas) {
        this.citas = citas;
    }
    
}
