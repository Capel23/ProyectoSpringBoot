package com.example.ProyectoSpringBoot.entity;

import com.example.ProyectoSpringBoot.enums.TipoMetodoPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.envers.Audited;

// PayPal como método de pago
@Entity
@DiscriminatorValue("PAYPAL")
@Audited
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PayPal extends MetodoPago {

    @NotBlank(message = "Email de PayPal obligatorio")
    @Email(message = "Formato de email no válido")
    @Size(max = 150)
    @Column(name = "email_paypal", length = 150)
    private String emailPaypal;

    @Column(name = "paypal_id", length = 100)
    private String paypalId;

    @Column(name = "cuenta_verificada")
    @Builder.Default
    private Boolean cuentaVerificada = false;

    @Override
    public TipoMetodoPago getTipoMetodoPago() {
        return TipoMetodoPago.PAYPAL;
    }

    @Override
    public String getMascarado() {
        if (emailPaypal == null || !emailPaypal.contains("@")) {
            return "***@***";
        }
        String[] partes = emailPaypal.split("@");
        String usuario = partes[0];
        if (usuario.length() <= 2) {
            return usuario + "***@" + partes[1];
        }
        return usuario.substring(0, 2) + "***@" + partes[1];
    }

    @Override
    public boolean esValido() {
        return emailPaypal != null && emailPaypal.contains("@");
    }
}
