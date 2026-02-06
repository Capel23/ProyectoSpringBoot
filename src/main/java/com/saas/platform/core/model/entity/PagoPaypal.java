package com.saas.platform.core.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

/**
 * Pago a través de PayPal.
 */
@Entity
@DiscriminatorValue("PAYPAL")
@Audited
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PagoPaypal extends Pago {

    @Column(name = "email_paypal")
    private String emailPaypal;

    @Column(name = "id_transaccion")
    private String idTransaccion;
}
