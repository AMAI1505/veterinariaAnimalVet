package com.veterinaria.veterinariaAnimalVet.controller;

import com.veterinaria.veterinariaAnimalVet.model.*;
import com.veterinaria.veterinariaAnimalVet.repository.CitaRepository;
import com.veterinaria.veterinariaAnimalVet.repository.ConsultaMedicaRepository;
import com.veterinaria.veterinariaAnimalVet.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/atenciones")
@PreAuthorize("hasRole('PERSONAL_VETERINARIA')")
public class AtencionApiController {
    private final CitaRepository citas; private final ConsultaMedicaRepository consultas; private final UsuarioRepository usuarios;
    public AtencionApiController(CitaRepository citas,ConsultaMedicaRepository consultas,UsuarioRepository usuarios){this.citas=citas;this.consultas=consultas;this.usuarios=usuarios;}

    @GetMapping("/{citaId}") @PreAuthorize("@usuarioPermiso.esVeterinario(authentication)") @Transactional(readOnly=true)
    public AtencionContext get(@PathVariable Long citaId,@AuthenticationPrincipal Usuario auth){Cita cita=cita(citaId,auth);ConsultaMedica c=consultas.findByCitaId(citaId).orElse(null);
        Cliente tutor = cita.getMascota().getCliente();
        return new AtencionContext(new CitaInfo(cita.getId(),cita.getFechaHora(),cita.getMotivo(),cita.getEstado(),cita.getMascota().getId(),cita.getMascota().getNombre(),cita.getMascota().getEspecie(),cita.getMascota().getSexo(),cita.getMascota().getEsterilizado(),cita.getMascota().getColor(),cita.getMascota().getRaza(),cita.getMascota().getPeso(),tutor.getId(),tutor.getNombre(),tutor.getDni(),tutor.getTelefono()),c==null?null:AtencionDto.from(c));}

    @GetMapping("/mascota/{mascotaId}/historial") @PreAuthorize("@usuarioPermiso.esVeterinario(authentication)") @Transactional(readOnly=true)
    public java.util.List<HistorialDto> historial(@PathVariable Long mascotaId) {
        return citas.findByMascota_IdOrderByFechaHoraDesc(mascotaId).stream()
                .filter(cita -> !esCitaGrooming(cita))
                .map(cita -> {
            ConsultaMedica consulta = consultas.findByCitaId(cita.getId()).orElse(null);
            return new HistorialDto(cita.getId(), cita.getFechaHora(), consulta == null ? null : consulta.getFechaAtencion(),
                    consulta == null ? null : consulta.getDiagnostico(), consulta == null ? null : consulta.getTratamiento(),
                    consulta == null ? null : consulta.getExploracionClinica(), consulta == null ? null : consulta.getPesoKg(),
                    consulta == null ? null : consulta.getIndicacionesTutor(), consulta == null ? null : consulta.getProximoControl(),
                    nombreVeterinario(cita, consulta),
                    cita.getMotivo(), cita.getEstado(), consulta != null);
        }).toList();
    }

    private boolean esCitaGrooming(Cita cita) {
        boolean motivoGrooming = cita.getMotivo() != null && cita.getMotivo().trim().equalsIgnoreCase("Grooming");
        boolean asignadaAEstilista = cita.getVeterinario() != null
                && "ESTILISTA".equalsIgnoreCase(cita.getVeterinario().getTipoPersonal());
        return motivoGrooming || asignadaAEstilista;
    }

    private String nombreVeterinario(Cita cita, ConsultaMedica consulta) {
        if (consulta != null && consulta.getVeterinario() != null) return consulta.getVeterinario().getNombreCompleto();
        if (consulta != null && consulta.getNombreVeterinario() != null) return consulta.getNombreVeterinario();
        if (cita.getVeterinario() != null) return cita.getVeterinario().getNombreCompleto();
        return cita.getNombreProfesional() == null ? "Sin asignar" : cita.getNombreProfesional();
    }

    @GetMapping("/mi-horario") @Transactional(readOnly=true)
    public java.util.List<HorarioDto> miHorario(@AuthenticationPrincipal Usuario auth) {
        Usuario vet=usuarios.findByUsername(auth.getUsername()).orElseThrow();
        return vet.getTurnos().stream().map(t -> new HorarioDto(t.getDiaSemana(), t.getHoraInicio(), t.getHoraFin(), t.getHoraDescansoInicio(), t.getDuracionDescansoMinutos())).toList();
    }

    @PostMapping("/{citaId}") @PreAuthorize("@usuarioPermiso.esVeterinario(authentication)") @Transactional
    public AtencionDto guardar(@PathVariable Long citaId,@Valid @RequestBody AtencionRequest request,@AuthenticationPrincipal Usuario auth){
        if (request.proximoControl() != null && request.proximoControl().isBefore(LocalDate.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de próximo control no puede estar en el pasado.");
        Cita cita=cita(citaId,auth);if(consultas.findByCitaId(citaId).isPresent()||terminal(cita.getEstado()))throw new ResponseStatusException(HttpStatus.CONFLICT,"La cita ya fue atendida o está cancelada.");
        Usuario vet=usuarios.findByUsername(auth.getUsername()).orElseThrow();ConsultaMedica c=new ConsultaMedica();c.setCita(cita);c.setMascota(cita.getMascota());c.setTutor(cita.getMascota().getCliente());c.setVeterinario(vet);c.setNombreVeterinario(vet.getNombreCompleto());
        c.setPesoKg(request.pesoKg());c.setExploracionClinica(request.exploracionClinica());c.setDiagnostico(request.diagnostico().trim());c.setTratamiento(request.tratamiento().trim());c.setIndicacionesTutor(request.indicacionesTutor());c.setProximoControl(request.proximoControl());
        consultas.save(c);cita.setEstado("FINALIZADA");citas.save(cita);return AtencionDto.from(c);}

    private Cita cita(Long id,Usuario auth){Cita cita=citas.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Cita no encontrada."));
        if(cita.getVeterinario()==null||!cita.getVeterinario().getId().equals(auth.getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Solo puedes atender las citas asignadas a tu usuario.");return cita;}
    private boolean terminal(String s){return "FINALIZADA".equalsIgnoreCase(s)||"COMPLETADA".equalsIgnoreCase(s)||"CANCELADA".equalsIgnoreCase(s);}
    public record AtencionRequest(@DecimalMin("0.01") @DecimalMax("99999.99") BigDecimal pesoKg,@Size(max=4000) String exploracionClinica,
            @NotBlank @Size(max=4000) String diagnostico,@NotBlank @Size(max=4000) String tratamiento,
            @Size(max=4000) String indicacionesTutor,LocalDate proximoControl){ }
    public record CitaInfo(Long id,LocalDateTime fechaHora,String motivo,String estado,Long mascotaId,String mascota,String especie,String sexo,Boolean esterilizado,String color,String raza,String pesoRegistrado,Long tutorId,String tutor,String dniTutor,String telefono){ }
    public record AtencionDto(Long id,BigDecimal pesoKg,String exploracionClinica,String diagnostico,String tratamiento,String indicacionesTutor,LocalDate proximoControl,LocalDateTime fechaAtencion){
        static AtencionDto from(ConsultaMedica c){return new AtencionDto(c.getId(),c.getPesoKg(),c.getExploracionClinica(),c.getDiagnostico(),c.getTratamiento(),c.getIndicacionesTutor(),c.getProximoControl(),c.getFechaAtencion());}}
    public record AtencionContext(CitaInfo cita,AtencionDto atencion){ }
    public record HistorialDto(Long id,LocalDateTime citaFecha,LocalDateTime fechaAtencion,String diagnostico,String tratamiento,String exploracionClinica,BigDecimal pesoKg,String indicacionesTutor,LocalDate proximoControl,String veterinario,String motivo,String estado,boolean atencionRegistrada) { }
    public record HorarioDto(String diaSemana,java.time.LocalTime horaInicio,java.time.LocalTime horaFin,java.time.LocalTime horaDescansoInicio,Integer duracionDescansoMinutos) { }
}
