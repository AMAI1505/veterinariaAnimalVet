package com.veterinaria.veterinariaAnimalVet.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.veterinaria.veterinariaAnimalVet.model.Cita;
import com.veterinaria.veterinariaAnimalVet.repository.CitaRepository;

@Service
public class CitaService {

    private final CitaRepository repo;

    public CitaService(CitaRepository repo) {
        this.repo = repo;
    }

    public List<Cita> findAll() {
        return repo.findAll();
    }

    public Cita save(Cita cita) {
        return repo.save(cita);
    }

    public Optional<Cita> findById(Long id) {
        return repo.findById(id);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public boolean existsById(Long id) {
        return repo.existsById(id);
    }

    public boolean existsByFechaHora(java.time.LocalDateTime fechaHora) {
        return repo.existsByFechaHora(fechaHora);
    }

    public boolean existsByFechaHoraAndIdNot(java.time.LocalDateTime fechaHora, Long id) {
        return repo.existsByFechaHoraAndIdNot(fechaHora, id);
    }
}
