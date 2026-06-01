package com.duoc.cloud.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.cloud.dto.CursoResumenDTO;
import com.duoc.cloud.dto.InscripcionRequestDTO;
import com.duoc.cloud.dto.InscripcionResumenDTO;
import com.duoc.cloud.model.Curso;
import com.duoc.cloud.model.Estudiante;
import com.duoc.cloud.model.Inscripcion;
import com.duoc.cloud.repository.EstudianteRepository;
import com.duoc.cloud.repository.InscripcionRepository;
import com.duoc.cloud.repository.S3Repository;

@Service
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final EstudianteRepository estudianteRepository;
    private final CursoService cursoService;
    private final S3Repository s3Repository;

    public InscripcionService(InscripcionRepository inscripcionRepository,
                              EstudianteRepository estudianteRepository,
                              CursoService cursoService,
                              S3Repository s3Repository) {
        this.inscripcionRepository = inscripcionRepository;
        this.estudianteRepository = estudianteRepository;
        this.cursoService = cursoService;
        this.s3Repository = s3Repository;
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

    // Método auxiliar para transformar el DTO de resumen a una Cadena legible (Formato Físico)
    public String generarTextoResumen(InscripcionResumenDTO resumen) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RESUMEN DE INSCRIPCIÓN N° ").append(resumen.getInscripcionId()).append(" ===\n");
        sb.append("Estudiante: ").append(resumen.getNombreEstudiante()).append("\n");
        sb.append("Fecha: ").append(resumen.getFecha()).append("\n");
        sb.append("Cursos Inscritos:\n");
        resumen.getCursos().forEach(c -> sb.append(" - ").append(c.getNombre()).append(" ($").append(c.getCosto()).append(")\n"));
        sb.append("Total a Pagar: $").append(resumen.getTotalPagar()).append("\n");
        return sb.toString();
    }
    
    // Lógica para Subir a S3
    public void guardarResumenEnS3(InscripcionResumenDTO resumen) {
        String folderName = String.valueOf(resumen.getInscripcionId()); // Nombre de la carpeta = Número del resumen
        String fileName = "resumen_" + resumen.getInscripcionId() + ".txt";
        String fileContent = generarTextoResumen(resumen);

        s3Repository.subirArchivo(folderName, fileName, fileContent.getBytes());
    }

    // Lógica para Descargar de S3
    public byte[] obtenerResumenDeS3(Long inscripcionId) {
        String folderName = String.valueOf(inscripcionId);
        String fileName = "resumen_" + inscripcionId + ".txt";
        
        return s3Repository.descargarArchivo(folderName, fileName);
    }

    // Lógica para Borrar de S3
    public void eliminarResumenDeS3(Long inscripcionId) {
        String folderName = String.valueOf(inscripcionId);
        String fileName = "resumen_" + inscripcionId + ".txt";

        s3Repository.borrarArchivo(folderName, fileName);
    }
}
