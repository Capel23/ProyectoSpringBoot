package com.example.ProyectoSpringBoot.controller.api;

import com.example.ProyectoSpringBoot.dto.SuscripcionDTO;
import com.example.ProyectoSpringBoot.service.CicloVidaSuscripcionService;
import com.example.ProyectoSpringBoot.service.SuscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller para Ciclo de Vida de Suscripciones
 * Endpoints: /api/suscripciones/ciclo-vida
 * 
 * Cumple criterio Parte 2: "Para manejar el ciclo de vida de la suscripción"
 */
@RestController
@RequestMapping("/api/suscripciones/ciclo-vida")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
public class CicloVidaSuscripcionController {

    private final CicloVidaSuscripcionService cicloVidaService;
    private final SuscripcionService suscripcionService;

    /**
     * POST /api/suscripciones/ciclo-vida/{id}/cancelar - Cancelar suscripción
     */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<SuscripcionDTO> cancelarSuscripcion(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Sin motivo especificado") String motivo) {
        try {
            var suscripcion = cicloVidaService.cancelarSuscripcion(id, motivo);
            return ResponseEntity.ok(suscripcionService.toDTO(suscripcion));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/suscripciones/ciclo-vida/{id}/reactivar - Reactivar suscripción cancelada
     */
    @PostMapping("/{id}/reactivar")
    public ResponseEntity<SuscripcionDTO> reactivarSuscripcion(@PathVariable Long id) {
        try {
            var suscripcion = cicloVidaService.reactivarSuscripcion(id);
            return ResponseEntity.ok(suscripcionService.toDTO(suscripcion));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * POST /api/suscripciones/ciclo-vida/{id}/toggle-renovacion - Alternar renovación automática
     */
    @PostMapping("/{id}/toggle-renovacion")
    public ResponseEntity<SuscripcionDTO> toggleRenovacionAutomatica(
            @PathVariable Long id,
            @RequestParam boolean renovacionAutomatica) {
        try {
            var suscripcion = cicloVidaService.toggleRenovacionAutomatica(id, renovacionAutomatica);
            return ResponseEntity.ok(suscripcionService.toDTO(suscripcion));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/suscripciones/ciclo-vida/estadisticas - Estadísticas del ciclo de vida
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<CicloVidaSuscripcionService.EstadisticasCicloVida> getEstadisticas() {
        return ResponseEntity.ok(cicloVidaService.obtenerEstadisticas());
    }

    /**
     * POST /api/suscripciones/ciclo-vida/ejecutar-renovaciones - Ejecutar renovaciones manualmente
     * Solo para administradores/pruebas
     */
    @PostMapping("/ejecutar-renovaciones")
    public ResponseEntity<Map<String, Object>> ejecutarRenovaciones() {
        int renovadas = cicloVidaService.ejecutarRenovacionesManual();
        return ResponseEntity.ok(Map.of(
                "mensaje", "Proceso de renovaciones completado",
                "suscripcionesRenovadas", renovadas
        ));
    }

    /**
     * POST /api/suscripciones/ciclo-vida/ejecutar-morosos - Procesar morosos manualmente
     * Solo para administradores/pruebas
     */
    @PostMapping("/ejecutar-morosos")
    public ResponseEntity<Map<String, Object>> ejecutarProcesoMorosos() {
        int procesados = cicloVidaService.ejecutarProcesoMorososManual();
        return ResponseEntity.ok(Map.of(
                "mensaje", "Proceso de morosos completado",
                "suscripcionesProcesadas", procesados
        ));
    }

    /**
     * POST /api/suscripciones/ciclo-vida/ejecutar-suspensiones - Procesar suspensiones manualmente
     * Solo para administradores/pruebas
     */
    @PostMapping("/ejecutar-suspensiones")
    public ResponseEntity<Map<String, Object>> ejecutarProcesoSuspensiones() {
        int procesados = cicloVidaService.ejecutarProcesoSuspensionesManual();
        return ResponseEntity.ok(Map.of(
                "mensaje", "Proceso de suspensiones completado",
                "suscripcionesProcesadas", procesados
        ));
    }

    /**
     * POST /api/suscripciones/ciclo-vida/ejecutar-expiraciones - Procesar expiraciones manualmente
     * Solo para administradores/pruebas
     */
    @PostMapping("/ejecutar-expiraciones")
    public ResponseEntity<Map<String, Object>> ejecutarProcesoExpiraciones() {
        int procesados = cicloVidaService.ejecutarProcesoExpiracionesManual();
        return ResponseEntity.ok(Map.of(
                "mensaje", "Proceso de expiraciones completado",
                "suscripcionesProcesadas", procesados
        ));
    }
}
