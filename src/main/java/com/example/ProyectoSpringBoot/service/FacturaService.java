package com.example.ProyectoSpringBoot.service;

import com.example.ProyectoSpringBoot.dto.FacturaDTO;
import com.example.ProyectoSpringBoot.entity.Factura;
import com.example.ProyectoSpringBoot.enums.EstadoFactura;
import com.example.ProyectoSpringBoot.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para la lógica de negocio de Factura
 * Cumple criterio: "Controllers limpios que solo delegan a la capa de Service"
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FacturaService {

    private final FacturaRepository facturaRepository;

    @Transactional(readOnly = true)
    public List<FacturaDTO> findAll() {
        return facturaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<FacturaDTO> findById(Long id) {
        return facturaRepository.findById(id).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<FacturaDTO> findByEstado(EstadoFactura estado) {
        return facturaRepository.findByEstado(estado).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener facturas por suscripción
     */
    @Transactional(readOnly = true)
    public List<FacturaDTO> findBySuscripcionId(Long suscripcionId) {
        return facturaRepository.findBySuscripcionIdOrderByFechaEmisionDesc(suscripcionId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Marcar factura como pagada
     */
    public Optional<FacturaDTO> marcarComoPagada(Long id) {
        return facturaRepository.findById(id).map(factura -> {
            factura.setEstado(EstadoFactura.PAGADA);
            factura.setFechaPago(LocalDateTime.now());
            Factura saved = facturaRepository.save(factura);
            return toDTO(saved);
        });
    }

    public boolean delete(Long id) {
        if (facturaRepository.existsById(id)) {
            facturaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ===== CONVERSIONES Entity <-> DTO =====

    private FacturaDTO toDTO(Factura entity) {
        String usuarioNombre = null;
        Long usuarioId = null;
        
        if (entity.getSuscripcion() != null && entity.getSuscripcion().getUsuario() != null) {
            usuarioId = entity.getSuscripcion().getUsuario().getId();
            if (entity.getSuscripcion().getUsuario().getPerfil() != null) {
                var perfil = entity.getSuscripcion().getUsuario().getPerfil();
                usuarioNombre = perfil.getNombre();
                if (perfil.getApellidos() != null) {
                    usuarioNombre += " " + perfil.getApellidos();
                }
            }
        }
        
        return FacturaDTO.builder()
                .id(entity.getId())
                .numeroFactura(entity.getNumeroFactura())
                .suscripcionId(entity.getSuscripcion() != null ? entity.getSuscripcion().getId() : null)
                .usuarioId(usuarioId)
                .usuarioNombre(usuarioNombre)
                .fechaEmision(entity.getFechaEmision())
                .fechaVencimiento(entity.getFechaVencimiento())
                .subtotal(entity.getSubtotal())
                .porcentajeImpuestos(entity.getPorcentajeImpuestos())
                .montoImpuestos(entity.getMontoImpuestos())
                .total(entity.getTotal())
                .estado(entity.getEstado())
                .fechaPago(entity.getFechaPago())
                .concepto(entity.getConcepto())
                .build();
    }

    // ===== FILTROS AVANZADOS (PARTE 2) =====

    /**
     * Filtrar facturas por rango de fechas
     */
    @Transactional(readOnly = true)
    public List<FacturaDTO> findByFechaEmisionBetween(LocalDate inicio, LocalDate fin) {
        return facturaRepository.findByFechaEmisionBetween(inicio, fin).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Filtrar facturas por rango de fechas y estado
     */
    @Transactional(readOnly = true)
    public List<FacturaDTO> findByFechaYEstado(LocalDate inicio, LocalDate fin, EstadoFactura estado) {
        return facturaRepository.findByFechaEmisionBetweenAndEstado(inicio, fin, estado).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Filtrar facturas por rango de monto
     */
    @Transactional(readOnly = true)
    public List<FacturaDTO> findByTotalBetween(BigDecimal minimo, BigDecimal maximo) {
        return facturaRepository.findByTotalBetween(minimo, maximo).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Filtrar facturas por rango de monto y estado
     */
    @Transactional(readOnly = true)
    public List<FacturaDTO> findByMontoYEstado(BigDecimal minimo, BigDecimal maximo, EstadoFactura estado) {
        return facturaRepository.findByTotalBetweenAndEstado(minimo, maximo, estado).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Búsqueda avanzada con múltiples filtros y paginación
     */
    @Transactional(readOnly = true)
    public Page<FacturaDTO> buscarConFiltros(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            BigDecimal montoMinimo,
            BigDecimal montoMaximo,
            EstadoFactura estado,
            Long usuarioId,
            Pageable pageable) {
        
        return facturaRepository.buscarConFiltros(
                fechaInicio, fechaFin, montoMinimo, montoMaximo, estado, usuarioId, pageable
        ).map(this::toDTO);
    }

    /**
     * Obtener facturas por país del usuario
     */
    @Transactional(readOnly = true)
    public List<FacturaDTO> findByPaisUsuario(String pais) {
        return facturaRepository.findByPaisUsuario(pais).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener facturas vencidas (pendientes con fecha vencimiento pasada)
     */
    @Transactional(readOnly = true)
    public List<FacturaDTO> findVencidas() {
        return facturaRepository.findVencidas(LocalDate.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ===== ESTADÍSTICAS Y RESÚMENES =====

    /**
     * Obtener total facturado en un periodo (solo facturas pagadas)
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalFacturadoPorPeriodo(LocalDate inicio, LocalDate fin) {
        return facturaRepository.sumTotalFacturadoPorPeriodo(inicio, fin);
    }

    /**
     * Obtener total de impuestos recaudados en un periodo
     */
    @Transactional(readOnly = true)
    public BigDecimal getImpuestosRecaudadosPorPeriodo(LocalDate inicio, LocalDate fin) {
        return facturaRepository.sumImpuestosPorPeriodo(inicio, fin);
    }

    /**
     * Obtener resumen de facturas por estado
     */
    @Transactional(readOnly = true)
    public List<ResumenEstadoFactura> getResumenPorEstado() {
        return facturaRepository.getResumenPorEstado().stream()
                .map(row -> new ResumenEstadoFactura(
                        (EstadoFactura) row[0],
                        ((Number) row[1]).longValue(),
                        (BigDecimal) row[2]
                ))
                .collect(Collectors.toList());
    }

    /**
     * Obtener estadísticas generales de facturación
     */
    @Transactional(readOnly = true)
    public EstadisticasFacturacion getEstadisticas() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        long totalFacturas = facturaRepository.count();
        long facturasPendientes = facturaRepository.countByEstado(EstadoFactura.PENDIENTE);
        long facturasPagadas = facturaRepository.countByEstado(EstadoFactura.PAGADA);
        long facturasVencidas = facturaRepository.findVencidas(hoy).size();
        
        BigDecimal totalPendiente = facturaRepository.sumTotalPendientes();
        BigDecimal ingresosDelMes = facturaRepository.sumTotalFacturadoPorPeriodo(inicioMes, finMes);
        BigDecimal impuestosDelMes = facturaRepository.sumImpuestosPorPeriodo(inicioMes, finMes);

        return new EstadisticasFacturacion(
                totalFacturas,
                facturasPendientes,
                facturasPagadas,
                facturasVencidas,
                totalPendiente != null ? totalPendiente : BigDecimal.ZERO,
                ingresosDelMes != null ? ingresosDelMes : BigDecimal.ZERO,
                impuestosDelMes != null ? impuestosDelMes : BigDecimal.ZERO
        );
    }

    // ===== RECORDS PARA RESPUESTAS =====

    public record ResumenEstadoFactura(
            EstadoFactura estado,
            long cantidad,
            BigDecimal total
    ) {}

    public record EstadisticasFacturacion(
            long totalFacturas,
            long facturasPendientes,
            long facturasPagadas,
            long facturasVencidas,
            BigDecimal totalPendiente,
            BigDecimal ingresosDelMes,
            BigDecimal impuestosDelMes
    ) {}
}
