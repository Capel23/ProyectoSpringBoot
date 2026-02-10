package com.example.ProyectoSpringBoot.enums;

// Tipos de métodos de pago (herencia SINGLE_TABLE)
public enum TipoMetodoPago {
    TARJETA_CREDITO("Tarjeta de Crédito"),
    PAYPAL("PayPal"),
    TRANSFERENCIA("Transferencia Bancaria");

    private final String descripcion;

    TipoMetodoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
