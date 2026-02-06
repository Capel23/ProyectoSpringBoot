package com.saas.platform.core.model.entity;

import com.saas.platform.core.model.enums.EstadoFactura;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad Factura: generada automáticamente cada 30 días por la suscripción.
 */
@Entity
@Table(name = "facturas")
@Audited
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suscripcion_id", nullable = false)
    private Suscripcion suscripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private String concepto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoFactura estado;

    @Column(nullable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    private LocalDateTime fechaCreacion;

    /**
     * Indica si esta factura es un prorrateo por cambio de plan.
     */
    @Column(nullable = false)
    private Boolean esProrrateo;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoFactura.PENDIENTE;
        }
        if (this.esProrrateo == null) {
            this.esProrrateo = false;
        }
    }
}
