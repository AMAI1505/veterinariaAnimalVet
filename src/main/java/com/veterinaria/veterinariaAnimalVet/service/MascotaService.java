package com.veterinaria.veterinariaAnimalVet.service;

import com.veterinaria.veterinariaAnimalVet.model.Mascota;
import com.veterinaria.veterinariaAnimalVet.repository.MascotaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MascotaService {

    private final MascotaRepository repo;

    public MascotaService(MascotaRepository repo) {
        this.repo = repo;
    }

    // Listar todas las mascotas → SELECT * FROM mascotas
    public List<Mascota> findAll() {
        return repo.findAll();
    }

    // Guardar (INSERT si es nueva, UPDATE si ya tiene ID)
    public Mascota save(Mascota mascota) {
        return repo.save(mascota);
    }

    // Buscar por ID → SELECT * FROM mascotas WHERE id=?
    public Optional<Mascota> findById(Long id) {
        return repo.findById(id);
    }

    // Eliminar → DELETE FROM mascotas WHERE id=?
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    // Verificar si existe
    public boolean existsById(Long id) {
        return repo.existsById(id);
    }
}
