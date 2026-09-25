package com.veterinaria.veterinariaAnimalVet.model;

import jakarta.persistence.*;
import java.util.List;

public class Rol {

    private Long id;
    private String nombre;
    private String descripcion;
    private boolean puedeVer = true;
    private boolean puedeCrear = false;
    private boolean puedeEditar = false;
    private boolean puedeEliminar = false;
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