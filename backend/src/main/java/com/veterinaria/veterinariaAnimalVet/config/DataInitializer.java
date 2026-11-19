package com.veterinaria.veterinariaAnimalVet.config;

import com.veterinaria.veterinariaAnimalVet.model.Rol;
import com.veterinaria.veterinariaAnimalVet.model.Usuario;
import com.veterinaria.veterinariaAnimalVet.repository.RolRepository;
import com.veterinaria.veterinariaAnimalVet.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.env.Environment;

@Component
public class DataInitializer implements CommandLineRunner {
    private final RolRepository roles;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String initialPassword;
    private final String passwordReset;
    private final String receptionPassword;
    private final String vetPassword;
    private final Environment environment;

    public DataInitializer(RolRepository roles, UsuarioRepository usuarios, PasswordEncoder encoder,
            @Value("${animalvet.initial-admin-password:admin123}") String initialPassword,
            @Value("${animalvet.admin-password-reset:}") String passwordReset,
            @Value("${animalvet.initial-reception-password:recepcion123}") String receptionPassword,
            @Value("${animalvet.initial-vet-password:vet12345}") String vetPassword, Environment environment) {
        this.roles = roles; this.usuarios = usuarios; this.encoder = encoder;
        this.initialPassword = initialPassword; this.passwordReset = passwordReset;
        this.receptionPassword = receptionPassword; this.vetPassword = vetPassword;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(String... args) {
        boolean nuevaInstalacion = usuarios.count() == 0;
        if (java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            requireStrong("INITIAL_ADMIN_PASSWORD", initialPassword);
            requireStrong("INITIAL_RECEPTION_PASSWORD", receptionPassword);
            requireStrong("INITIAL_VET_PASSWORD", vetPassword);
            if (!passwordReset.isBlank()) requireStrong("ADMIN_PASSWORD_RESET", passwordReset);
        }
        Rol admin = roles.findByNombre("ADMINISTRADOR").orElseGet(() -> roles.findByNombre("PROPIETARIO").map(legacy -> {
            legacy.setNombre("ADMINISTRADOR"); legacy.setDescripcion("Cuenta administrativa raíz fija."); return roles.save(legacy);
        }).orElseGet(() -> roles.save(new Rol("ADMINISTRADOR", "Cuenta administrativa raíz fija.", true, true, true, true))));
        Rol legacyVeterinario = roles.findByNombre("VETERINARIO").orElse(null);
        Rol personalVeterinaria = roles.findByNombre("PERSONAL_VETERINARIA").orElse(null);
        if (personalVeterinaria == null) {
            if (legacyVeterinario != null) {
                legacyVeterinario.setNombre("PERSONAL_VETERINARIA");
                legacyVeterinario.setDescripcion("Veterinarios y estilistas de mascotas.");
                personalVeterinaria = roles.save(legacyVeterinario);
            } else {
                personalVeterinaria = roles.save(new Rol("PERSONAL_VETERINARIA", "Veterinarios y estilistas de mascotas.", true, false, false, false));
            }
        } else if (legacyVeterinario != null && !legacyVeterinario.getId().equals(personalVeterinaria.getId())) {
            for (Usuario user : usuarios.findAll()) {
                if (user.getRol() != null && legacyVeterinario.getId().equals(user.getRol().getId())) {
                    user.setRol(personalVeterinaria);
                    usuarios.save(user);
                }
            }
            roles.delete(legacyVeterinario);
        }
        Rol recepcion = roles.findByNombre("RECEPCION").orElseGet(() -> roles.save(new Rol(
                "RECEPCION", "Gestión de citas y atención administrativa.", true, true, true, true)));
        Rol cliente = roles.findByNombre("CLIENTE").orElseGet(() -> roles.save(new Rol(
                "CLIENTE", "Acceso de tutores a sus propias citas.", true, false, false, false)));

        Usuario root = usuarios.findByUsername("administrador").orElse(null);
        Usuario legacyRoot = usuarios.findByUsername("propietario").orElse(null);
        if (root == null) root = legacyRoot == null ? new Usuario() : legacyRoot;
        else if (legacyRoot != null && !legacyRoot.getId().equals(root.getId())) {
            legacyRoot.setUsername("propietario.inactivo." + legacyRoot.getId());
            legacyRoot.setEnabled(false);
            usuarios.save(legacyRoot);
        }
        root.setUsername("administrador"); root.setRol(admin); root.setEnabled(true);
        if (!passwordReset.isBlank()) { root.setPassword(encoder.encode(passwordReset)); root.invalidateTokens(); }
        else if (root.getId() == null) root.setPassword(encoder.encode(initialPassword));
        root.setIntentosFallidos(0); root.setFechaDesbloqueo(null);
        usuarios.save(root);

        Usuario frontDesk = usuarios.findByUsername("recepcion").orElse(null);
        Usuario vet = usuarios.findByUsername("veterinario.prueba").orElse(null);
        if (nuevaInstalacion) {
            if (frontDesk == null) frontDesk = new Usuario();
            if (frontDesk.getId() == null) frontDesk.setPassword(encoder.encode(receptionPassword));
            frontDesk.setUsername("recepcion"); frontDesk.setNombreCompleto("Recepción AnimalVet");
            frontDesk.setTelefono("900000001"); frontDesk.setRol(recepcion); frontDesk.setEnabled(true);
            usuarios.save(frontDesk);

            if (vet == null) vet = new Usuario();
            if (vet.getId() == null) {
                vet.setPassword(encoder.encode(vetPassword)); vet.setUsername("veterinario.prueba");
                vet.setNombreCompleto("Dra. Veterinaria de Prueba"); vet.setTelefono("900000002");
                vet.setEspecialidad("Medicina General"); vet.setEnabled(true);
                java.time.LocalTime inicio = java.time.LocalTime.of(9, 0), fin = java.time.LocalTime.of(17, 0);
                for (String dia : java.util.List.of("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES")) {
                    com.veterinaria.veterinariaAnimalVet.model.TurnoLaboral turno = new com.veterinaria.veterinariaAnimalVet.model.TurnoLaboral();
                    turno.setVeterinario(vet); turno.setDiaSemana(dia); turno.setHoraInicio(inicio); turno.setHoraFin(fin); vet.getTurnos().add(turno);
                }
            }
            vet.setRol(personalVeterinaria); vet.setTipoPersonal("VETERINARIO");
            usuarios.save(vet);
        }

        for (Usuario user : usuarios.findAll()) {
            if (user.getId().equals(root.getId())
                    || (frontDesk != null && user.getId().equals(frontDesk.getId()))
                    || (vet != null && user.getId().equals(vet.getId()))) continue;
            boolean personal = user.getRol() != null && "PERSONAL_VETERINARIA".equals(user.getRol().getNombre());
            if (user.getRol() != null && "ADMINISTRADOR".equals(user.getRol().getNombre())) { user.setEnabled(false); user.invalidateTokens(); }
            else if (!personal && !"RECEPCION".equals(user.getRol() == null ? null : user.getRol().getNombre())
                    && !"CLIENTE".equals(user.getRol() == null ? null : user.getRol().getNombre())) user.setEnabled(false);
            if (personal && (user.getTipoPersonal() == null || user.getTipoPersonal().isBlank())) user.setTipoPersonal("VETERINARIO");
            usuarios.save(user);
        }
    }

    private void requireStrong(String name, String password) {
        if (password == null || password.length() < 12 || password.length() > 100
                || !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).+$")) {
            throw new IllegalStateException(name + " debe tener 12 a 100 caracteres e incluir mayúscula, minúscula, número y símbolo.");
        }
    }
}
