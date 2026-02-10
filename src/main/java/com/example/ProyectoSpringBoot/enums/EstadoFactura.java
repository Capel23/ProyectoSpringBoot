package com.example.ProyectoSpringBoot.enums;

// Estados de factura
public enum EstadoFactura {
    PENDIENTE("Pendiente"),
    PAGADA("Pagada"),
    VENCIDA("Vencida"),
    CANCELADA("Cancelada"),
    REEMBOLSADA("Reembolsada");

    private final String descripcion;

    EstadoFactura(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
