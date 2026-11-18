package com.veterinaria.veterinariaAnimalVet.controller;

import com.veterinaria.veterinariaAnimalVet.model.Cita;
import com.veterinaria.veterinariaAnimalVet.model.Mascota;
import com.veterinaria.veterinariaAnimalVet.model.Usuario;
import com.veterinaria.veterinariaAnimalVet.repository.UsuarioRepository;
import com.veterinaria.veterinariaAnimalVet.service.CitaService;
import com.veterinaria.veterinariaAnimalVet.service.MascotaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaApiController {
    private final CitaService citas;
    private final MascotaService mascotas;
    private final UsuarioRepository usuarios;

    public CitaApiController(CitaService citas, MascotaService mascotas, UsuarioRepository usuarios) {
        this.citas = citas; this.mascotas = mascotas; this.usuarios = usuarios;
    }

    @GetMapping @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PERSONAL_VETERINARIA') or @usuarioPermiso.puedeVer(authentication)") @org.springframework.transaction.annotation.Transactional(readOnly=true)
    public List<CitaDto> listar(@org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario) {
        boolean personal = usuario.getRol() != null && "PERSONAL_VETERINARIA".equals(usuario.getRol().getNombre());
        return citas.findAll().stream().filter(c -> !personal ||
                (c.getVeterinario() != null && c.getVeterinario().getId().equals(usuario.getId()))).map(CitaDto::from).toList();
    }

    @GetMapping("/mis-citas") @PreAuthorize("hasRole('CLIENTE')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<CitaDto> misCitas(@org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario) {
        return citas.findByOwnerEmail(usuario.getUsername()).stream().map(CitaDto::from).toList();
    }

    @GetMapping("/{id}") @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PERSONAL_VETERINARIA') or @usuarioPermiso.puedeVer(authentication)") @org.springframework.transaction.annotation.Transactional(readOnly=true)
    public CitaDto obtener(@PathVariable Long id, @org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario) {
        Cita cita = citas.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada."));
        boolean personal = usuario.getRol() != null && "PERSONAL_VETERINARIA".equals(usuario.getRol().getNombre());
        if (personal &&
                (cita.getVeterinario() == null || !cita.getVeterinario().getId().equals(usuario.getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a esta cita.");
        }
        return CitaDto.from(cita);
    }

    @GetMapping("/veterinarios") @PreAuthorize("hasRole('ADMINISTRADOR') or @usuarioPermiso.puedeVer(authentication)")
    public List<VeterinarioDto> veterinarios() { return usuarios.findPersonalActivoParaCitas().stream().map(v -> new VeterinarioDto(v.getId(), v.getNombreCompleto(), v.getUsername(), v.getEspecialidad())).toList(); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMINISTRADOR') or @usuarioPermiso.puedeCrear(authentication)")
    public CitaDto crear(@Valid @RequestBody CitaRequest request) { return CitaDto.from(citas.save(armarCita(request, null))); }

    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CitaDto editar(@PathVariable Long id, @Valid @RequestBody CitaRequest request) {
        if (citas.findById(id).isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return CitaDto.from(citas.save(armarCita(request, id)));
    }

    @PostMapping("/{id}/finalizar-grooming")
    @PreAuthorize("@usuarioPermiso.esGroomi(authentication)")
    @org.springframework.transaction.annotation.Transactional
    public CitaDto finalizarGrooming(@PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Usuario usuario) {
        Cita cita = citas.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada."));
        if (usuario.getTipoPersonal() == null || !"ESTILISTA".equals(usuario.getTipoPersonal())
                || cita.getVeterinario() == null || !cita.getVeterinario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes finalizar las citas de grooming asignadas a tu usuario.");
        }
        if ("CANCELADA".equalsIgnoreCase(cita.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede finalizar una cita cancelada.");
        }
        cita.setEstado("FINALIZADA");
        return CitaDto.from(citas.save(cita));
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(@PathVariable Long id) { if (!citas.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND); citas.deleteById(id); }

    private Cita armarCita(CitaRequest request, Long id) {
        Mascota mascota = mascotas.findById(request.mascotaId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La mascota seleccionada no existe."));
        Usuario veterinario = usuarios.findPersonalActivoParaCitas().stream().filter(v -> v.getId().equals(request.veterinarioId())).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El veterinario seleccionado no está activo."));
        Cita cita = id == null ? new Cita() : citas.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada."));
        cita.setMascota(mascota); cita.setCliente(mascota.getCliente());
        cita.setVeterinario(veterinario); cita.setFechaHora(request.fechaHora()); cita.setMotivo(request.motivo());
        cita.setNombreProfesional(veterinario.getNombreCompleto());
        if (id == null) cita.setEstado("Programada");
        return cita;
    }

    public record CitaRequest(@NotNull LocalDateTime fechaHora, @NotNull Long mascotaId, @NotNull Long veterinarioId,
                              @NotBlank @Size(min = 3, max = 100) @Pattern(regexp = "(?i)^(consulta general|vacunación|desparasitación|procedimiento quirúrgico|grooming)$") String motivo) { }
    public record CitaDto(Long id, LocalDateTime fechaHora, Long mascotaId, String mascota, String tutor,
                          String dniTutor, String telefonoTutor, Long veterinarioId, String veterinario, String motivo, String estado) {
        static CitaDto from(Cita c) { return new CitaDto(c.getId(), c.getFechaHora(), c.getMascota().getId(), c.getMascota().getNombre(),
                c.getMascota().getCliente().getNombre(), c.getMascota().getCliente().getDni(), c.getMascota().getCliente().getTelefono(),
                c.getVeterinario() == null ? null : c.getVeterinario().getId(),
                c.getVeterinario() == null ? (c.getNombreProfesional() == null ? "Sin asignar" : c.getNombreProfesional()) : c.getVeterinario().getNombreCompleto(), c.getMotivo(), c.getEstado()); }
    }
    public record VeterinarioDto(Long id, String nombre, String correo, String especialidad) { }
}
