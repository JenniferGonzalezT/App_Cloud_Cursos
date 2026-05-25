package com.formativa.cloud.service;

import com.formativa.cloud.dto.CursoResumenDTO;
import com.formativa.cloud.dto.InscripcionRequestDTO;
import com.formativa.cloud.dto.InscripcionResumenDTO;
import com.formativa.cloud.model.Curso;
import com.formativa.cloud.model.Estudiante;
import com.formativa.cloud.model.Inscripcion;
import com.formativa.cloud.repository.EstudianteRepository;
import com.formativa.cloud.repository.InscripcionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final EstudianteRepository estudianteRepository;
    private final CursoService cursoService;

    public InscripcionService(InscripcionRepository inscripcionRepository,
                              EstudianteRepository estudianteRepository,
                              CursoService cursoService) {
        this.inscripcionRepository = inscripcionRepository;
        this.estudianteRepository = estudianteRepository;
        this.cursoService = cursoService;
    }

    @Transactional
    public InscripcionResumenDTO inscribir(InscripcionRequestDTO request) {
        // buscar el estudiante
        Estudiante estudiante = estudianteRepository.findById(request.getEstudianteId())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con id: " + request.getEstudianteId()));

        // buscar todos los cursos solicitados
        List<Curso> cursos = cursoService.buscarPorIds(request.getCursoIds());

        // calcular el total sumando el costo de cada curso
        BigDecimal total = cursos.stream()
                .map(Curso::getCosto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // crear y guardar la inscripcion
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setEstudiante(estudiante);
        inscripcion.setCursos(cursos);
        inscripcion.setTotalPagar(total);

        Inscripcion guardada = inscripcionRepository.save(inscripcion);

        // armar el resumen para la respuesta
        List<CursoResumenDTO> cursosResumen = cursos.stream()
                .map(c -> new CursoResumenDTO(c.getNombre(), c.getCosto()))
                .toList();

        InscripcionResumenDTO resumen = new InscripcionResumenDTO();
        resumen.setInscripcionId(guardada.getId());
        resumen.setNombreEstudiante(estudiante.getNombre());
        resumen.setFecha(guardada.getFecha());
        resumen.setCursos(cursosResumen);
        resumen.setTotalPagar(total);

        return resumen;
    }

}
