package com.duoc.cloud.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InscripcionResumenDTO {

    private Long inscripcionId;
    private String nombreEstudiante;
    private LocalDateTime fecha;
    private List<CursoResumenDTO> cursos;
    private BigDecimal totalPagar;

}
