package com.saas.platform.core.repository;

import com.saas.platform.core.model.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    List<Factura> findBySuscripcionId(Long suscripcionId);

    List<Factura> findBySuscripcionUsuarioId(Long usuarioId);
}
