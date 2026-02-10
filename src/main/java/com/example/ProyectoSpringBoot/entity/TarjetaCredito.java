package com.example.ProyectoSpringBoot.entity;

import com.example.ProyectoSpringBoot.enums.TipoMetodoPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.YearMonth;

@Entity
@DiscriminatorValue("TARJETA_CREDITO")
@Audited
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TarjetaCredito extends MetodoPago {

    @NotBlank(message = "Nombre del titular obligatorio")
    @Size(max = 150)
    @Column(name = "nombre_titular", length = 150)
    private String nombreTitular;

    @NotBlank(message = "Número de tarjeta obligatorio")
    @Column(name = "numero_tarjeta", length = 255)
    private String numeroTarjeta;

    @NotNull(message = "Mes de expiración obligatorio")
    @Min(1) @Max(12)
    @Column(name = "mes_expiracion")
    private Integer mesExpiracion;

    @NotNull(message = "Año de expiración obligatorio")
    @Min(2024)
    @Column(name = "anio_expiracion")
    private Integer anioExpiracion;

    @Column(name = "cvv", length = 255)
    private String cvv;

    @Column(name = "marca_tarjeta", length = 50)
    private String marcaTarjeta;

    @Override
    public TipoMetodoPago getTipoMetodoPago() {
        return TipoMetodoPago.TARJETA_CREDITO;
    }

    @Override
    public String getMascarado() {
        if (numeroTarjeta == null || numeroTarjeta.length() < 4) {
            return "**** **** **** ****";
        }
        return "**** **** **** " + numeroTarjeta.substring(numeroTarjeta.length() - 4);
    }

    @Override
    public boolean esValido() {
        if (mesExpiracion == null || anioExpiracion == null) return false;
        YearMonth expiracion = YearMonth.of(anioExpiracion, mesExpiracion);
        return expiracion.isAfter(YearMonth.now()) || expiracion.equals(YearMonth.now());
    }
}
