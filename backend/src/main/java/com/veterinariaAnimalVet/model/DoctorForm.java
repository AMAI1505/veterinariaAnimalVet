package com.veterinaria.veterinariaAnimalVet.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class DoctorForm {
    @NotBlank @Size(min = 3, max = 100) private String nombreCompleto;
    @NotBlank @Email @Size(max = 50) private String username;
    @NotBlank @Size(min = 6, max = 100) private String password;
    @NotBlank @Size(max = 20) private String telefono;
    @NotBlank private String especialidad;
    private List<TurnoForm> turnos = new ArrayList<>();
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public List<TurnoForm> getTurnos() { return turnos; }
    public void setTurnos(List<TurnoForm> turnos) { this.turnos = turnos; }
}
