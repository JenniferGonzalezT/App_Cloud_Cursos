package com.duoc.cloud.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.duoc.cloud.model.Curso;
import com.duoc.cloud.service.CursoService;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    // GET /api/cursos - lista todos los cursos disponibles
    @GetMapping
    public ResponseEntity<List<Curso>> listarCursos() {
        List<Curso> cursos = cursoService.listarCursos();
        return ResponseEntity.ok(cursos);
    }

    // POST /api/cursos - agrega un nuevo curso
    @PostMapping
    public ResponseEntity<Curso> agregarCurso(@Valid @RequestBody Curso curso) {
        Curso nuevo = cursoService.agregarCurso(curso);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // DELETE /api/cursos/id - agrega un nuevo curso
    @DeleteMapping("{id}")
    public ResponseEntity<String> eliminarCurso(@PathVariable Long id) {
        cursoService.eliminarCurso(id);
        return ResponseEntity.ok("Curso " + id + " eliminado.");
    }

}
