package com.saas.platform.core.model.entity;

import com.saas.platform.core.model.enums.EstadoSuscripcion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad Suscripción: vincula un usuario a un plan con estado y fechas de facturación.
 */
@Entity
@Table(name = "suscripciones")
@Audited
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSuscripcion estado;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    /**
     * Próxima fecha en la que se generará la factura automática (cada 30 días).
     */
    @Column(nullable = false)
    private LocalDate proximaFechaFacturacion;

    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoSuscripcion.ACTIVA;
        }
    }
}
