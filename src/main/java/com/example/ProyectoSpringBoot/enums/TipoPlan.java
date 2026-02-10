package com.example.ProyectoSpringBoot.enums;

// Tipos de planes disponibles
public enum TipoPlan {
    BASIC("Plan Básico"),
    PREMIUM("Plan Premium"),
    ENTERPRISE("Plan Empresarial");

    private final String descripcion;

    TipoPlan(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
