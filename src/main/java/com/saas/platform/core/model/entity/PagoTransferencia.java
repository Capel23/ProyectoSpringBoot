package com.saas.platform.core.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

/**
 * Pago por transferencia bancaria.
 */
@Entity
@DiscriminatorValue("TRANSFERENCIA")
@Audited
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PagoTransferencia extends Pago {

    @Column(name = "banco_origen")
    private String bancoOrigen;

    @Column(name = "numero_cuenta")
    private String numeroCuenta;

    @Column(name = "referencia_transferencia")
    private String referenciaTransferencia;
}
