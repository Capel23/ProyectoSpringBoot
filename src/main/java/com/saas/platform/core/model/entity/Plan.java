package com.saas.platform.core.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Plan: define los niveles de suscripción (Basic, Premium, Enterprise).
 */
@Entity
@Table(name = "planes")
@Audited
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMensual;

    @Column(nullable = false)
    private Integer maxUsuarios;

    @Column(nullable = false)
    private Boolean activo;

    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.activo == null) {
            this.activo = true;
        }
    }
}
