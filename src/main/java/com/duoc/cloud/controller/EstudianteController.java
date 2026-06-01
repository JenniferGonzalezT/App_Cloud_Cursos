package com.duoc.cloud.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.cloud.model.Estudiante;
import com.duoc.cloud.service.EstudianteService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {
    
    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    // POST /api/estudiantes - Crea un estudiante
    @PostMapping()
    public ResponseEntity<Estudiante> agregarEstudiante(@Valid @RequestBody Estudiante estudiante) {
        Estudiante nuevo = estudianteService.agregarEstudiante(estudiante);

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // GET /api/estudiantes - Listar estudiantes
    @GetMapping()
    public ResponseEntity<List<Estudiante>> listarEstudiantes() {
        return ResponseEntity.ok(estudianteService.listarEstudiantes());
    }
    
    // DELETE /api/estudiantes/id - Eliminar estudiante por id
    @DeleteMapping("{id}")
    public ResponseEntity<String> eliminarEstudiante(@PathVariable Long id) {
        estudianteService.eliminarEstudiante(id);

        return ResponseEntity.ok("Estudiante " + id + "eliminado.");
    }
}
