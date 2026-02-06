package com.saas.platform.core.repository;

import com.saas.platform.core.model.entity.Suscripcion;
import com.saas.platform.core.model.enums.EstadoSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    Optional<Suscripcion> findByUsuarioId(Long usuarioId);

    List<Suscripcion> findByEstado(EstadoSuscripcion estado);

    /**
     * Busca suscripciones activas cuya próxima fecha de facturación sea anterior o igual a la fecha dada.
     * Se usa para generar facturas automáticas.
     */
    List<Suscripcion> findByEstadoAndProximaFechaFacturacionLessThanEqual(
            EstadoSuscripcion estado, LocalDate fecha);
}
