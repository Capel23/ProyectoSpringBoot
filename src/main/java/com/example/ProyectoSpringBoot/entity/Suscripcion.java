package com.example.ProyectoSpringBoot.entity;

import com.example.ProyectoSpringBoot.enums.EstadoSuscripcion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Entidad Suscripcion - auditada con Envers para historial de cambios
@Entity
@Table(name = "suscripciones")
@Audited
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @NotNull(message = "La fecha del próximo cobro es obligatoria")
    @Column(name = "fecha_proximo_cobro", nullable = false)
    private LocalDate fechaProximoCobro;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSuscripcion estado;

    @Column(name = "renovacion_automatica", nullable = false)
    @Builder.Default
    private Boolean renovacionAutomatica = true;

    @Column(name = "precio_actual", precision = 10, scale = 2)
    private BigDecimal precioActual;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;

    // Auditoría JPA
    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @CreatedBy
    @Column(name = "creado_por", length = 100)
    private String creadoPor;

    @LastModifiedBy
    @Column(name = "modificado_por", length = 100)
    private String modificadoPor;

    // Relación N:1 con Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Relación N:1 con Plan (no auditada)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @NotAudited
    private Plan plan;

    // Relación 1:N con Factura (no auditada)
    @OneToMany(mappedBy = "suscripcion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @NotAudited
    private List<Factura> facturas = new ArrayList<>();

    public boolean estaActiva() {
        return estado == EstadoSuscripcion.ACTIVA;
    }
}
