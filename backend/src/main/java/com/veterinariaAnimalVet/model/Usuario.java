package com.veterinaria.veterinariaAnimalVet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El usuario es obligatorio")
    @Size(min=3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Size(max = 100)
    @Column(name = "nombre_completo", length = 100)
    private String nombreCompleto;

    @Size(max = 20)
    @Column(length = 20)
    private String telefono;

    @Size(max = 60)
    @Column(length = 60)
    private String especialidad;

    @Column(name = "tipo_personal", length = 30)
    private String tipoPersonal;

    @OneToMany(mappedBy = "veterinario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TurnoLaboral> turnos = new ArrayList<>();

    @Column(nullable = false, length = 255)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "intentos_fallidos")
    private int intentosFallidos = 0;

    @Column(name = "fecha_desbloqueo")
    private LocalDateTime fechaDesbloqueo;

    @Column(name = "token_version", nullable = false, columnDefinition = "bigint default 0")
    private long tokenVersion = 0;

    public Usuario() {
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.getNombre()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (fechaDesbloqueo == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(fechaDesbloqueo);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public String getTipoPersonal() { return tipoPersonal; }
    public void setTipoPersonal(String tipoPersonal) { this.tipoPersonal = tipoPersonal; }
    public List<TurnoLaboral> getTurnos() { return turnos; }
    public void setTurnos(List<TurnoLaboral> turnos) { this.turnos = turnos; }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public LocalDateTime getFechaDesbloqueo() {
        return fechaDesbloqueo;
    }

    public void setFechaDesbloqueo(LocalDateTime fechaDesbloqueo) {
        this.fechaDesbloqueo = fechaDesbloqueo;
    }

    public long getTokenVersion() { return tokenVersion; }
    public void invalidateTokens() { tokenVersion++; }
}
