package com.duoc.cloud.service;

import org.springframework.stereotype.Service;

import com.duoc.cloud.model.Curso;
import com.duoc.cloud.repository.CursoRepository;

import java.util.List;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    public List<Curso> listarCursos() {
        return cursoRepository.findAll();
    }

    public Curso agregarCurso(Curso curso) {
        return cursoRepository.save(curso);
    }

    // busca cursos por sus ids, lanza excepcion si alguno no existe
    public List<Curso> buscarPorIds(List<Long> ids) {
        List<Curso> cursos = cursoRepository.findAllById(ids);

        if (cursos.size() != ids.size()) {
            throw new RuntimeException("Uno o mas cursos no fueron encontrados");
        }

        return cursos;
    }

}
