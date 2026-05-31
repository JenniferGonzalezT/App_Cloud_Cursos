package com.duoc.cloud.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.duoc.cloud.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Long> {
}
