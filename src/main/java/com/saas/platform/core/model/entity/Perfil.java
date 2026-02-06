package com.saas.platform.core.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

/**
 * Entidad Perfil: información adicional del usuario.
 */
@Entity
@Table(name = "perfiles")
@Audited
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String telefono;

    private String direccion;

    private String empresa;

    private String cargo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;
}
