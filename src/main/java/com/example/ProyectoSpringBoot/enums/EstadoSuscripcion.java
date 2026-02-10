package com.example.ProyectoSpringBoot.enums;

// Estados de suscripción
public enum EstadoSuscripcion {
    ACTIVA("Activa"),
    CANCELADA("Cancelada"),
    MOROSA("Morosa"),
    SUSPENDIDA("Suspendida"),
    EXPIRADA("Expirada");

    private final String descripcion;

    EstadoSuscripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
