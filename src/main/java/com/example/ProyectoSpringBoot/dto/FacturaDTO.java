package com.example.ProyectoSpringBoot.dto;

import com.example.ProyectoSpringBoot.enums.EstadoFactura;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para Factura - No expone la entidad JPA directamente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaDTO {
    
    private Long id;
    private String numeroFactura;
    private Long suscripcionId;
    private Long usuarioId;
    private String usuarioNombre;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private BigDecimal subtotal;
    private BigDecimal porcentajeImpuestos;
    private BigDecimal montoImpuestos;
    private BigDecimal total;
    private EstadoFactura estado;
    private LocalDateTime fechaPago;
    private String concepto;
}
