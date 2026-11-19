package com.veterinaria.veterinariaAnimalVet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.veterinaria.veterinariaAnimalVet.model.Usuario;
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);

    @Query(value = "select u.* from usuarios u join roles r on r.id = u.rol_id " +
            "where u.id = :id and r.nombre = 'PERSONAL_VETERINARIA' and u.tipo_personal in ('VETERINARIO', 'ESTILISTA') and u.enabled = true for update", nativeQuery = true)
    Optional<Usuario> findPersonalForUpdate(@Param("id") Long id);

    @Query("select u from Usuario u join u.rol r where r.nombre = 'PERSONAL_VETERINARIA' and u.tipoPersonal in ('VETERINARIO', 'ESTILISTA') and u.enabled = true order by u.username")
    java.util.List<Usuario> findPersonalActivoParaCitas();
}
