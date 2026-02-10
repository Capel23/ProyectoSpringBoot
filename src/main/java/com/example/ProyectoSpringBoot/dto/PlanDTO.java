package com.example.ProyectoSpringBoot.dto;

import com.example.ProyectoSpringBoot.enums.TipoPlan;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO para Plan - No expone la entidad JPA directamente
 * Cumple con el criterio de "Controllers limpios que solo delegan a Service"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDTO {
    
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @NotNull(message = "El tipo de plan es obligatorio")
    private TipoPlan tipoPlan;
    
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio debe ser positivo")
    private BigDecimal precioMensual;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    private String caracteristicas;
    
    @Min(value = 1, message = "Mínimo 1 usuario")
    private Integer maxUsuarios;
    
    @Min(value = 1, message = "Mínimo 1 GB de almacenamiento")
    private Integer almacenamientoGb;
    
    private Boolean soportePrioritario;
    
    private Boolean activo;
    
    private Integer ordenVisualizacion;
}
