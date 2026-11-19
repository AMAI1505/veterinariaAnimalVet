package com.veterinaria.veterinariaAnimalVet.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.veterinaria.veterinariaAnimalVet.model.Mascota;
import com.veterinaria.veterinariaAnimalVet.repository.MascotaRepository;

@Service
public class MascotaService {

    private final MascotaRepository repo;

    public MascotaService(MascotaRepository repo) {
        this.repo = repo;
    }

    public List<Mascota> findAll() {
        return repo.findAll();
    }

    public Mascota save(Mascota mascota) {
        return repo.save(mascota);
    }

    public Optional<Mascota> findById(Long id) {
        return repo.findById(id);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public boolean existsById(Long id) {
        return repo.existsById(id);
    }
}
