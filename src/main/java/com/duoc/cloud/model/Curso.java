package com.duoc.cloud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "cursos")
@Data
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "curso_seq")
    @SequenceGenerator(name = "curso_seq", sequenceName = "CURSO_SEQ", allocationSize = 1)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false, length = 200)
    private String nombre;

    @NotBlank(message = "El instructor es obligatorio")
    @Column(nullable = false, length = 150)
    private String instructor;

    // duracion en horas
    @NotNull(message = "La duracion es obligatoria")
    @Positive(message = "La duracion debe ser mayor a 0")
    @Column(nullable = false)
    private Integer duracion;

    @NotNull(message = "El costo es obligatorio")
    @Positive(message = "El costo debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costo;

}
