package com.example.ProyectoSpringBoot.enums;

/**
 * Roles de usuario en el sistema
 */
public enum RolUsuario {
    USER("Usuario"),
    ADMIN("Administrador");
    
    private final String descripcion;
    
    RolUsuario(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}
