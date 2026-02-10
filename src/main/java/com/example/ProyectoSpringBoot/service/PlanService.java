package com.example.ProyectoSpringBoot.service;

import com.example.ProyectoSpringBoot.dto.PlanDTO;
import com.example.ProyectoSpringBoot.entity.Plan;
import com.example.ProyectoSpringBoot.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para la lógica de negocio de Plan
 * Cumple criterio: "Controllers limpios que solo delegan a la capa de Service"
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PlanService {

    private final PlanRepository planRepository;

    /**
     * Obtiene todos los planes
     */
    @Transactional(readOnly = true)
    public List<PlanDTO> findAll() {
        return planRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los planes activos
     */
    @Transactional(readOnly = true)
    public List<PlanDTO> findAllActive() {
        return planRepository.findByActivoTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca un plan por ID
     */
    @Transactional(readOnly = true)
    public Optional<PlanDTO> findById(Long id) {
        return planRepository.findById(id).map(this::toDTO);
    }

    /**
     * Crea un nuevo plan
     */
    public PlanDTO create(PlanDTO dto) {
        Plan plan = toEntity(dto);
        plan.setId(null); // Asegurar que es nuevo
        Plan saved = planRepository.save(plan);
        return toDTO(saved);
    }

    /**
     * Actualiza un plan existente
     */
    public Optional<PlanDTO> update(Long id, PlanDTO dto) {
        return planRepository.findById(id).map(existing -> {
            updateEntity(existing, dto);
            Plan saved = planRepository.save(existing);
            return toDTO(saved);
        });
    }

    /**
     * Elimina un plan
     */
    public boolean delete(Long id) {
        if (planRepository.existsById(id)) {
            planRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Verifica si existe un plan con el nombre dado
     */
    @Transactional(readOnly = true)
    public boolean existsByNombre(String nombre) {
        return planRepository.existsByNombre(nombre);
    }

    // ===== MÉTODOS DE CONVERSIÓN (Entity <-> DTO) =====

    private PlanDTO toDTO(Plan entity) {
        return PlanDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .tipoPlan(entity.getTipoPlan())
                .precioMensual(entity.getPrecioMensual())
                .descripcion(entity.getDescripcion())
                .caracteristicas(entity.getCaracteristicas())
                .maxUsuarios(entity.getMaxUsuarios())
                .almacenamientoGb(entity.getAlmacenamientoGb())
                .soportePrioritario(entity.getSoportePrioritario())
                .activo(entity.getActivo())
                .ordenVisualizacion(entity.getOrdenVisualizacion())
                .build();
    }

    private Plan toEntity(PlanDTO dto) {
        return Plan.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .tipoPlan(dto.getTipoPlan())
                .precioMensual(dto.getPrecioMensual())
                .descripcion(dto.getDescripcion())
                .caracteristicas(dto.getCaracteristicas())
                .maxUsuarios(dto.getMaxUsuarios())
                .almacenamientoGb(dto.getAlmacenamientoGb())
                .soportePrioritario(dto.getSoportePrioritario() != null ? dto.getSoportePrioritario() : false)
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .ordenVisualizacion(dto.getOrdenVisualizacion() != null ? dto.getOrdenVisualizacion() : 0)
                .build();
    }

    private void updateEntity(Plan entity, PlanDTO dto) {
        entity.setNombre(dto.getNombre());
        entity.setTipoPlan(dto.getTipoPlan());
        entity.setPrecioMensual(dto.getPrecioMensual());
        entity.setDescripcion(dto.getDescripcion());
        entity.setCaracteristicas(dto.getCaracteristicas());
        entity.setMaxUsuarios(dto.getMaxUsuarios());
        entity.setAlmacenamientoGb(dto.getAlmacenamientoGb());
        if (dto.getSoportePrioritario() != null) {
            entity.setSoportePrioritario(dto.getSoportePrioritario());
        }
        if (dto.getActivo() != null) {
            entity.setActivo(dto.getActivo());
        }
        if (dto.getOrdenVisualizacion() != null) {
            entity.setOrdenVisualizacion(dto.getOrdenVisualizacion());
        }
    }
}
