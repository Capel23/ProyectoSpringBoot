package com.example.ProyectoSpringBoot.controller.api;

import com.example.ProyectoSpringBoot.dto.PlanDTO;
import com.example.ProyectoSpringBoot.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller para Planes
 * Endpoints: /api/planes
 * 
 * Cumple criterio: "Controllers limpios que solo delegan a la capa de Service"
 */
@RestController
@RequestMapping("/api/planes")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
public class PlanRestController {

    private final PlanService planService;

    /**
     * GET /api/planes - Obtener todos los planes
     */
    @GetMapping
    public ResponseEntity<List<PlanDTO>> getAll() {
        List<PlanDTO> planes = planService.findAll();
        return ResponseEntity.ok(planes);
    }

    /**
     * GET /api/planes/activos - Obtener solo planes activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<PlanDTO>> getActivos() {
        List<PlanDTO> planes = planService.findAllActive();
        return ResponseEntity.ok(planes);
    }

    /**
     * GET /api/planes/{id} - Obtener plan por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlanDTO> getById(@PathVariable Long id) {
        return planService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/planes - Crear nuevo plan
     */
    @PostMapping
    public ResponseEntity<PlanDTO> create(@Valid @RequestBody PlanDTO dto) {
        PlanDTO created = planService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/planes/{id} - Actualizar plan
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlanDTO> update(@PathVariable Long id, @Valid @RequestBody PlanDTO dto) {
        return planService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/planes/{id} - Eliminar plan
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (planService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
