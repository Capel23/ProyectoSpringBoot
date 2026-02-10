package com.example.ProyectoSpringBoot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// Entidad Perfil - información personal del usuario
@Entity
@Table(name = "perfiles")
@Audited
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 100)
    @Column(length = 100)
    private String apellidos;

    @Size(max = 20)
    @Column(length = 20)
    private String telefono;

    @Size(max = 255)
    @Column(length = 255)
    private String direccion;

    @Size(max = 100)
    @Column(length = 100)
    private String ciudad;

    @Size(max = 20)
    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Size(max = 100)
    @Column(length = 100)
    private String pais;

    @Size(max = 50)
    @Column(name = "nif_cif", length = 50)
    private String nifCif;

    @Column(name = "empresa")
    private String empresa;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // Relación 1:1 con Usuario
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    public String getNombreCompleto() {
        if (apellidos != null && !apellidos.isEmpty()) {
            return nombre + " " + apellidos;
        }
        return nombre;
    }
}
