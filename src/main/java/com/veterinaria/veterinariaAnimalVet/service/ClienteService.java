package com.veterinaria.veterinariaAnimalVet.service;

import java.util.List;
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

    public List<Cliente> findAll() {
        return repo.findAll();
    }

    public Cliente save(Cliente cliente) {
        return repo.save(cliente);
    }

    public Optional<Cliente> findById(Long id) {
        return repo.findById(id);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public boolean existsById(Long id) {
        return repo.existsById(id);
    }

    public boolean existsByDni(String dni) {
        return repo.existsByDni(dni);
    }

    public boolean existsByDniAndIdNot(String dni, Long id) {
        return repo.existsByDniAndIdNot(dni, id);
    }
}
