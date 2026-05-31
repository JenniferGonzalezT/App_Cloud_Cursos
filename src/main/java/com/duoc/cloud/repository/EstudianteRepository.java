package com.duoc.cloud.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.duoc.cloud.model.Estudiante;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
}
