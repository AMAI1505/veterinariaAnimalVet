package com.veterinaria.veterinariaAnimalVet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            jakarta.servlet.http.HttpServletRequest request,
                            Model model) {
        if (error != null) {
            String errorMessage = (String) request.getSession().getAttribute("errorMessage");
            if (errorMessage != null) {
                model.addAttribute("error", errorMessage);
                request.getSession().removeAttribute("errorMessage");
            } else {
                model.addAttribute("error", "Usuario o contraseña incorrectos. Intenta de nuevo.");
            }
        }
        if (logout != null) {
            model.addAttribute("logout", "Has cerrado sesión correctamente.");
        }
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/citas";
    }
}
