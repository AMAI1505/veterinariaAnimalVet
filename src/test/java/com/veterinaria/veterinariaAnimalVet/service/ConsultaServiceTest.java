package com.veterinaria.veterinariaAnimalVet.service;

import com.veterinaria.veterinariaAnimalVet.model.Consulta;
import com.veterinaria.veterinariaAnimalVet.repository.ConsultaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {
    @Mock ConsultaRepository repository;
    @InjectMocks ConsultaService service;

    @Test
    void devuelveHistorialDeLaMascotaOrdenadoPorFecha() {
        when(repository.findByMascotaIdOrderByFechaDesc(12L)).thenReturn(List.of(new Consulta()));
        assertEquals(1, service.porMascota(12L).size());
        verify(repository).findByMascotaIdOrderByFechaDesc(12L);
    }

    @Test
    void guardaLaConsultaClinica() {
        Consulta consulta = new Consulta();
        when(repository.save(consulta)).thenReturn(consulta);
        assertEquals(consulta, service.guardar(consulta));
        verify(repository).save(consulta);
    }
}
