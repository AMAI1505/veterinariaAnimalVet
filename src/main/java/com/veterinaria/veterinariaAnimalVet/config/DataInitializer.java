package com.veterinaria.veterinariaAnimalVet.config;

import com.veterinaria.veterinariaAnimalVet.model.Rol;
import com.veterinaria.veterinariaAnimalVet.model.Usuario;
import com.veterinaria.veterinariaAnimalVet.repository.RolRepository;
import com.veterinaria.veterinariaAnimalVet.repository.UsuarioRepository;
import com.veterinaria.veterinariaAnimalVet.repository.ClienteRepository;
import com.veterinaria.veterinariaAnimalVet.repository.MascotaRepository;
import com.veterinaria.veterinariaAnimalVet.model.Cliente;
import com.veterinaria.veterinariaAnimalVet.model.Mascota;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String initialAdminPassword;
    private final ClienteRepository clienteRepository;
    private final MascotaRepository mascotaRepository;

    public DataInitializer(RolRepository rolRepository,
                           UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           ClienteRepository clienteRepository,
                           MascotaRepository mascotaRepository,
                           @Value("${animalvet.initial-admin-password:admin123}") String initialAdminPassword) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.clienteRepository = clienteRepository;
        this.mascotaRepository = mascotaRepository;
        this.initialAdminPassword = initialAdminPassword;
    }

    @Override
    public void run(String... args) throws Exception {
        //Crear rol PROPIETARIO si no existe
        Rol rolPropietario;
        if (!rolRepository.existsByNombre("PROPIETARIO")) {
            rolPropietario = new Rol(
                    "PROPIETARIO",
                    "Acceso total al sistema veterinario",
                    true,   // puedeVer
                    true,   // puedeCrear
                    true,   // puedeEditar
                    true    // puedeEliminar
            );
            rolPropietario = rolRepository.save(rolPropietario);
            System.out.println("Rol PROPIETARIO creado.");
        } else {
            rolPropietario = rolRepository.findByNombre("PROPIETARIO").orElseThrow();
        }

        if (!rolRepository.existsByNombre("USUARIO")) {
            rolRepository.save(new Rol(
                    "USUARIO",
                    "Acceso de usuario registrado: puede consultar y crear registros.",
                    true,
                    true,
                    false,
                    false
            ));
        }

        Rol veterinario = rolRepository.findByNombre("VETERINARIO").orElseGet(() -> rolRepository.save(
                new Rol("VETERINARIO", "Atención clínica e historial médico", true, true, true, false)));
        if (usuarioRepository.findByUsername("dra.sofia").isEmpty()) {
            Usuario doctor = new Usuario();
            doctor.setUsername("dra.sofia");
            doctor.setPassword(passwordEncoder.encode("vet123"));
            doctor.setRol(veterinario);
            doctor.setEnabled(true);
            usuarioRepository.save(doctor);
        }

        //Crear usuario 'propietario' si no existe
        if (!usuarioRepository.findByUsername("propietario").isPresent()) {
            Usuario propietario = new Usuario();
            propietario.setUsername("propietario");
            // La contraseña se encripta con BCrypt en tiempo de ejecución
            propietario.setPassword(passwordEncoder.encode(initialAdminPassword));
            propietario.setRol(rolPropietario);
            propietario.setEnabled(true);
            usuarioRepository.save(propietario);
            System.out.println("Usuario administrador inicial creado.");
        }

        if (clienteRepository.count() == 0) {
            Cliente cliente = new Cliente();
            cliente.setDni("45678912");
            cliente.setNombre("María Fernanda López");
            cliente.setTelefono("987654321");
            cliente = clienteRepository.save(cliente);
            Mascota mascota = new Mascota();
            mascota.setNombre("Luna"); mascota.setEspecie("Perro"); mascota.setRaza("Labrador");
            mascota.setPeso("18.5"); mascota.setColor("Dorado"); mascota.setCliente(cliente);
            mascotaRepository.save(mascota);
        }
    }
}
