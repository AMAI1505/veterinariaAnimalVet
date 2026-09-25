package com.veterinaria.veterinariaAnimalVet.controller;

import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.veterinaria.veterinariaAnimalVet.model.Cita;
import com.veterinaria.veterinariaAnimalVet.model.Cliente;
import com.veterinaria.veterinariaAnimalVet.model.Mascota;
import com.veterinaria.veterinariaAnimalVet.model.Usuario;
import com.veterinaria.veterinariaAnimalVet.service.CitaService;
import com.veterinaria.veterinariaAnimalVet.service.ClienteService;
import com.veterinaria.veterinariaAnimalVet.service.MascotaService;

import jakarta.validation.Valid;

@Controller
public class VeterinariaController {

    private final ClienteService clienteService;
    private final MascotaService mascotaService;
    private final CitaService citaService;

    public VeterinariaController(ClienteService clienteService,
                                 MascotaService mascotaService,
                                 CitaService citaService) {
        this.clienteService = clienteService;
        this.mascotaService = mascotaService;
        this.citaService = citaService;
    }

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
    
    @GetMapping("/citas")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeVer(authentication)")
    public String citas(Model model, @AuthenticationPrincipal Usuario usuario) {
        agregarInfoUsuario(model, usuario);
        model.addAttribute("activePage", "citas");
        model.addAttribute("listaCitas", citaService.findAll());
        model.addAttribute("nuevaCita", new Cita());
        model.addAttribute("listaMascotas", mascotaService.findAll());
        model.addAttribute("listaClientes", clienteService.findAll());
        return "citas";
    }

    @PostMapping("/citas")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeCrear(authentication)")
    public String guardarCita(@Valid @ModelAttribute("nuevaCita") Cita nuevaCita,
                              BindingResult result,
                              Model model,
                              @AuthenticationPrincipal Usuario usuario) {
        validarMascotaDeCita(nuevaCita, result);
        if (nuevaCita.getFechaHora() != null && citaService.existsByFechaHora(nuevaCita.getFechaHora())) {
            result.rejectValue("fechaHora", "error.cita", "Ya existe una cita en esa fecha y hora.");
        }
        if (result.hasErrors()) {
            agregarInfoUsuario(model, usuario);
            model.addAttribute("activePage", "citas");
            model.addAttribute("listaCitas", citaService.findAll());
            model.addAttribute("listaMascotas", mascotaService.findAll());
            model.addAttribute("listaClientes", clienteService.findAll());
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
        if (citaEditar.getFechaHora() != null
                && citaService.existsByFechaHoraAndIdNot(citaEditar.getFechaHora(), citaEditar.getId())) {
            result.rejectValue("fechaHora", "error.cita", "Ya existe una cita en esa fecha y hora.");
        }
        if (result.hasErrors()) {
            agregarInfoUsuario(model, usuario);
            model.addAttribute("activePage", "citas");
            model.addAttribute("listaCitas", citaService.findAll());
            model.addAttribute("listaMascotas", mascotaService.findAll());
            model.addAttribute("listaClientes", clienteService.findAll());
            model.addAttribute("nuevaCita", new Cita());
            return "citas";
        }
        citaService.save(citaEditar);
        redirectAttributes.addFlashAttribute("mensajeExito", "Cita actualizada correctamente.");
        return "redirect:/citas";
    }

    @PostMapping("/citas/eliminar")
    @PreAuthorize("hasRole('PROPIETARIO') or @usuarioPermiso.puedeEliminar(authentication)")
    public String eliminarCitas(@RequestParam Long id) {
        citaService.deleteById(id);
        return "redirect:/citas";
    }

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
}
