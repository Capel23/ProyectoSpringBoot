package com.example.ProyectoSpringBoot.entity;

import com.example.ProyectoSpringBoot.enums.TipoMetodoPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.envers.Audited;

@Entity
@DiscriminatorValue("TRANSFERENCIA")
@Audited
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transferencia extends MetodoPago {

    @NotBlank(message = "Nombre del banco obligatorio")
    @Size(max = 100)
    @Column(name = "nombre_banco", length = 100)
    private String nombreBanco;

    @NotBlank(message = "IBAN obligatorio")
    @Column(name = "iban", length = 255)
    private String iban;

    @Size(max = 11)
    @Column(name = "codigo_swift", length = 11)
    private String codigoSwift;

    @NotBlank(message = "Nombre del titular obligatorio")
    @Size(max = 150)
    @Column(name = "nombre_titular", length = 150)
    private String nombreTitular;

    @Size(max = 50)
    @Column(name = "pais_banco", length = 50)
    private String paisBanco;

    @Override
    public TipoMetodoPago getTipoMetodoPago() {
        return TipoMetodoPago.TRANSFERENCIA;
    }

    @Override
    public String getMascarado() {
        if (iban == null || iban.length() < 4) {
            return "****";
        }
        return "****" + iban.substring(iban.length() - 4);
    }

    @Override
    public boolean esValido() {
        return iban != null && iban.length() >= 15 && iban.length() <= 34;
    }
}
