package com.veterinaria.veterinariaAnimalVet.controller;

import com.veterinaria.veterinariaAnimalVet.model.Rol;
import com.veterinaria.veterinariaAnimalVet.model.Usuario;
import com.veterinaria.veterinariaAnimalVet.service.RolService;
import com.veterinaria.veterinariaAnimalVet.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('PROPIETARIO')")
public class AdminController {

    private final RolService rolService;
    private final UsuarioService usuarioService;

    public AdminController(RolService rolService, UsuarioService usuarioService) {
        this.rolService = rolService;
        this.usuarioService = usuarioService;
    }

    // ============ PANEL PRINCIPAL ============
    @GetMapping
    public String panel(Model model) {
        model.addAttribute("activePage", "admin");
        model.addAttribute("totalRoles", rolService.findAll().size());
        model.addAttribute("totalUsuarios", usuarioService.findAll().size());
        return "admin/panel";
    }

    // ============ GESTIÓN DE ROLES ============
    @GetMapping("/roles")
    public String listarRoles(Model model) {
        model.addAttribute("activePage", "admin-roles");
        model.addAttribute("listaRoles", rolService.findAll());
        model.addAttribute("nuevoRol", new Rol());
        return "admin/roles";
    }

    @PostMapping("/roles")
    public String crearRol(@Valid @ModelAttribute("nuevoRol") Rol nuevoRol,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        // Valida que el nombre del rol no esté duplicado
        if (rolService.existsByNombre(nuevoRol.getNombre() != null ?
                nuevoRol.getNombre().toUpperCase().trim() : "")) {
            result.rejectValue("nombre", "error.rol", "Ya existe un rol con ese nombre");
        }

        if (result.hasErrors()) {
            model.addAttribute("activePage", "admin-roles");
            model.addAttribute("listaRoles", rolService.findAll());
            return "admin/roles";
        }

        rolService.save(nuevoRol);
        redirectAttributes.addFlashAttribute("mensajeExito", "Rol creado exitosamente.");
        return "redirect:/admin/roles";
    }

    @PostMapping("/roles/eliminar")
    public String eliminarRol(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        // Verifica que el rol no tiene usuarios asignados antes de eliminar
        rolService.findById(id).ifPresent(rol -> {
            if (rol.getUsuarios() == null || rol.getUsuarios().isEmpty()) {
                rolService.deleteById(id);
                redirectAttributes.addFlashAttribute("mensajeExito", "Rol eliminado.");
            } else {
                redirectAttributes.addFlashAttribute("mensajeError",
                        "No se puede eliminar el rol '" + rol.getNombre() +
                        "' porque tiene " + rol.getUsuarios().size() + " usuario(s) asignado(s).");
            }
        });
        return "redirect:/admin/roles";
    }

    // ============ GESTIÓN DE USUARIOS ============
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("activePage", "admin-usuarios");
        model.addAttribute("listaUsuarios", usuarioService.findAll());
        model.addAttribute("listaRoles", rolService.findAll());
        model.addAttribute("nuevoUsuario", new Usuario());
        return "admin/usuarios";
    }

    @PostMapping("/usuarios")
    public String crearUsuario(@Valid @ModelAttribute("nuevoUsuario") Usuario nuevoUsuario,
                               BindingResult result,
                               @RequestParam(value = "rolId", required = false) Long rolId,
                               @RequestParam(value = "passwordRaw", required = false) String passwordRaw,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // Si rolId no se pasa por parametro, lo tomamos del objeto bindeado
        if (rolId == null && nuevoUsuario.getRol() != null) {
            rolId = nuevoUsuario.getRol().getId();
        }

        // Si passwordRaw no se pasa por parametro, lo tomamos del objeto bindeado
        if (passwordRaw == null) {
            passwordRaw = nuevoUsuario.getPassword();
        }

        // Validar username duplicado
        if (usuarioService.existsByUsername(nuevoUsuario.getUsername())) {
            result.rejectValue("username", "error.usuario", "Ya existe un usuario con ese nombre");
        }

        // Validar que se eligió un rol válido
        if (rolId == null) {
            result.rejectValue("rol", "error.rol", "Debes seleccionar un rol");
        }

        // Validar contraseña
        if (passwordRaw == null || passwordRaw.trim().length() < 6) {
            model.addAttribute("errorPassword", "La contraseña debe tener al menos 6 caracteres");
            result.rejectValue("password", "error.password", "Contraseña inválida");
        }

        //Errores
        if (result.hasErrors()) {
            model.addAttribute("activePage", "admin-usuarios");
            model.addAttribute("listaUsuarios", usuarioService.findAll());
            model.addAttribute("listaRoles", rolService.findAll());
            return "admin/usuarios";
        }

        // Asignar el rol al usuario
        rolService.findById(rolId).ifPresent(nuevoUsuario::setRol);

        // Asignar la contraseña raw (será encriptada en el service)
        nuevoUsuario.setPassword(passwordRaw);

        // Registrar usuario con contraseña encriptada
        usuarioService.registrarUsuario(nuevoUsuario);

        redirectAttributes.addFlashAttribute("mensajeExito",
                "Usuario '" + nuevoUsuario.getUsername() + "' registrado exitosamente.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/eliminar")
    public String eliminarUsuario(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        usuarioService.findById(id).ifPresent(u -> {
            // No permitir eliminar el único PROPIETARIO
            if ("PROPIETARIO".equals(u.getRol().getNombre())) {
                long propietarios = usuarioService.findAll().stream()
                        .filter(us -> "PROPIETARIO".equals(us.getRol().getNombre()))
                        .count();
                if (propietarios <= 1) {
                    redirectAttributes.addFlashAttribute("mensajeError",
                            "No se puede eliminar el único usuario PROPIETARIO del sistema.");
                    return;
                }
            }
            usuarioService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Usuario '" + u.getUsername() + "' eliminado.");
        });
        return "redirect:/admin/usuarios";
    }
}
