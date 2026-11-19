package com.veterinaria.veterinariaAnimalVet.controller;

import com.veterinaria.veterinariaAnimalVet.model.*;
import com.veterinaria.veterinariaAnimalVet.repository.CitaRepository;
import com.veterinaria.veterinariaAnimalVet.repository.ConsultaMedicaRepository;
import com.veterinaria.veterinariaAnimalVet.repository.RolRepository;
import com.veterinaria.veterinariaAnimalVet.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/doctores")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminApiController {
    private static final Set<String> DIAS = Set.of("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO");
    private static final Set<String> ESPECIALIDADES = Set.of("Medicina General", "Cirugía", "Dermatología", "Odontología", "Animales Exóticos");
    private final UsuarioService usuarios;
    private final RolRepository roles;
    private final CitaRepository citas;
    private final ConsultaMedicaRepository consultas;
    public AdminApiController(UsuarioService usuarios, RolRepository roles, CitaRepository citas, ConsultaMedicaRepository consultas) {
        this.usuarios=usuarios; this.roles=roles; this.citas=citas; this.consultas=consultas;
    }

    @GetMapping @Transactional(readOnly = true) public List<DoctorDto> listar() { return doctores().stream().map(DoctorDto::from).toList(); }

    @GetMapping("/{id}") @Transactional(readOnly = true)
    public DoctorDto obtener(@PathVariable Long id) { return DoctorDto.from(personal(id)); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public DoctorDto crear(@Valid @RequestBody DoctorRequest request) {
        String email=request.correo().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByUsername(email)) throw new ResponseStatusException(HttpStatus.CONFLICT,"El correo ya está registrado.");
        String tipo=request.tipoPersonal()==null||request.tipoPersonal().isBlank()?"VETERINARIO":request.tipoPersonal().toUpperCase(Locale.ROOT);
        if (!Set.of("VETERINARIO","ESTILISTA").contains(tipo)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Selecciona un tipo de personal válido.");
        validarEspecialidad(tipo, request.especialidad()); validarTurnos(request.turnos());
        Usuario doctor=new Usuario(); doctor.setUsername(email); doctor.setNombreCompleto(request.nombreCompleto().trim());
        doctor.setTelefono(request.telefono().trim()); doctor.setEspecialidad(request.especialidad().trim()); doctor.setEnabled(true);
        doctor.setRol(roles.findByNombre("PERSONAL_VETERINARIA").orElseThrow()); doctor.setTipoPersonal(tipo); doctor.setPassword(request.password());
        doctor.setTurnos(mapTurnos(request.turnos(),doctor));
        return DoctorDto.from(usuarios.registrarUsuario(doctor));
    }

    @PutMapping("/{id}")
    @Transactional
    public DoctorDto actualizar(@PathVariable Long id, @Valid @RequestBody UpdatePersonalRequest request) {
        Usuario persona=personal(id);
        String email=request.correo().trim().toLowerCase(Locale.ROOT);
        usuarios.findByUsername(email).filter(existing -> !existing.getId().equals(id)).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"El correo ya está registrado.");
        });
        String tipo=request.tipoPersonal().toUpperCase(Locale.ROOT);
        if (!Set.of("VETERINARIO","ESTILISTA").contains(tipo)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Selecciona un tipo de personal válido.");
        validarEspecialidad(tipo,request.especialidad()); validarTurnos(request.turnos());
        persona.setUsername(email); persona.setNombreCompleto(request.nombreCompleto().trim()); persona.setTelefono(request.telefono().trim());
        persona.setTipoPersonal(tipo); persona.setEspecialidad(request.especialidad().trim());
        if (request.password()!=null && !request.password().isBlank()) {
            if (request.password().length()<10) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La contraseña debe tener al menos 10 caracteres.");
            persona.setPassword(usuarios.encodePassword(request.password()));
            persona.invalidateTokens();
        }
        reemplazarTurnos(persona,request.turnos());
        return DoctorDto.from(usuarios.save(persona));
    }

    @PutMapping("/{id}/turnos")
    @Transactional
    public DoctorDto actualizarTurnos(@PathVariable Long id,@Valid @RequestBody List<TurnoRequest> turnos) {
        Usuario doctor=personal(id); validarTurnos(turnos); reemplazarTurnos(doctor,turnos);
        return DoctorDto.from(usuarios.save(doctor));
    }

    private void reemplazarTurnos(Usuario doctor,List<TurnoRequest> turnos) {
        Map<String,TurnoLaboral> actuales=new HashMap<>();
        doctor.getTurnos().forEach(t->actuales.put(t.getDiaSemana(),t));
        for(TurnoRequest row:turnos){TurnoLaboral actual=actuales.get(row.diaSemana());if(!row.activo()){if(actual!=null)doctor.getTurnos().remove(actual);continue;}
            if(actual==null){actual=new TurnoLaboral();actual.setVeterinario(doctor);actual.setDiaSemana(row.diaSemana());doctor.getTurnos().add(actual);}
            actual.setHoraInicio(row.horaInicio());actual.setHoraFin(row.horaFin());actual.setHoraDescansoInicio(row.horaDescansoInicio());actual.setDuracionDescansoMinutos(row.duracionDescansoMinutos());}
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @Transactional
    public void eliminarCuenta(@PathVariable Long id) {
        Usuario persona=personal(id);
        String nombre=persona.getNombreCompleto();

        for (Cita cita : citas.findByVeterinario_Id(id)) {
            String estado=cita.getEstado()==null ? "" : cita.getEstado().trim().toUpperCase(Locale.ROOT);
            boolean pendiente=Set.of("PROGRAMADA","PENDIENTE","CONFIRMADA","EN ESPERA").contains(estado);
            if (pendiente) {
                cita.setNombreProfesional(null);
            } else if (cita.getNombreProfesional() == null || cita.getNombreProfesional().isBlank()) {
                cita.setNombreProfesional(nombre);
            }
            cita.setVeterinario(null);
        }
        for (ConsultaMedica consulta : consultas.findByVeterinario_Id(id)) {
            if (consulta.getNombreVeterinario() == null || consulta.getNombreVeterinario().isBlank()) {
                consulta.setNombreVeterinario(nombre);
            }
            consulta.setVeterinario(null);
        }
        citas.flush();
        consultas.flush();
        usuarios.deleteById(id);
    }

    private List<Usuario> doctores(){return usuarios.findAll().stream().filter(this::esPersonal).toList();}
    private Usuario personal(Long id){return usuarios.findById(id).filter(this::esPersonal).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Personal no encontrado."));}
    private boolean esPersonal(Usuario usuario){return usuario.getRol()!=null&&"PERSONAL_VETERINARIA".equals(usuario.getRol().getNombre());}
    private void validarEspecialidad(String tipo,String value){if(value==null||value.isBlank()||value.length()>60||("VETERINARIO".equals(tipo)&&!ESPECIALIDADES.contains(value)))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Selecciona una especialidad válida.");}
    private void validarTurnos(List<TurnoRequest> rows){if(rows==null||rows.isEmpty()||rows.stream().noneMatch(TurnoRequest::activo))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Selecciona al menos un turno laboral.");
        Set<String> seen=new HashSet<>();for(TurnoRequest row:rows){if(!DIAS.contains(row.diaSemana())||!seen.add(row.diaSemana()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Hay días de turno inválidos o duplicados.");
            if(row.activo()&&(row.horaInicio()==null||row.horaFin()==null||!row.horaInicio().isBefore(row.horaFin())))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La hora de inicio debe ser anterior a la hora de fin.");
            if(row.activo()){boolean descansoParcial=(row.horaDescansoInicio()==null)!=(row.duracionDescansoMinutos()==null);if(descansoParcial)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Completa la hora y duración del descanso o deja ambos campos vacíos.");if(row.duracionDescansoMinutos()!=null&&(row.duracionDescansoMinutos()<=0||row.duracionDescansoMinutos()>240||row.horaDescansoInicio().isBefore(row.horaInicio())||row.horaDescansoInicio().plusMinutes(row.duracionDescansoMinutos()).isAfter(row.horaFin())))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El descanso debe estar dentro del turno laboral y durar hasta 240 minutos.");}}}
    private List<TurnoLaboral> mapTurnos(List<TurnoRequest> rows,Usuario doctor){List<TurnoLaboral> turnos=new ArrayList<>();for(TurnoRequest row:rows)if(row.activo()){TurnoLaboral t=new TurnoLaboral();t.setVeterinario(doctor);t.setDiaSemana(row.diaSemana());t.setHoraInicio(row.horaInicio());t.setHoraFin(row.horaFin());t.setHoraDescansoInicio(row.horaDescansoInicio());t.setDuracionDescansoMinutos(row.duracionDescansoMinutos());turnos.add(t);}return turnos;}

    public record TurnoRequest(@NotBlank @Pattern(regexp="^(LUNES|MARTES|MIERCOLES|JUEVES|VIERNES|SABADO|DOMINGO)$") String diaSemana,
            boolean activo,LocalTime horaInicio,LocalTime horaFin,LocalTime horaDescansoInicio,@Min(1) @Max(240) Integer duracionDescansoMinutos){ }
    public record DoctorRequest(@NotBlank @Size(min=3,max=100) String nombreCompleto,@NotBlank @Email @Size(max=50) String correo,
            @NotBlank @Size(min=10,max=100) @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).+$") String password,
            @NotBlank @Pattern(regexp="^[0-9]{9}$") String telefono,
            @NotBlank @Pattern(regexp="^(VETERINARIO|ESTILISTA)$") String tipoPersonal,
            @NotBlank @Size(max=60) String especialidad,@NotEmpty List<@Valid TurnoRequest> turnos){ }
    public record UpdatePersonalRequest(@NotBlank @Size(min=3,max=100) String nombreCompleto,@NotBlank @Email @Size(max=50) String correo,
            @Size(max=100) @Pattern(regexp="^$|^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{10,}$") String password,
            @NotBlank @Pattern(regexp="^[0-9]{9}$") String telefono,
            @NotBlank @Pattern(regexp="^(VETERINARIO|ESTILISTA)$") String tipoPersonal,
            @NotBlank @Size(max=60) String especialidad,@NotEmpty List<@Valid TurnoRequest> turnos){ }
    public record DoctorDto(Long id,String nombreCompleto,String correo,String telefono,String especialidad,String tipoPersonal,List<TurnoRequest> turnos){
        static DoctorDto from(Usuario u){return new DoctorDto(u.getId(),u.getNombreCompleto(),u.getUsername(),u.getTelefono(),u.getEspecialidad(),u.getTipoPersonal(),u.getTurnos().stream().map(t->new TurnoRequest(t.getDiaSemana(),true,t.getHoraInicio(),t.getHoraFin(),t.getHoraDescansoInicio(),t.getDuracionDescansoMinutos())).toList());}}
}
