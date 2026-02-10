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
import java.util.List;

/**
 * Servicio programado para la facturación automática
 * Se ejecuta cada día a las 00:00 para generar facturas de suscripciones activas
 * 
 * Cumple requisito: "El sistema debe generar automáticamente una factura cada 30 días"
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FacturacionScheduledService {

    private final SuscripcionService suscripcionService;
    private final SuscripcionRepository suscripcionRepository;
    private final FacturaRepository facturaRepository;

    /**
     * Tarea programada que se ejecuta todos los días a las 00:00
     * Busca suscripciones activas con fecha de próximo cobro <= hoy
     * y genera automáticamente sus facturas mensuales
     */
    @Scheduled(cron = "0 0 0 * * ?") // Todos los días a las 00:00
    @Transactional
    public void procesarFacturacionAutomatica() {
        log.info("=== Iniciando proceso de facturación automática ===");
        
        List<Suscripcion> suscripcionesParaFacturar = suscripcionService.obtenerSuscripcionesParaFacturar();
        
        log.info("Suscripciones pendientes de facturar: {}", suscripcionesParaFacturar.size());
        
        int facturasGeneradas = 0;
        int errores = 0;
        
        for (Suscripcion suscripcion : suscripcionesParaFacturar) {
            try {
                Factura factura = suscripcionService.generarFacturaMensual(suscripcion);
                facturasGeneradas++;
                log.info("Factura {} generada para usuario {} (suscripción {})", 
                        factura.getNumeroFactura(),
                        suscripcion.getUsuario().getEmail(),
                        suscripcion.getId());
            } catch (Exception e) {
                errores++;
                log.error("Error al generar factura para suscripción {}: {}", 
                        suscripcion.getId(), e.getMessage());
            }
        }
        
        log.info("=== Proceso de facturación completado: {} facturas generadas, {} errores ===", 
                facturasGeneradas, errores);
    }

    /**
     * Tarea programada que se ejecuta todos los días a las 01:00
     * Marca como morosas las suscripciones con facturas vencidas
     */
    @Scheduled(cron = "0 0 1 * * ?") // Todos los días a las 01:00
    @Transactional
    public void procesarSuscripcionesMorosas() {
        log.info("=== Verificando suscripciones morosas ===");
        
        List<Factura> facturasVencidas = facturaRepository.findVencidas(LocalDate.now());
        
        int suscripcionesMarcadas = 0;
        
        for (Factura factura : facturasVencidas) {
            Suscripcion suscripcion = factura.getSuscripcion();
            
            if (suscripcion.getEstado() == EstadoSuscripcion.ACTIVA) {
                suscripcion.setEstado(EstadoSuscripcion.MOROSA);
                suscripcionRepository.save(suscripcion);
                suscripcionesMarcadas++;
                
                log.warn("Suscripción {} marcada como MOROSA - Factura {} vencida", 
                        suscripcion.getId(), factura.getNumeroFactura());
            }
        }
        
        log.info("=== {} suscripciones marcadas como morosas ===", suscripcionesMarcadas);
    }

    /**
     * Método para ejecutar facturación manualmente (útil para testing)
     * @return Número de facturas generadas
     */
    @Transactional
    public int ejecutarFacturacionManual() {
        log.info("Ejecutando facturación manual...");
        
        List<Suscripcion> suscripcionesParaFacturar = suscripcionService.obtenerSuscripcionesParaFacturar();
        int facturasGeneradas = 0;
        
        for (Suscripcion suscripcion : suscripcionesParaFacturar) {
            try {
                suscripcionService.generarFacturaMensual(suscripcion);
                facturasGeneradas++;
            } catch (Exception e) {
                log.error("Error al generar factura: {}", e.getMessage());
            }
        }
        
        return facturasGeneradas;
    }
}
