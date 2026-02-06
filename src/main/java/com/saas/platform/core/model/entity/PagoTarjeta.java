package com.saas.platform.core.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

/**
 * Pago con tarjeta de crédito/débito.
 */
@Entity
@DiscriminatorValue("TARJETA")
@Audited
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PagoTarjeta extends Pago {

    @Column(name = "numero_tarjeta")
    private String numeroTarjeta;

    @Column(name = "titular_tarjeta")
    private String titularTarjeta;

    @Column(name = "fecha_expiracion")
    private String fechaExpiracion;
}
