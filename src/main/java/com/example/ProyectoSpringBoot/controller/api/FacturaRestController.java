package com.example.ProyectoSpringBoot.controller.api;

import com.example.ProyectoSpringBoot.dto.FacturaDTO;
import com.example.ProyectoSpringBoot.enums.EstadoFactura;
import com.example.ProyectoSpringBoot.service.FacturaService;
import com.example.ProyectoSpringBoot.service.FacturacionScheduledService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST Controller para Facturas
 * Endpoints: /api/facturas
 */
@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
public class FacturaRestController {

    private final FacturaService facturaService;
    private final FacturacionScheduledService facturacionScheduledService;

    /**
     * GET /api/facturas - Obtener todas las facturas
     */
    @GetMapping
    public ResponseEntity<List<FacturaDTO>> getAll() {
        return ResponseEntity.ok(facturaService.findAll());
    }

    /**
     * GET /api/facturas/{id} - Obtener factura por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FacturaDTO> getById(@PathVariable Long id) {
        return facturaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/facturas/estado/{estado} - Facturas por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<FacturaDTO>> getByEstado(@PathVariable EstadoFactura estado) {
        return ResponseEntity.ok(facturaService.findByEstado(estado));
    }

    /**
     * GET /api/facturas/pendientes - Facturas pendientes de pago
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<FacturaDTO>> getPendientes() {
        return ResponseEntity.ok(facturaService.findByEstado(EstadoFactura.PENDIENTE));
    }

    /**
     * GET /api/facturas/suscripcion/{suscripcionId} - Facturas de una suscripción
     */
    @GetMapping("/suscripcion/{suscripcionId}")
    public ResponseEntity<List<FacturaDTO>> getBySuscripcion(@PathVariable Long suscripcionId) {
        return ResponseEntity.ok(facturaService.findBySuscripcionId(suscripcionId));
    }

    /**
     * POST /api/facturas/{id}/pagar - Marcar factura como pagada
     */
    @PostMapping("/{id}/pagar")
    public ResponseEntity<FacturaDTO> marcarComoPagada(@PathVariable Long id) {
        return facturaService.marcarComoPagada(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/facturas/ejecutar-facturacion - Ejecutar facturación manual
     * Útil para pruebas y para disparar facturación fuera del horario programado
     */
    @PostMapping("/ejecutar-facturacion")
    public ResponseEntity<Map<String, Object>> ejecutarFacturacionManual() {
        int facturasGeneradas = facturacionScheduledService.ejecutarFacturacionManual();
        
        return ResponseEntity.ok(Map.of(
                "mensaje", "Facturación ejecutada correctamente",
                "facturasGeneradas", facturasGeneradas
        ));
    }

    /**
     * DELETE /api/facturas/{id} - Eliminar factura
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (facturaService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ===== FILTROS AVANZADOS (PARTE 2) =====

    /**
     * GET /api/facturas/filtrar/fecha - Filtrar por rango de fechas
     */
    @GetMapping("/filtrar/fecha")
    public ResponseEntity<List<FacturaDTO>> filtrarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam(required = false) EstadoFactura estado) {
        
        if (estado != null) {
            return ResponseEntity.ok(facturaService.findByFechaYEstado(inicio, fin, estado));
        }
        return ResponseEntity.ok(facturaService.findByFechaEmisionBetween(inicio, fin));
    }

    /**
     * GET /api/facturas/filtrar/monto - Filtrar por rango de monto
     */
    @GetMapping("/filtrar/monto")
    public ResponseEntity<List<FacturaDTO>> filtrarPorMonto(
            @RequestParam BigDecimal minimo,
            @RequestParam BigDecimal maximo,
            @RequestParam(required = false) EstadoFactura estado) {
        
        if (estado != null) {
            return ResponseEntity.ok(facturaService.findByMontoYEstado(minimo, maximo, estado));
        }
        return ResponseEntity.ok(facturaService.findByTotalBetween(minimo, maximo));
    }

    /**
     * GET /api/facturas/buscar - Búsqueda avanzada con múltiples filtros
     */
    @GetMapping("/buscar")
    public ResponseEntity<Page<FacturaDTO>> buscarConFiltros(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) BigDecimal montoMinimo,
            @RequestParam(required = false) BigDecimal montoMaximo,
            @RequestParam(required = false) EstadoFactura estado,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaEmision") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(facturaService.buscarConFiltros(
                fechaInicio, fechaFin, montoMinimo, montoMaximo, estado, usuarioId, pageable
        ));
    }

    /**
     * GET /api/facturas/vencidas - Obtener facturas vencidas
     */
    @GetMapping("/vencidas")
    public ResponseEntity<List<FacturaDTO>> getVencidas() {
        return ResponseEntity.ok(facturaService.findVencidas());
    }

    /**
     * GET /api/facturas/pais/{pais} - Facturas por país del usuario
     */
    @GetMapping("/pais/{pais}")
    public ResponseEntity<List<FacturaDTO>> getByPais(@PathVariable String pais) {
        return ResponseEntity.ok(facturaService.findByPaisUsuario(pais));
    }

    // ===== ESTADÍSTICAS =====

    /**
     * GET /api/facturas/estadisticas - Obtener estadísticas de facturación
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<FacturaService.EstadisticasFacturacion> getEstadisticas() {
        return ResponseEntity.ok(facturaService.getEstadisticas());
    }

    /**
     * GET /api/facturas/resumen-estado - Resumen por estado
     */
    @GetMapping("/resumen-estado")
    public ResponseEntity<List<FacturaService.ResumenEstadoFactura>> getResumenPorEstado() {
        return ResponseEntity.ok(facturaService.getResumenPorEstado());
    }

    /**
     * GET /api/facturas/totales - Obtener totales por periodo
     */
    @GetMapping("/totales")
    public ResponseEntity<Map<String, Object>> getTotales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        
        BigDecimal totalFacturado = facturaService.getTotalFacturadoPorPeriodo(inicio, fin);
        BigDecimal totalImpuestos = facturaService.getImpuestosRecaudadosPorPeriodo(inicio, fin);
        
        return ResponseEntity.ok(Map.of(
                "periodo", Map.of("inicio", inicio, "fin", fin),
                "totalFacturado", totalFacturado,
                "totalImpuestos", totalImpuestos,
                "totalNeto", totalFacturado.subtract(totalImpuestos)
        ));
    }
}
