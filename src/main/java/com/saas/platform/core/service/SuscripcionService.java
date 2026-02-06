package com.saas.platform.core.service;

import com.saas.platform.core.model.entity.*;
import com.saas.platform.core.model.enums.EstadoFactura;
import com.saas.platform.core.model.enums.EstadoSuscripcion;
import com.saas.platform.core.repository.FacturaRepository;
import com.saas.platform.core.repository.SuscripcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;
    private final FacturaRepository facturaRepository;

    /**
     * Crea una suscripción para un usuario con un plan determinado.
     * Genera la primera factura automáticamente.
     */
    public Suscripcion crear(Usuario usuario, Plan plan) {
        // Verificar si ya tiene suscripción activa
        Optional<Suscripcion> existente = suscripcionRepository.findByUsuarioId(usuario.getId());
        if (existente.isPresent() && existente.get().getEstado() == EstadoSuscripcion.ACTIVA) {
            throw new IllegalStateException("El usuario ya tiene una suscripción activa.");
        }

        LocalDate hoy = LocalDate.now();

        Suscripcion suscripcion = Suscripcion.builder()
                .usuario(usuario)
                .plan(plan)
                .estado(EstadoSuscripcion.ACTIVA)
                .fechaInicio(hoy)
                .proximaFechaFacturacion(hoy.plusDays(30))
                .build();

        suscripcion = suscripcionRepository.save(suscripcion);

        // Generar primera factura
        generarFactura(suscripcion, plan.getPrecioMensual(), "Suscripción " + plan.getNombre() + " - Primera factura");

        return suscripcion;
    }

    /**
     * Cambia el plan de una suscripción.
     * Si el plan nuevo es más caro, se genera una factura de prorrateo.
     */
    public Suscripcion cambiarPlan(Long suscripcionId, Plan nuevoPlan) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Suscripción no encontrada"));

        if (suscripcion.getEstado() != EstadoSuscripcion.ACTIVA) {
            throw new IllegalStateException("Solo se puede cambiar el plan de una suscripción activa.");
        }

        Plan planAnterior = suscripcion.getPlan();

        // Calcular prorrateo si el nuevo plan es más caro
        if (nuevoPlan.getPrecioMensual().compareTo(planAnterior.getPrecioMensual()) > 0) {
            BigDecimal prorrateo = calcularProrrateo(suscripcion, planAnterior, nuevoPlan);
            generarFacturaProrrateo(suscripcion, prorrateo, planAnterior, nuevoPlan);
        }

        suscripcion.setPlan(nuevoPlan);
        return suscripcionRepository.save(suscripcion);
    }

    /**
     * Calcula el monto de prorrateo basado en los días restantes del ciclo actual.
     */
    public BigDecimal calcularProrrateo(Suscripcion suscripcion, Plan planAnterior, Plan nuevoPlan) {
        LocalDate hoy = LocalDate.now();
        LocalDate proximaFactura = suscripcion.getProximaFechaFacturacion();

        long diasRestantes = ChronoUnit.DAYS.between(hoy, proximaFactura);
        if (diasRestantes <= 0) {
            diasRestantes = 0;
        }

        BigDecimal diferenciaDiaria = nuevoPlan.getPrecioMensual()
                .subtract(planAnterior.getPrecioMensual())
                .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP);
        return diferenciaDiaria.multiply(BigDecimal.valueOf(diasRestantes))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Genera facturas automáticas para todas las suscripciones activas
     * cuya próxima fecha de facturación ya pasó.
     */
    public List<Factura> generarFacturasAutomaticas() {
        LocalDate hoy = LocalDate.now();
        List<Suscripcion> suscripciones = suscripcionRepository
                .findByEstadoAndProximaFechaFacturacionLessThanEqual(EstadoSuscripcion.ACTIVA, hoy);

        return suscripciones.stream()
                .map(s -> {
                    Factura factura = generarFactura(s, s.getPlan().getPrecioMensual(),
                            "Suscripción " + s.getPlan().getNombre() + " - Factura automática");
                    // Avanzar la próxima fecha de facturación 30 días
                    s.setProximaFechaFacturacion(s.getProximaFechaFacturacion().plusDays(30));
                    suscripcionRepository.save(s);
                    return factura;
                })
                .toList();
    }

    /**
     * Cancela una suscripción.
     */
    public Suscripcion cancelar(Long suscripcionId) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Suscripción no encontrada"));

        suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
        suscripcion.setFechaFin(LocalDate.now());
        return suscripcionRepository.save(suscripcion);
    }

    @Transactional(readOnly = true)
    public Optional<Suscripcion> buscarPorId(Long id) {
        return suscripcionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Suscripcion> buscarPorUsuarioId(Long usuarioId) {
        return suscripcionRepository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<Suscripcion> listarTodas() {
        return suscripcionRepository.findAll();
    }

    // --- Métodos privados ---

    private Factura generarFactura(Suscripcion suscripcion, BigDecimal monto, String concepto) {
        LocalDate hoy = LocalDate.now();
        Factura factura = Factura.builder()
                .suscripcion(suscripcion)
                .monto(monto)
                .concepto(concepto)
                .estado(EstadoFactura.PENDIENTE)
                .fechaEmision(hoy)
                .fechaVencimiento(hoy.plusDays(15))
                .esProrrateo(false)
                .build();
        return facturaRepository.save(factura);
    }

    private Factura generarFacturaProrrateo(Suscripcion suscripcion, BigDecimal monto,
                                             Plan planAnterior, Plan nuevoPlan) {
        LocalDate hoy = LocalDate.now();
        Factura factura = Factura.builder()
                .suscripcion(suscripcion)
                .monto(monto)
                .concepto("Prorrateo: Cambio de " + planAnterior.getNombre() + " a " + nuevoPlan.getNombre())
                .estado(EstadoFactura.PENDIENTE)
                .fechaEmision(hoy)
                .fechaVencimiento(hoy.plusDays(5))
                .esProrrateo(true)
                .build();
        return facturaRepository.save(factura);
    }
}
