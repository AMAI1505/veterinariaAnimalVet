package com.veterinaria.veterinariaAnimalVet.controller;

import com.veterinaria.veterinariaAnimalVet.model.Usuario;
import com.veterinaria.veterinariaAnimalVet.repository.ClienteRepository;
import com.veterinaria.veterinariaAnimalVet.repository.RolRepository;
import com.veterinaria.veterinariaAnimalVet.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class RegistroClienteApiController {
    private final ClienteRepository clientes;
    private final RolRepository roles;
    private final UsuarioService usuarios;

    public RegistroClienteApiController(ClienteRepository clientes, RolRepository roles, UsuarioService usuarios) {
        this.clientes = clientes;
        this.roles = roles;
        this.usuarios = usuarios;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public RegistroResponse registrar(@Valid @RequestBody RegistroRequest request) {
        String correo = request.correo().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByUsername(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo.");
        }
        var cliente = clientes.findByCorreoIgnoreCaseAndDni(correo, request.dni()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El correo y DNI no coinciden con los datos del dueño registrados en una ficha de mascota."));

        Usuario cuenta = new Usuario();
        cuenta.setUsername(correo);
        cuenta.setNombreCompleto(cliente.getNombre());
        cuenta.setRol(roles.findByNombre("CLIENTE").orElseThrow(() ->
                new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No está disponible el registro de clientes.")));
        cuenta.setEnabled(true);
        cuenta.setPassword(request.password());
        usuarios.registrarUsuario(cuenta);
        return new RegistroResponse(correo, "La cuenta se creó correctamente. Ya puedes iniciar sesión.");
    }

    public record RegistroRequest(@NotBlank @Email @Size(max = 50) String correo,
            @NotBlank @Pattern(regexp = "^[0-9]{8}$") String dni,
            @NotBlank @Size(min = 10, max = 100) @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).+$", message = "Usa al menos 10 caracteres, mayúscula, minúscula, número y símbolo.") String password) { }

    public record RegistroResponse(String correo, String mensaje) { }
}
