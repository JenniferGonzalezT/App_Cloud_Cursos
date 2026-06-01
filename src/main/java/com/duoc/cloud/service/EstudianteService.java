package com.duoc.cloud.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.duoc.cloud.model.Estudiante;
import com.duoc.cloud.repository.EstudianteRepository;

@Service
public class EstudianteService {
    
    private final EstudianteRepository estudianteRepository;

    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    public List<Estudiante> listarEstudiantes() {
        return estudianteRepository.findAll();
    }

    public Estudiante agregarEstudiante(Estudiante estudiante) {
        return estudianteRepository.save(estudiante);
    }

    public void eliminarEstudiante(Long id) {
        estudianteRepository.deleteById(id);
    }
}
