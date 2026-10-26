package com.veterinaria.veterinariaAnimalVet.controller;

import com.veterinaria.veterinariaAnimalVet.model.Cita;
import com.veterinaria.veterinariaAnimalVet.model.Consulta;
import com.veterinaria.veterinariaAnimalVet.model.Cliente;
import com.veterinaria.veterinariaAnimalVet.model.Mascota;
import com.veterinaria.veterinariaAnimalVet.model.Usuario;
import com.veterinaria.veterinariaAnimalVet.service.CitaService;
import com.veterinaria.veterinariaAnimalVet.service.ConsultaService;
import com.veterinaria.veterinariaAnimalVet.service.UsuarioService;
import com.veterinaria.veterinariaAnimalVet.service.ClienteService;
import com.veterinaria.veterinariaAnimalVet.service.MascotaService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.IOException;

import java.util.Optional;

@Controller
public class VeterinariaController {

    private final ClienteService clienteService;
    private final MascotaService mascotaService;
    private final CitaService citaService;
    private final ConsultaService consultaService;
    private final UsuarioService usuarioService;

    public VeterinariaController(ClienteService clienteService,
                                 MascotaService mascotaService,
                                 CitaService citaService,
                                 ConsultaService consultaService,
                                 UsuarioService usuarioService) {
        this.clienteService = clienteService;
        this.mascotaService = mascotaService;
        this.citaService = citaService;
        this.consultaService = consultaService;
        this.usuarioService = usuarioService;
    }

    // Inyecta el usuario autenticado en el modelo para los templates
    private void agregarInfoUsuario(Model model, Usuario usuario) {
        if (usuario != null && usuario.getRol() != null) {
            model.addAttribute("rolNombre", usuario.getRol().getNombre());
            model.addAttribute("puedeCrear", usuario.getRol().isPuedeCrear());
            model.addAttribute("puedeEditar", usuario.getRol().isPuedeEditar());
            model.addAttribute("puedeEliminar", usuario.getRol().isPuedeEliminar());
            model.addAttribute("usuarioNombre", usuario.getUsername());
        }
    }

    private void validarMascotaDeCita(Cita cita, BindingResult result) {
        Long mascotaId = cita.getMascota() == null ? null : cita.getMascota().getId();
        if (mascotaId == null) {
            result.rejectValue("mascota.id", "error.cita", "Debes seleccionar una mascota.");
            return;
        }
        Mascota mascota = mascotaService.findById(mascotaId).orElse(null);
        if (mascota == null) {
            result.rejectValue("mascota.id", "error.cita", "La mascota seleccionada no existe.");
            return;
        }
        cita.setMascota(mascota);
        cita.setCliente(mascota.getCliente());
    }
    // ==================== CITAS ====================
    @GetMapping("/citas")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeVer(authentication)")
    public String citas(Model model, @AuthenticationPrincipal Usuario usuario) {
        agregarInfoUsuario(model, usuario);
        model.addAttribute("activePage", "citas");
        model.addAttribute("listaCitas", citaService.findAll());
        model.addAttribute("nuevaCita", new Cita());
        model.addAttribute("listaMascotas", mascotaService.findAll());
        model.addAttribute("listaClientes", clienteService.findAll());
        model.addAttribute("listaVeterinarios", veterinarios());
        return "citas";
    }

    @PostMapping("/citas")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeCrear(authentication)")
    public String guardarCita(@Valid @ModelAttribute("nuevaCita") Cita nuevaCita,
                              BindingResult result,
                              Model model,
                              @AuthenticationPrincipal Usuario usuario) {
        validarMascotaDeCita(nuevaCita, result);
        validarVeterinario(nuevaCita, result);
        if (nuevaCita.getFechaHora() != null && nuevaCita.getVeterinario() != null
                && citaService.existsByFechaHoraAndVeterinarioId(nuevaCita.getFechaHora(), nuevaCita.getVeterinario().getId())) {
            result.rejectValue("fechaHora", "error.cita", "El veterinario ya tiene una cita en ese horario.");
        }
        if (result.hasErrors()) {
            agregarInfoUsuario(model, usuario);
            model.addAttribute("activePage", "citas");
            model.addAttribute("listaCitas", citaService.findAll());
            model.addAttribute("listaMascotas", mascotaService.findAll());
            model.addAttribute("listaClientes", clienteService.findAll());
            model.addAttribute("listaVeterinarios", veterinarios());
            return "citas";
        }
        citaService.save(nuevaCita);
        return "redirect:/citas";
    }

    @GetMapping("/citas/editar/{id}")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeEditar(authentication)")
    public String editarCitaForm(@PathVariable Long id, Model model,
                                 @AuthenticationPrincipal Usuario usuario) {
        Optional<Cita> cita = citaService.findById(id);
        if (cita.isEmpty()) return "redirect:/citas";
        agregarInfoUsuario(model, usuario);
        model.addAttribute("activePage", "citas");
        model.addAttribute("citaEditar", cita.get());
        model.addAttribute("listaMascotas", mascotaService.findAll());
        model.addAttribute("listaClientes", clienteService.findAll());
        model.addAttribute("listaVeterinarios", veterinarios());
        model.addAttribute("listaCitas", citaService.findAll());
        model.addAttribute("nuevaCita", new Cita());
        return "citas";
    }

    @PostMapping("/citas/editar")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeEditar(authentication)")
    public String editarCita(@Valid @ModelAttribute("citaEditar") Cita citaEditar,
                             BindingResult result,
                             Model model,
                              @AuthenticationPrincipal Usuario usuario,
                              RedirectAttributes redirectAttributes) {
        validarMascotaDeCita(citaEditar, result);
        validarVeterinario(citaEditar, result);
        if (citaEditar.getFechaHora() != null && citaEditar.getVeterinario() != null
                && citaService.existsByFechaHoraAndVeterinarioIdAndIdNot(citaEditar.getFechaHora(), citaEditar.getVeterinario().getId(), citaEditar.getId())) {
            result.rejectValue("fechaHora", "error.cita", "El veterinario ya tiene una cita en ese horario.");
        }
        if (result.hasErrors()) {
            agregarInfoUsuario(model, usuario);
            model.addAttribute("activePage", "citas");
            model.addAttribute("listaCitas", citaService.findAll());
            model.addAttribute("listaMascotas", mascotaService.findAll());
            model.addAttribute("listaClientes", clienteService.findAll());
            model.addAttribute("listaVeterinarios", veterinarios());
            model.addAttribute("nuevaCita", new Cita());
            return "citas";
        }
        citaService.save(citaEditar);
        redirectAttributes.addFlashAttribute("mensajeExito", "Cita actualizada correctamente.");
        return "redirect:/citas";
    }

    private java.util.List<Usuario> veterinarios() {
        return usuarioService.findAll().stream().filter(u -> u.getRol() != null && "VETERINARIO".equals(u.getRol().getNombre())).toList();
    }

    private void validarVeterinario(Cita cita, BindingResult result) {
        Long id = cita.getVeterinario() == null ? null : cita.getVeterinario().getId();
        Usuario veterinario = id == null ? null : usuarioService.findById(id).orElse(null);
        if (veterinario == null || veterinario.getRol() == null || !"VETERINARIO".equals(veterinario.getRol().getNombre())) {
            result.rejectValue("veterinario", "error.cita", "Selecciona un veterinario válido.");
        } else cita.setVeterinario(veterinario);
        if (cita.getFechaHora() != null) {
            java.time.LocalDateTime hora = cita.getFechaHora();
            if (hora.isBefore(java.time.LocalDateTime.now()))
                result.rejectValue("fechaHora", "error.cita", "La cita debe programarse en una fecha futura.");
            if (hora.getHour() < 8 || hora.getHour() >= 20 || hora.getDayOfWeek() == java.time.DayOfWeek.SUNDAY)
                result.rejectValue("fechaHora", "error.cita", "El horario de atención es de lunes a sábado, de 08:00 a 20:00.");
        }
    }

    @PostMapping("/citas/eliminar")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeEliminar(authentication)")
    public String eliminarCitas(@RequestParam Long id) {
        citaService.deleteById(id);
        return "redirect:/citas";
    }

    // ==================== CLIENTES ====================
    @GetMapping("/clientes")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeVer(authentication)")
    public String clientes(Model model, @AuthenticationPrincipal Usuario usuario) {
        agregarInfoUsuario(model, usuario);
        model.addAttribute("activePage", "clientes");
        model.addAttribute("listaClientes", clienteService.findAll());
        model.addAttribute("nuevoCliente", new Cliente());
        return "clientes";
    }

    @PostMapping("/clientes")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeCrear(authentication)")
    public String guardarCliente(@Valid @ModelAttribute("nuevoCliente") Cliente nuevoCliente,
                                 BindingResult result,
                                 Model model,
                                 @AuthenticationPrincipal Usuario usuario) {
        if (nuevoCliente.getDni() != null && clienteService.existsByDni(nuevoCliente.getDni())) {
            result.rejectValue("dni", "error.cliente", "Ya existe un cliente con ese DNI.");
        }
        if (result.hasErrors()) {
            agregarInfoUsuario(model, usuario);
            model.addAttribute("activePage", "clientes");
            model.addAttribute("listaClientes", clienteService.findAll());
            return "clientes";
        }
        clienteService.save(nuevoCliente);
        return "redirect:/clientes";
    }

    @GetMapping("/clientes/editar/{id}")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeEditar(authentication)")
    public String editarClienteForm(@PathVariable Long id, Model model,
                                    @AuthenticationPrincipal Usuario usuario) {
        Optional<Cliente> cliente = clienteService.findById(id);
        if (cliente.isEmpty()) return "redirect:/clientes";
        agregarInfoUsuario(model, usuario);
        model.addAttribute("activePage", "clientes");
        model.addAttribute("clienteEditar", cliente.get());
        model.addAttribute("listaClientes", clienteService.findAll());
        model.addAttribute("nuevoCliente", new Cliente());
        return "clientes";
    }

    @PostMapping("/clientes/editar")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeEditar(authentication)")
    public String editarCliente(@Valid @ModelAttribute("clienteEditar") Cliente clienteEditar,
                                BindingResult result,
                                Model model,
                                @AuthenticationPrincipal Usuario usuario,
                                RedirectAttributes redirectAttributes) {
        if (clienteEditar.getDni() != null
                && clienteService.existsByDniAndIdNot(clienteEditar.getDni(), clienteEditar.getId())) {
            result.rejectValue("dni", "error.cliente", "Ya existe otro cliente con ese DNI.");
        }
        if (result.hasErrors()) {
            agregarInfoUsuario(model, usuario);
            model.addAttribute("activePage", "clientes");
            model.addAttribute("listaClientes", clienteService.findAll());
            model.addAttribute("nuevoCliente", new Cliente());
            return "clientes";
        }
        clienteService.save(clienteEditar);
        redirectAttributes.addFlashAttribute("mensajeExito", "Cliente actualizado correctamente.");
        return "redirect:/clientes";
    }

    @PostMapping("/clientes/eliminar")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeEliminar(authentication)")
    public String eliminarCliente(@RequestParam Long id) {
        clienteService.deleteById(id);
        return "redirect:/clientes";
    }

    // ==================== MASCOTAS ====================
    @GetMapping("/mascotas")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeVer(authentication)")
    public String mascotas(Model model, @AuthenticationPrincipal Usuario usuario) {
        agregarInfoUsuario(model, usuario);
        model.addAttribute("activePage", "mascotas");
        model.addAttribute("listaMascotas", mascotaService.findAll());
        model.addAttribute("listaClientes", clienteService.findAll());
        model.addAttribute("nuevaMascota", new Mascota());
        return "mascotas";
    }

    @PostMapping("/mascotas")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeCrear(authentication)")
    public String guardarMascota(@Valid @ModelAttribute("nuevaMascota") Mascota nuevaMascota,
                                 @RequestParam(value = "foto", required = false) MultipartFile foto,
                                 BindingResult result,
                                 Model model,
                                 @AuthenticationPrincipal Usuario usuario) {
        if (nuevaMascota.getCliente() == null || nuevaMascota.getCliente().getId() == null) {
            result.rejectValue("cliente", "error.cliente", "Debes seleccionar un dueño para la mascota");
        }
        if (result.hasErrors()) {
            agregarInfoUsuario(model, usuario);
            model.addAttribute("activePage", "mascotas");
            model.addAttribute("listaMascotas", mascotaService.findAll());
            model.addAttribute("listaClientes", clienteService.findAll());
            return "mascotas";
        }
        guardarFoto(nuevaMascota, foto);
        mascotaService.save(nuevaMascota);
        return "redirect:/mascotas";
    }

    @GetMapping("/mascotas/editar/{id}")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeEditar(authentication)")
    public String editarMascotaForm(@PathVariable Long id, Model model,
                                    @AuthenticationPrincipal Usuario usuario) {
        Optional<Mascota> mascota = mascotaService.findById(id);
        if (mascota.isEmpty()) return "redirect:/mascotas";
        agregarInfoUsuario(model, usuario);
        model.addAttribute("activePage", "mascotas");
        model.addAttribute("mascotaEditar", mascota.get());
        model.addAttribute("listaMascotas", mascotaService.findAll());
        model.addAttribute("listaClientes", clienteService.findAll());
        model.addAttribute("nuevaMascota", new Mascota());
        return "mascotas";
    }

    @PostMapping("/mascotas/editar")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeEditar(authentication)")
    public String editarMascota(@Valid @ModelAttribute("mascotaEditar") Mascota mascotaEditar,
                                @RequestParam(value = "foto", required = false) MultipartFile foto,
                                BindingResult result,
                                Model model,
                                @AuthenticationPrincipal Usuario usuario,
                                RedirectAttributes redirectAttributes) {
        if (mascotaEditar.getCliente() == null || mascotaEditar.getCliente().getId() == null) {
            result.rejectValue("cliente", "error.cliente", "Debes seleccionar un dueño");
        }
        if (result.hasErrors()) {
            agregarInfoUsuario(model, usuario);
            model.addAttribute("activePage", "mascotas");
            model.addAttribute("listaMascotas", mascotaService.findAll());
            model.addAttribute("listaClientes", clienteService.findAll());
            model.addAttribute("nuevaMascota", new Mascota());
            return "mascotas";
        }
        mascotaEditar.setFotoUrl(mascotaService.findById(mascotaEditar.getId()).map(Mascota::getFotoUrl).orElse(null));
        guardarFoto(mascotaEditar, foto);
        mascotaService.save(mascotaEditar);
        redirectAttributes.addFlashAttribute("mensajeExito", "Mascota actualizada correctamente.");
        return "redirect:/mascotas";
    }

    @PostMapping("/mascotas/eliminar")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeEliminar(authentication)")
    public String eliminarMascota(@RequestParam Long id) {
        mascotaService.deleteById(id);
        return "redirect:/mascotas";
    }

    private void guardarFoto(Mascota mascota, MultipartFile foto) {
        if (foto == null || foto.isEmpty()) return;
        String tipo = foto.getContentType();
        if (tipo == null || !tipo.matches("image/(jpeg|png|webp)"))
            throw new IllegalArgumentException("La foto debe ser JPG, PNG o WEBP.");
        if (foto.getSize() > 5 * 1024 * 1024) throw new IllegalArgumentException("La foto no puede superar 5 MB.");
        try {
            String extension = switch (tipo) {
                case "image/png" -> "png";
                case "image/webp" -> "webp";
                default -> "jpg";
            };
            String nombre = java.util.UUID.randomUUID() + "." + extension;
            Path carpeta = Path.of("uploads", "mascotas");
            Files.createDirectories(carpeta);
            Files.copy(foto.getInputStream(), carpeta.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
            mascota.setFotoUrl("/uploads/mascotas/" + nombre);
        } catch (IOException e) { throw new IllegalStateException("No se pudo guardar la foto.", e); }
    }

    @GetMapping("/mascotas/{id}/historial")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeVer(authentication)")
    public String historial(@PathVariable Long id, Model model, @AuthenticationPrincipal Usuario usuario) {
        Mascota mascota = mascotaService.findById(id).orElseThrow();
        agregarInfoUsuario(model, usuario);
        model.addAttribute("activePage", "mascotas");
        model.addAttribute("mascota", mascota);
        model.addAttribute("consultas", consultaService.porMascota(id));
        model.addAttribute("consulta", new Consulta());
        return "historial";
    }

    @PostMapping("/mascotas/{id}/historial")
    @PreAuthorize("hasRole('PROPIETARIO') or hasRole('VETERINARIO')")
    public String guardarConsulta(@PathVariable Long id, @Valid @ModelAttribute("consulta") Consulta consulta,
                                  BindingResult result, @AuthenticationPrincipal Usuario usuario, Model model) {
        Mascota mascota = mascotaService.findById(id).orElseThrow();
        if (result.hasErrors()) {
            agregarInfoUsuario(model, usuario);
            model.addAttribute("activePage", "mascotas");
            model.addAttribute("mascota", mascota);
            model.addAttribute("consultas", consultaService.porMascota(id));
            return "historial";
        }
        consulta.setMascota(mascota);
        consulta.setVeterinario(usuario);
        consultaService.guardar(consulta);
        return "redirect:/mascotas/" + id + "/historial";
    }
}
