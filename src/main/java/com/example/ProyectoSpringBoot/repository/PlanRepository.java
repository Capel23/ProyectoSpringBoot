package com.example.ProyectoSpringBoot.repository;

import com.example.ProyectoSpringBoot.entity.Plan;
import com.example.ProyectoSpringBoot.enums.TipoPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// Repositorio de Plan
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    // Por tipo y nombre
    Optional<Plan> findByTipoPlan(TipoPlan tipoPlan);
    Optional<Plan> findByNombre(String nombre);
    boolean existsByNombre(String nombre);

    // Planes activos
    List<Plan> findByActivoTrue();
    List<Plan> findByActivoTrueOrderByPrecioMensualAsc();
    List<Plan> findByActivoTrueOrderByOrdenVisualizacionAsc();

    // Por precio
    List<Plan> findByPrecioMensualLessThanEqualAndActivoTrue(BigDecimal precio);
    List<Plan> findByPrecioMensualBetweenAndActivoTrue(BigDecimal min, BigDecimal max);

    // Con soporte prioritario
    List<Plan> findBySoportePrioritarioTrueAndActivoTrue();

    // Conteo de suscripciones por plan
    @Query("SELECT p.nombre, COUNT(s) FROM Plan p LEFT JOIN p.suscripciones s GROUP BY p.nombre")
    List<Object[]> countSuscripcionesPorPlan();
}
