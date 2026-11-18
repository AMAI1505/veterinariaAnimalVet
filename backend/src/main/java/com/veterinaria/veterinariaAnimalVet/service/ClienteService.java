package com.veterinaria.veterinariaAnimalVet.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.veterinaria.veterinariaAnimalVet.model.Cliente;
import com.veterinaria.veterinariaAnimalVet.repository.ClienteRepository;

@Service
public class ClienteService {

    private final ClienteRepository repo;

    public ClienteService(ClienteRepository repo) {
        this.repo = repo;
    }

    public Cliente save(Cliente cliente) {
        return repo.save(cliente);
    }

    public Optional<Cliente> findByDni(String dni) { return repo.findByDni(dni); }
}
