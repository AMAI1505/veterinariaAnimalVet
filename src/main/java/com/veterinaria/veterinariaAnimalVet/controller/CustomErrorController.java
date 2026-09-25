package com.veterinaria.veterinariaAnimalVet.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (statusObj != null) {
            int statusCode = Integer.parseInt(statusObj.toString());

            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                // 403 - Acceso Denegado
                model.addAttribute("codigoError", "403");
                model.addAttribute("tituloError", "Acceso Denegado");
                model.addAttribute("mensajeError",
                        "No tienes permiso para acceder a esta página.");
                model.addAttribute("descripcionError",
                        "Tu rol actual no tiene los privilegios necesarios para ver este contenido. " +
                        "Si crees que esto es un error, contacta al administrador del sistema.");
                model.addAttribute("icono", "bi-shield-lock");
                return "error/403";

            } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                // 404 - Página No Encontrada
                String uri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
                model.addAttribute("codigoError", "404");
                model.addAttribute("tituloError", "Página No Encontrada");
                model.addAttribute("mensajeError",
                        "La ruta '" + (uri != null ? uri : "solicitada") + "' no existe.");
                model.addAttribute("descripcionError",
                        "La página que buscas no existe o fue movida. " +
                        "Verifica la URL e intenta de nuevo.");
                model.addAttribute("icono", "bi-map");
                return "error/404";

            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                // 500 - Error Interno del Servidor
                model.addAttribute("codigoError", "500");
                model.addAttribute("tituloError", "Error Interno del Servidor");
                model.addAttribute("mensajeError",
                        "Ocurrió un error inesperado en el servidor.");
                model.addAttribute("descripcionError",
                        "El sistema encontró un problema técnico. " +
                        "Intenta de nuevo más tarde.");
                model.addAttribute("icono", "bi-exclamation-triangle");
                return "error/500";
            }
        }

        // Error genérico
        model.addAttribute("codigoError", "Error");
        model.addAttribute("tituloError", "Error Inesperado");
        model.addAttribute("mensajeError", "Ocurrió un error desconocido.");
        model.addAttribute("descripcionError",
                "Por favor regresa al inicio e intenta de nuevo.");
        model.addAttribute("icono", "bi-bug");
        return "error/500";
    }

    @RequestMapping("/error/403")
    public String error403(Model model) {
        model.addAttribute("codigoError", "403");
        model.addAttribute("tituloError", "Acceso Denegado");
        model.addAttribute("mensajeError", "No tienes permiso para acceder a esta página.");
        model.addAttribute("descripcionError",
                "Tu rol no tiene los privilegios necesarios para ver este contenido. " +
                "Contacta al administrador si crees que esto es un error.");
        model.addAttribute("icono", "bi-shield-lock");
        return "error/403";
    }
}
