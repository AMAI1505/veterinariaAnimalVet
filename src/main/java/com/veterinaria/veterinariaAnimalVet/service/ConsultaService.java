package com.veterinaria.veterinariaAnimalVet.service;

import com.veterinaria.veterinariaAnimalVet.model.Consulta;
import com.veterinaria.veterinariaAnimalVet.repository.ConsultaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ConsultaService {
    private final ConsultaRepository repository;
    public ConsultaService(ConsultaRepository repository) { this.repository = repository; }
    public List<Consulta> porMascota(Long mascotaId) { return repository.findByMascotaIdOrderByFechaDesc(mascotaId); }
    public Consulta guardar(Consulta consulta) { return repository.save(consulta); }
}
