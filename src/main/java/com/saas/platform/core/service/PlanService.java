package com.saas.platform.core.service;

import com.saas.platform.core.model.entity.Plan;
import com.saas.platform.core.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanService {

    private final PlanRepository planRepository;

    public Plan crear(Plan plan) {
        return planRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public List<Plan> listarTodos() {
        return planRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Plan> listarActivos() {
        return planRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public Optional<Plan> buscarPorId(Long id) {
        return planRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Plan> buscarPorNombre(String nombre) {
        return planRepository.findByNombre(nombre);
    }

    public Plan actualizar(Plan plan) {
        return planRepository.save(plan);
    }

    public void eliminar(Long id) {
        planRepository.deleteById(id);
    }
}
