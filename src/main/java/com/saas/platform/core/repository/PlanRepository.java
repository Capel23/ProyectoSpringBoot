package com.saas.platform.core.repository;

import com.saas.platform.core.model.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByNombre(String nombre);

    List<Plan> findByActivoTrue();
}
