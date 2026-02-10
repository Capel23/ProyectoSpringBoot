package com.example.ProyectoSpringBoot.entity;

import com.example.ProyectoSpringBoot.enums.TipoPlan;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Entidad Plan - niveles de suscripción (BASIC, PREMIUM, ENTERPRISE)
@Entity
@Table(name = "planes")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotNull(message = "El tipo de plan es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_plan", nullable = false, length = 20)
    private TipoPlan tipoPlan;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "precio_mensual", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMensual;

    @Size(max = 500)
    @Column(length = 500)
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String caracteristicas;

    @Column(name = "max_usuarios")
    private Integer maxUsuarios;

    @Column(name = "almacenamiento_gb")
    private Integer almacenamientoGb;

    @Column(name = "soporte_prioritario", nullable = false)
    @Builder.Default
    private Boolean soportePrioritario = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "orden_visualizacion")
    @Builder.Default
    private Integer ordenVisualizacion = 0;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // Relación 1:N con Suscripcion
    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Suscripcion> suscripciones = new ArrayList<>();
}
