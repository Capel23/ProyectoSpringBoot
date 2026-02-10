package com.example.ProyectoSpringBoot.controller.api;

import com.example.ProyectoSpringBoot.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller para Auditoría (Admin)
 * Endpoints: /api/auditoria
 * 
 * Cumple criterio Parte 2: "Panel de Auditoría (Admin): Una vista especial 
 * para ver el historial de cambios en las suscripciones o facturas usando Envers."
 */
@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
public class AuditoriaRestController {

    private final AuditoriaService auditoriaService;

    /**
     * GET /api/auditoria/recientes - Obtener cambios recientes
     */
    @GetMapping("/recientes")
    public ResponseEntity<List<AuditoriaService.RegistroAuditoria>> getCambiosRecientes(
            @RequestParam(defaultValue = "50") int limite) {
        return ResponseEntity.ok(auditoriaService.obtenerCambiosRecientes(limite));
    }

    /**
     * GET /api/auditoria/suscripciones - Historial de suscripciones
     */
    @GetMapping("/suscripciones")
    public ResponseEntity<List<AuditoriaService.RegistroAuditoria>> getHistorialSuscripciones(
            @RequestParam(defaultValue = "50") int limite) {
        return ResponseEntity.ok(auditoriaService.obtenerHistorialSuscripciones(limite));
    }

    /**
     * GET /api/auditoria/facturas - Historial de facturas
     */
    @GetMapping("/facturas")
    public ResponseEntity<List<AuditoriaService.RegistroAuditoria>> getHistorialFacturas(
            @RequestParam(defaultValue = "50") int limite) {
        return ResponseEntity.ok(auditoriaService.obtenerHistorialFacturas(limite));
    }

    /**
     * GET /api/auditoria/usuarios - Historial de usuarios
     */
    @GetMapping("/usuarios")
    public ResponseEntity<List<AuditoriaService.RegistroAuditoria>> getHistorialUsuarios(
            @RequestParam(defaultValue = "50") int limite) {
        return ResponseEntity.ok(auditoriaService.obtenerHistorialUsuarios(limite));
    }

    /**
     * GET /api/auditoria/entidad/{tipo}/{id} - Historial completo de una entidad
     */
    @GetMapping("/entidad/{tipo}/{id}")
    public ResponseEntity<List<AuditoriaService.RegistroAuditoria>> getHistorialEntidad(
            @PathVariable String tipo,
            @PathVariable Long id) {
        return ResponseEntity.ok(auditoriaService.obtenerHistorialEntidad(tipo, id));
    }

    /**
     * GET /api/auditoria/comparar - Comparar dos revisiones de una entidad
     */
    @GetMapping("/comparar")
    public ResponseEntity<AuditoriaService.ComparacionRevisiones> compararRevisiones(
            @RequestParam String tipoEntidad,
            @RequestParam Long entityId,
            @RequestParam Long revisionAnterior,
            @RequestParam Long revisionActual) {
        return ResponseEntity.ok(auditoriaService.compararRevisiones(
                tipoEntidad, entityId, revisionAnterior, revisionActual));
    }

    /**
     * GET /api/auditoria/estadisticas - Estadísticas de auditoría
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<AuditoriaService.EstadisticasAuditoria> getEstadisticas() {
        return ResponseEntity.ok(auditoriaService.obtenerEstadisticas());
    }
}
