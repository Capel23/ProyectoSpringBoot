package com.saas.platform.core.service;

import com.saas.platform.core.model.entity.Factura;
import com.saas.platform.core.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacturaService {

    private final FacturaRepository facturaRepository;

    public List<Factura> listarPorUsuario(Long usuarioId) {
        return facturaRepository.findBySuscripcionUsuarioId(usuarioId);
    }

    public List<Factura> listarPorSuscripcion(Long suscripcionId) {
        return facturaRepository.findBySuscripcionId(suscripcionId);
    }

    public Optional<Factura> buscarPorId(Long id) {
        return facturaRepository.findById(id);
    }

    public List<Factura> listarTodas() {
        return facturaRepository.findAll();
    }
}
