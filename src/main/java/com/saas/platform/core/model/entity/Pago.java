package com.saas.platform.core.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Clase base abstracta para Pagos.
 * Usa herencia SINGLE_TABLE con discriminador "tipo_pago".
 * Subclases: PagoTarjeta, PagoPaypal, PagoTransferencia.
 */
@Entity
@Table(name = "pagos")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_pago", discriminatorType = DiscriminatorType.STRING)
@Audited
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public abstract class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    private LocalDateTime fechaPago;

    @PrePersist
    protected void onCreate() {
        this.fechaPago = LocalDateTime.now();
    }
}
