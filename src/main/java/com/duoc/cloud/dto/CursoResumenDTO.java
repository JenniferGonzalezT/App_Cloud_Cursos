package com.duoc.cloud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CursoResumenDTO {

    private String nombre;
    private BigDecimal costo;

}
