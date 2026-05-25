package com.formativa.cloud.controller;

import com.formativa.cloud.dto.InscripcionRequestDTO;
import com.formativa.cloud.dto.InscripcionResumenDTO;
import com.formativa.cloud.service.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    // POST /api/inscripciones - inscribe a un estudiante en uno o mas cursos
    @PostMapping
    public ResponseEntity<InscripcionResumenDTO> inscribir(@Valid @RequestBody InscripcionRequestDTO request) {
        InscripcionResumenDTO resumen = inscripcionService.inscribir(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resumen);
    }

}
