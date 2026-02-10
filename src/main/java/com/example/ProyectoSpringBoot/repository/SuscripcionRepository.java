package com.example.ProyectoSpringBoot.repository;

import com.example.ProyectoSpringBoot.entity.Suscripcion;
import com.example.ProyectoSpringBoot.enums.EstadoSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Repositorio de Suscripcion
@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    // Por usuario
    List<Suscripcion> findByUsuarioId(Long usuarioId);
    Optional<Suscripcion> findByUsuarioIdAndEstado(Long usuarioId, EstadoSuscripcion estado);

    // Por estado y plan
    List<Suscripcion> findByEstado(EstadoSuscripcion estado);
    List<Suscripcion> findByPlanId(Long planId);

    // Para renovación automática
    @Query("SELECT s FROM Suscripcion s WHERE s.estado = 'ACTIVA' " +
           "AND s.renovacionAutomatica = true AND s.fechaProximoCobro <= :fecha")
    List<Suscripcion> findSuscripcionesParaRenovar(@Param("fecha") LocalDate fecha);

    // Próximas a vencer
    @Query("SELECT s FROM Suscripcion s WHERE s.estado = 'ACTIVA' " +
           "AND s.fechaProximoCobro BETWEEN :hoy AND :limite")
    List<Suscripcion> findProximasARenovar(@Param("hoy") LocalDate hoy, @Param("limite") LocalDate limite);

    // Morosas
    @Query("SELECT s FROM Suscripcion s WHERE s.estado = 'ACTIVA' AND s.fechaProximoCobro < :fecha")
    List<Suscripcion> findMorosas(@Param("fecha") LocalDate fecha);

    // Conteo por estado
    long countByEstado(EstadoSuscripcion estado);

    // Con relaciones cargadas
    @Query("SELECT s FROM Suscripcion s JOIN FETCH s.usuario JOIN FETCH s.plan WHERE s.estado = :estado")
    List<Suscripcion> findByEstadoConDetalles(@Param("estado") EstadoSuscripcion estado);
}
