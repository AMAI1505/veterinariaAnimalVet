package com.veterinaria.veterinariaAnimalVet.controller;

import com.veterinaria.veterinariaAnimalVet.model.Cliente;
import com.veterinaria.veterinariaAnimalVet.model.Mascota;
import com.veterinaria.veterinariaAnimalVet.service.ClienteService;
import com.veterinaria.veterinariaAnimalVet.service.MascotaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaApiController {
    private final MascotaService mascotas;
    private final ClienteService clientes;
    public MascotaApiController(MascotaService mascotas, ClienteService clientes) { this.mascotas = mascotas; this.clientes = clientes; }

    @GetMapping @PreAuthorize("@usuarioPermiso.puedeVerMascotas(authentication)") @Transactional(readOnly=true)
    public List<MascotaDto> listar() { return mascotas.findAll().stream().map(MascotaDto::from).toList(); }

    @GetMapping("/{id}") @PreAuthorize("@usuarioPermiso.puedeVerMascotas(authentication)") @Transactional(readOnly=true)
    public MascotaDto obtener(@PathVariable Long id) {
        return mascotas.findById(id).map(MascotaDto::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada."));
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMINISTRADOR') or @usuarioPermiso.puedeCrear(authentication)") @Transactional
    public MascotaDto crear(@Valid @RequestBody MascotaRequest request) { return MascotaDto.from(mascotas.save(request.toEntity(guardarTutor(request)))); }

    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMINISTRADOR')") @Transactional
    public MascotaDto editar(@PathVariable Long id, @Valid @RequestBody MascotaRequest request) {
        Mascota mascota = mascotas.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        mascota.setNombre(request.nombre()); mascota.setEspecie(request.especie()); mascota.setSexo(request.sexo()); mascota.setEsterilizado(request.esterilizado()); mascota.setRaza(request.raza());
        mascota.setPeso(request.peso()); mascota.setColor(request.color()); mascota.setCliente(guardarTutor(request));
        return MascotaDto.from(mascotas.save(mascota));
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(@PathVariable Long id) { if (!mascotas.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND); mascotas.deleteById(id); }

    private Cliente guardarTutor(MascotaRequest request) {
        Cliente tutor = clientes.findByDni(request.dniTutor()).orElseGet(Cliente::new);
        tutor.setDni(request.dniTutor());
        tutor.setNombre(request.tutor().trim());
        tutor.setTelefono(request.telefonoTutor());
        if (request.correoTutor()!=null && !request.correoTutor().isBlank()) tutor.setCorreo(request.correoTutor().trim().toLowerCase(java.util.Locale.ROOT));
        return clientes.save(tutor);
    }

    public record MascotaRequest(@NotBlank @Size(min = 3, max = 40) String nombre,
            @NotBlank @Size(min = 3, max = 30) String especie, @NotBlank @Pattern(regexp = "^(HEMBRA|MACHO)$") String sexo,
            @NotNull Boolean esterilizado, @NotBlank @Size(min = 3, max = 40) String raza,
            @NotBlank @Pattern(regexp = "^(?:0\\.[0-9]{1,2}|[1-9][0-9]{0,2}(?:\\.[0-9]{1,2})?)$") String peso,
            @NotBlank @Size(min = 3, max = 30) String color,
            @NotBlank @Pattern(regexp = "^[0-9]{8}$") String dniTutor,
            @NotBlank @Size(min = 3, max = 70) String tutor,
            @NotBlank @Pattern(regexp = "^[0-9]{9}$") String telefonoTutor,
            @Email @Size(max = 100) String correoTutor) {
        Mascota toEntity(Cliente cliente) { Mascota m = new Mascota(); m.setNombre(nombre); m.setEspecie(especie); m.setSexo(sexo); m.setEsterilizado(esterilizado); m.setRaza(raza); m.setPeso(peso); m.setColor(color); m.setCliente(cliente); return m; }
    }
    public record MascotaDto(Long id, String nombre, String especie, String sexo, Boolean esterilizado, String raza, String peso, String color,
            String tutor, String dniTutor, String telefonoTutor, String correoTutor) {
        static MascotaDto from(Mascota m) { return new MascotaDto(m.getId(), m.getNombre(), m.getEspecie(), m.getSexo(), m.getEsterilizado(), m.getRaza(), m.getPeso(), m.getColor(),
                m.getCliente().getNombre(), m.getCliente().getDni(), m.getCliente().getTelefono(), m.getCliente().getCorreo()); }
    }
}
