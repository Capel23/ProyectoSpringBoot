package com.example.ProyectoSpringBoot.entity;

import com.example.ProyectoSpringBoot.enums.EstadoFactura;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Entidad Factura - auditada con Envers
@Entity
@Table(name = "facturas")
@Audited
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Número de factura obligatorio")
    @Column(name = "numero_factura", unique = true, nullable = false, length = 50)
    private String numeroFactura;

    @NotNull(message = "Fecha de emisión obligatoria")
    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @NotNull(message = "Fecha de vencimiento obligatoria")
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @NotNull(message = "Subtotal obligatorio")
    @DecimalMin(value = "0.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @NotNull(message = "Porcentaje impuestos obligatorio")
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    @Column(name = "porcentaje_impuestos", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeImpuestos;

    @NotNull(message = "Monto impuestos obligatorio")
    @DecimalMin(value = "0.00")
    @Column(name = "monto_impuestos", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoImpuestos;

    @NotNull(message = "Total obligatorio")
    @DecimalMin(value = "0.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @NotNull(message = "Estado obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoFactura estado;

    @NotBlank(message = "Concepto obligatorio")
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String concepto;

    @Column(name = "es_prorrateo", nullable = false)
    @Builder.Default
    private Boolean esProrrateo = false;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // Relación N:1 con Suscripcion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suscripcion_id", nullable = false)
    private Suscripcion suscripcion;

    // Relación N:1 con MetodoPago
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metodo_pago_id")
    private MetodoPago metodoPago;

    public boolean estaPendiente() {
        return estado == EstadoFactura.PENDIENTE;
    }

    public boolean estaVencida() {
        return estado == EstadoFactura.PENDIENTE && fechaVencimiento.isBefore(LocalDate.now());
    }
}
