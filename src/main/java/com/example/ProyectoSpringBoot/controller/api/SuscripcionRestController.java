package com.example.ProyectoSpringBoot.controller.api;

import com.example.ProyectoSpringBoot.dto.SuscripcionDTO;
import com.example.ProyectoSpringBoot.enums.EstadoSuscripcion;
import com.example.ProyectoSpringBoot.service.SuscripcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller para Suscripciones
 * Endpoints: /api/suscripciones
 * 
 * Incluye endpoint para cambio de plan con prorrateo
 */
@RestController
@RequestMapping("/api/suscripciones")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
public class SuscripcionRestController {

    private final SuscripcionService suscripcionService;

    /**
     * GET /api/suscripciones - Obtener todas las suscripciones
     */
    @GetMapping
    public ResponseEntity<List<SuscripcionDTO>> getAll() {
        return ResponseEntity.ok(suscripcionService.findAll());
    }

    /**
     * GET /api/suscripciones/{id} - Obtener suscripción por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SuscripcionDTO> getById(@PathVariable Long id) {
        return suscripcionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/suscripciones/usuario/{usuarioId} - Suscripciones de un usuario
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<SuscripcionDTO>> getByUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(suscripcionService.findByUsuarioId(usuarioId));
    }

    /**
     * GET /api/suscripciones/estado/{estado} - Suscripciones por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SuscripcionDTO>> getByEstado(@PathVariable EstadoSuscripcion estado) {
        return ResponseEntity.ok(suscripcionService.findByEstado(estado));
    }

    /**
     * POST /api/suscripciones - Crear nueva suscripción
     */
    @PostMapping
    public ResponseEntity<SuscripcionDTO> create(@Valid @RequestBody SuscripcionDTO dto) {
        return suscripcionService.create(dto)
                .map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .orElse(ResponseEntity.badRequest().build());
    }

    /**
     * PUT /api/suscripciones/{id} - Actualizar suscripción
     */
    @PutMapping("/{id}")
    public ResponseEntity<SuscripcionDTO> update(@PathVariable Long id, @Valid @RequestBody SuscripcionDTO dto) {
        return suscripcionService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/suscripciones/{id}/cambiar-plan - Cambiar de plan con prorrateo
     * Si el nuevo plan es más caro, genera factura de prorrateo automáticamente
     * 
     * Body: { "planId": 2 }
     */
    @PostMapping("/{id}/cambiar-plan")
    public ResponseEntity<?> cambiarPlan(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long nuevoPlanId = body.get("planId");
        
        if (nuevoPlanId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Se requiere planId"));
        }

        return suscripcionService.cambiarPlan(id, nuevoPlanId)
                .map(s -> ResponseEntity.ok(Map.of(
                        "suscripcion", s,
                        "mensaje", "Plan actualizado correctamente. Si aplicaba, se generó factura de prorrateo."
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PATCH /api/suscripciones/{id}/estado - Cambiar estado de suscripción
     * 
     * Body: { "estado": "CANCELADA" }
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<SuscripcionDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        try {
            EstadoSuscripcion nuevoEstado = EstadoSuscripcion.valueOf(body.get("estado"));
            return suscripcionService.cambiarEstado(id, nuevoEstado)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DELETE /api/suscripciones/{id} - Eliminar suscripción
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (suscripcionService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
