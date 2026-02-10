package com.example.ProyectoSpringBoot.dto;

import com.example.ProyectoSpringBoot.enums.RolUsuario;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO para Usuario - No expone la entidad JPA directamente
 * Cumple criterio: No usar entidades JPA en entrada/salida de Controllers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {
    
    private Long id;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String email;
    
    private String password; // Solo para creación, nunca se devuelve
    
    private Boolean activo;
    
    private Boolean emailVerificado;
    
    private RolUsuario rol;
    
    private LocalDateTime fechaCreacion;
    
    private LocalDateTime ultimoAcceso;
    
    // ID de la suscripción activa
    private Long suscripcionId;
    
    // Datos del Perfil embebidos
    private String nombre;
    
    private String apellidos;
    
    private String telefono;
    
    private String pais;
}
