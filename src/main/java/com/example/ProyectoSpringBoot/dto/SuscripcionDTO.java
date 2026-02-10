package com.example.ProyectoSpringBoot.dto;

import com.example.ProyectoSpringBoot.enums.EstadoSuscripcion;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para Suscripción
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuscripcionDTO {
    
    private Long id;
    
    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;
    private String usuarioNombre;
    
    @NotNull(message = "El plan es obligatorio")
    private Long planId;
    private String planNombre;
    
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;
    
    private LocalDate fechaFin;
    
    private LocalDate fechaProximoCobro;
    
    @NotNull(message = "El estado es obligatorio")
    private EstadoSuscripcion estado;
    
    private Boolean renovacionAutomatica;
    
    private BigDecimal precioActual;
}
