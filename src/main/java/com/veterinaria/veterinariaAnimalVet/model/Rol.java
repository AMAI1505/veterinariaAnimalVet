package com.veterinaria.veterinariaAnimalVet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(min=3, max = 30, message = "El nombre debe tener entre 3 y 30 caracteres")
    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    @Size(max = 100, message = "La descripción no puede exceder 100 caracteres")
    @Column(length = 100)
    private String descripcion;

    @Column(nullable = false)
    private boolean puedeVer = true;

    @Column(nullable = false)
    private boolean puedeCrear = false;

    @Column(nullable = false)
    private boolean puedeEditar = false;

    @Column(nullable = false)
    private boolean puedeEliminar = false;

    @OneToMany(mappedBy = "rol", fetch = FetchType.LAZY)
    private List<Usuario> usuarios;

    public Rol() {
    }

    public Rol(String nombre, String descripcion, boolean puedeVer, boolean puedeCrear,
            boolean puedeEditar, boolean puedeEliminar) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.puedeVer = puedeVer;
        this.puedeCrear = puedeCrear;
        this.puedeEditar = puedeEditar;
        this.puedeEliminar = puedeEliminar;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isPuedeVer() {
        return puedeVer;
    }

    public void setPuedeVer(boolean puedeVer) {
        this.puedeVer = puedeVer;
    }

    public boolean isPuedeCrear() {
        return puedeCrear;
    }

    public void setPuedeCrear(boolean puedeCrear) {
        this.puedeCrear = puedeCrear;
    }

    public boolean isPuedeEditar() {
        return puedeEditar;
    }

    public void setPuedeEditar(boolean puedeEditar) {
        this.puedeEditar = puedeEditar;
    }

    public boolean isPuedeEliminar() {
        return puedeEliminar;
    }

    public void setPuedeEliminar(boolean puedeEliminar) {
        this.puedeEliminar = puedeEliminar;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}
