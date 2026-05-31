package com.duoc.cloud.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class InscripcionRequestDTO {

    @NotNull(message = "El id del estudiante es obligatorio")
    private Long estudianteId;

    @NotEmpty(message = "Debe seleccionar al menos un curso")
    private List<Long> cursoIds;

}
