package com.saas.platform.core.repository;

import com.saas.platform.core.model.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByFacturaId(Long facturaId);
}
