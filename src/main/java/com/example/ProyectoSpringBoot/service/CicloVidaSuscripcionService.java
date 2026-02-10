package com.example.ProyectoSpringBoot.service;

import com.example.ProyectoSpringBoot.entity.Factura;
import com.example.ProyectoSpringBoot.entity.Suscripcion;
import com.example.ProyectoSpringBoot.enums.EstadoFactura;
import com.example.ProyectoSpringBoot.enums.EstadoSuscripcion;
import com.example.ProyectoSpringBoot.repository.FacturaRepository;
import com.example.ProyectoSpringBoot.repository.SuscripcionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para manejar el ciclo de vida completo de las suscripciones.
 * Incluye: renovación automática, gestión de estados, manejo de morosos.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CicloVidaSuscripcionService {

    private final SuscripcionRepository suscripcionRepository;
    private final FacturaRepository facturaRepository;
    private final SuscripcionService suscripcionService;

    // Días de gracia antes de marcar como morosa
    private static final int DIAS_GRACIA = 7;
    
    // Días de impago antes de suspender
    private static final int DIAS_PARA_SUSPENDER = 30;
    
    // Días de suspensión antes de expirar
    private static final int DIAS_PARA_EXPIRAR = 60;

    /**
     * Procesa renovaciones automáticas diariamente a las 00:00
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void procesarRenovacionesAutomaticas() {
        log.info("=== Iniciando proceso de renovación automática ===");
        
        LocalDate hoy = LocalDate.now();
        List<Suscripcion> suscripcionesParaRenovar = suscripcionRepository
                .findSuscripcionesParaRenovar(hoy);
        
        int renovadas = 0;
        int noRenovadas = 0;
        
        for (Suscripcion suscripcion : suscripcionesParaRenovar) {
            try {
                if (suscripcion.getRenovacionAutomatica()) {
                    // Verificar si tiene facturas pendientes
                    if (tieneFacturasPendientes(suscripcion)) {
                        log.warn("Suscripción {} tiene facturas pendientes, no se renueva", 
                                suscripcion.getId());
                        noRenovadas++;
                        continue;
                    }
                    
                    // Generar factura de renovación
                    Factura factura = suscripcionService.generarFacturaMensual(suscripcion);
                    log.info("Renovación automática - Suscripción {} - Factura {}", 
                            suscripcion.getId(), factura.getNumeroFactura());
                    renovadas++;
                } else {
                    // Renovación automática desactivada
                    log.info("Suscripción {} no tiene renovación automática activada", 
                            suscripcion.getId());
                    noRenovadas++;
                }
            } catch (Exception e) {
                log.error("Error al renovar suscripción {}: {}", suscripcion.getId(), e.getMessage());
                noRenovadas++;
            }
        }
        
        log.info("=== Renovación automática completada: {} renovadas, {} no renovadas ===", 
                renovadas, noRenovadas);
    }

    /**
     * Procesa suscripciones morosas diariamente a las 01:00
     * Marca como MOROSA las suscripciones con facturas vencidas
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void procesarSuscripcionesMorosas() {
        log.info("=== Iniciando proceso de gestión de morosos ===");
        
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimiteMorosa = hoy.minusDays(DIAS_GRACIA);
        
        // Buscar facturas vencidas más allá del período de gracia
        List<Factura> facturasVencidas = facturaRepository.findVencidas(fechaLimiteMorosa);
        
        int marcadasMorosas = 0;
        
        for (Factura factura : facturasVencidas) {
            Suscripcion suscripcion = factura.getSuscripcion();
            
            if (suscripcion.getEstado() == EstadoSuscripcion.ACTIVA) {
                suscripcion.setEstado(EstadoSuscripcion.MOROSA);
                suscripcionRepository.save(suscripcion);
                log.info("Suscripción {} marcada como MOROSA por factura {} vencida", 
                        suscripcion.getId(), factura.getNumeroFactura());
                marcadasMorosas++;
            }
        }
        
        log.info("=== Gestión de morosos completada: {} suscripciones marcadas como morosas ===", 
                marcadasMorosas);
    }

    /**
     * Procesa suspensiones diariamente a las 02:00
     * Suspende suscripciones morosas por más de 30 días
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void procesarSuspensiones() {
        log.info("=== Iniciando proceso de suspensiones ===");
        
        LocalDate fechaLimiteSuspension = LocalDate.now().minusDays(DIAS_PARA_SUSPENDER);
        
        List<Suscripcion> morosas = suscripcionRepository.findByEstado(EstadoSuscripcion.MOROSA);
        int suspendidas = 0;
        
        for (Suscripcion suscripcion : morosas) {
            // Verificar si tiene facturas pendientes por más de DIAS_PARA_SUSPENDER
            List<Factura> facturasPendientes = facturaRepository
                    .findBySuscripcionIdOrderByFechaEmisionDesc(suscripcion.getId())
                    .stream()
                    .filter(f -> f.getEstado() == EstadoFactura.PENDIENTE || 
                                f.getEstado() == EstadoFactura.VENCIDA)
                    .filter(f -> f.getFechaVencimiento().isBefore(fechaLimiteSuspension))
                    .toList();
            
            if (!facturasPendientes.isEmpty()) {
                suscripcion.setEstado(EstadoSuscripcion.SUSPENDIDA);
                suscripcionRepository.save(suscripcion);
                log.info("Suscripción {} SUSPENDIDA por impago prolongado", suscripcion.getId());
                suspendidas++;
            }
        }
        
        log.info("=== Proceso de suspensiones completado: {} suscripciones suspendidas ===", 
                suspendidas);
    }

    /**
     * Procesa expiraciones diariamente a las 03:00
     * Expira suscripciones suspendidas por más de 60 días
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void procesarExpiraciones() {
        log.info("=== Iniciando proceso de expiraciones ===");
        
        LocalDate fechaLimiteExpiracion = LocalDate.now().minusDays(DIAS_PARA_EXPIRAR);
        
        List<Suscripcion> suspendidas = suscripcionRepository.findByEstado(EstadoSuscripcion.SUSPENDIDA);
        int expiradas = 0;
        
        for (Suscripcion suscripcion : suspendidas) {
            // Verificar el tiempo que lleva suspendida (usando fechas de facturas)
            List<Factura> facturasPendientes = facturaRepository
                    .findBySuscripcionIdOrderByFechaEmisionDesc(suscripcion.getId())
                    .stream()
                    .filter(f -> f.getEstado() == EstadoFactura.PENDIENTE || 
                                f.getEstado() == EstadoFactura.VENCIDA)
                    .filter(f -> f.getFechaVencimiento().isBefore(fechaLimiteExpiracion))
                    .toList();
            
            if (!facturasPendientes.isEmpty()) {
                suscripcion.setEstado(EstadoSuscripcion.EXPIRADA);
                suscripcion.setFechaCancelacion(LocalDateTime.now());
                suscripcion.setMotivoCancelacion("Expirada automáticamente por impago prolongado");
                suscripcionRepository.save(suscripcion);
                log.info("Suscripción {} EXPIRADA por impago prolongado", suscripcion.getId());
                expiradas++;
            }
        }
        
        log.info("=== Proceso de expiraciones completado: {} suscripciones expiradas ===", 
                expiradas);
    }

    /**
     * Cancela una suscripción manualmente
     */
    public Suscripcion cancelarSuscripcion(Long suscripcionId, String motivo) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada: " + suscripcionId));
        
        suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
        suscripcion.setFechaCancelacion(LocalDateTime.now());
        suscripcion.setMotivoCancelacion(motivo != null ? motivo : "Cancelación solicitada por el usuario");
        suscripcion.setRenovacionAutomatica(false);
        
        log.info("Suscripción {} cancelada manualmente. Motivo: {}", suscripcionId, motivo);
        return suscripcionRepository.save(suscripcion);
    }

    /**
     * Reactiva una suscripción cancelada o suspendida
     */
    public Suscripcion reactivarSuscripcion(Long suscripcionId) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada: " + suscripcionId));
        
        if (suscripcion.getEstado() == EstadoSuscripcion.EXPIRADA) {
            throw new RuntimeException("No se puede reactivar una suscripción expirada. Debe crear una nueva.");
        }
        
        // Verificar que no tenga facturas pendientes
        if (tieneFacturasPendientes(suscripcion)) {
            throw new RuntimeException("Debe pagar las facturas pendientes antes de reactivar");
        }
        
        suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
        suscripcion.setFechaCancelacion(null);
        suscripcion.setMotivoCancelacion(null);
        suscripcion.setRenovacionAutomatica(true);
        
        // Si la fecha de próximo cobro ya pasó, actualizarla
        if (suscripcion.getFechaProximoCobro().isBefore(LocalDate.now())) {
            suscripcion.setFechaProximoCobro(LocalDate.now().plusDays(30));
        }
        
        log.info("Suscripción {} reactivada", suscripcionId);
        return suscripcionRepository.save(suscripcion);
    }

    /**
     * Activa o desactiva la renovación automática
     */
    public Suscripcion toggleRenovacionAutomatica(Long suscripcionId, boolean renovacionAutomatica) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada: " + suscripcionId));
        
        suscripcion.setRenovacionAutomatica(renovacionAutomatica);
        log.info("Suscripción {} - Renovación automática: {}", suscripcionId, renovacionAutomatica);
        
        return suscripcionRepository.save(suscripcion);
    }

    /**
     * Verifica si una suscripción tiene facturas pendientes
     */
    public boolean tieneFacturasPendientes(Suscripcion suscripcion) {
        List<Factura> pendientes = facturaRepository
                .findBySuscripcionId(suscripcion.getId())
                .stream()
                .filter(f -> f.getEstado() == EstadoFactura.PENDIENTE || 
                            f.getEstado() == EstadoFactura.VENCIDA)
                .toList();
        return !pendientes.isEmpty();
    }

    /**
     * Obtiene estadísticas del ciclo de vida
     */
    public EstadisticasCicloVida obtenerEstadisticas() {
        long activas = suscripcionRepository.countByEstado(EstadoSuscripcion.ACTIVA);
        long morosas = suscripcionRepository.countByEstado(EstadoSuscripcion.MOROSA);
        long suspendidas = suscripcionRepository.countByEstado(EstadoSuscripcion.SUSPENDIDA);
        long canceladas = suscripcionRepository.countByEstado(EstadoSuscripcion.CANCELADA);
        long expiradas = suscripcionRepository.countByEstado(EstadoSuscripcion.EXPIRADA);
        long facturasPendientes = facturaRepository.countByEstado(EstadoFactura.PENDIENTE);
        
        return new EstadisticasCicloVida(activas, morosas, suspendidas, canceladas, expiradas, facturasPendientes);
    }

    /**
     * Ejecuta todos los procesos de ciclo de vida manualmente (para testing)
     */
    public void ejecutarCicloVidaCompleto() {
        procesarRenovacionesAutomaticas();
        procesarSuscripcionesMorosas();
        procesarSuspensiones();
        procesarExpiraciones();
    }

    // ===== MÉTODOS PARA EJECUCIÓN MANUAL VIA API =====

    /**
     * Ejecuta renovaciones manualmente y retorna el número de renovaciones
     */
    public int ejecutarRenovacionesManual() {
        LocalDate hoy = LocalDate.now();
        List<Suscripcion> suscripcionesParaRenovar = suscripcionRepository
                .findSuscripcionesParaRenovar(hoy);
        
        int renovadas = 0;
        for (Suscripcion suscripcion : suscripcionesParaRenovar) {
            try {
                if (suscripcion.getRenovacionAutomatica() && !tieneFacturasPendientes(suscripcion)) {
                    suscripcionService.generarFacturaMensual(suscripcion);
                    renovadas++;
                }
            } catch (Exception e) {
                log.error("Error al renovar suscripción {}: {}", suscripcion.getId(), e.getMessage());
            }
        }
        return renovadas;
    }

    /**
     * Procesa morosos manualmente y retorna el número de procesados
     */
    public int ejecutarProcesoMorososManual() {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimiteMorosa = hoy.minusDays(DIAS_GRACIA);
        
        List<Factura> facturasVencidas = facturaRepository.findVencidas(fechaLimiteMorosa);
        int procesados = 0;
        
        for (Factura factura : facturasVencidas) {
            Suscripcion suscripcion = factura.getSuscripcion();
            if (suscripcion != null && suscripcion.getEstado() == EstadoSuscripcion.ACTIVA) {
                suscripcion.setEstado(EstadoSuscripcion.MOROSA);
                suscripcionRepository.save(suscripcion);
                procesados++;
            }
        }
        return procesados;
    }

    /**
     * Procesa suspensiones manualmente y retorna el número de procesados
     */
    public int ejecutarProcesoSuspensionesManual() {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimiteSuspension = hoy.minusDays(DIAS_PARA_SUSPENDER);
        
        List<Factura> facturasVencidas = facturaRepository.findVencidas(fechaLimiteSuspension);
        int procesados = 0;
        
        for (Factura factura : facturasVencidas) {
            Suscripcion suscripcion = factura.getSuscripcion();
            if (suscripcion != null && suscripcion.getEstado() == EstadoSuscripcion.MOROSA) {
                suscripcion.setEstado(EstadoSuscripcion.SUSPENDIDA);
                suscripcionRepository.save(suscripcion);
                procesados++;
            }
        }
        return procesados;
    }

    /**
     * Procesa expiraciones manualmente y retorna el número de procesados
     */
    public int ejecutarProcesoExpiracionesManual() {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimiteExpiracion = hoy.minusDays(DIAS_PARA_EXPIRAR);
        
        List<Factura> facturasVencidas = facturaRepository.findVencidas(fechaLimiteExpiracion);
        int procesados = 0;
        
        for (Factura factura : facturasVencidas) {
            Suscripcion suscripcion = factura.getSuscripcion();
            if (suscripcion != null && suscripcion.getEstado() == EstadoSuscripcion.SUSPENDIDA) {
                suscripcion.setEstado(EstadoSuscripcion.EXPIRADA);
                suscripcion.setRenovacionAutomatica(false);
                suscripcionRepository.save(suscripcion);
                procesados++;
            }
        }
        return procesados;
    }

    /**
     * Record con estadísticas del ciclo de vida
     */
    public record EstadisticasCicloVida(
            long activas,
            long morosas,
            long suspendidas,
            long canceladas,
            long expiradas,
            long facturasPendientes
    ) {}
}
