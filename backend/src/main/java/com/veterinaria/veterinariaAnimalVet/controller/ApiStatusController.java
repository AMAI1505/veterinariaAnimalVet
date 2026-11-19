package com.veterinaria.veterinariaAnimalVet.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiStatusController {
    @GetMapping({"/", "/api", "/api/"})
    public ApiStatus status() {
        return new ApiStatus("API AnimalVet encendida", "UP");
    }

    public record ApiStatus(String mensaje, String estado) { }
}
