package com.example.ProyectoSpringBoot.entity;

import com.example.ProyectoSpringBoot.enums.TipoMetodoPago;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// Clase base para métodos de pago - herencia SINGLE_TABLE
@Entity
@Table(name = "metodos_pago")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_metodo", discriminatorType = DiscriminatorType.STRING, length = 30)
@Audited
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public abstract class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String alias;

    @Column(name = "es_predeterminado", nullable = false)
    private Boolean esPredeterminado = false;

    @Column(nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // Relación N:1 con Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public abstract TipoMetodoPago getTipoMetodoPago();

    public abstract String getMascarado();

    public abstract boolean esValido();
}
