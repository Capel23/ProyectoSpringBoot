package com.example.ProyectoSpringBoot.service;

import com.example.ProyectoSpringBoot.dto.SuscripcionDTO;
import com.example.ProyectoSpringBoot.entity.Factura;
import com.example.ProyectoSpringBoot.entity.Plan;
import com.example.ProyectoSpringBoot.entity.Suscripcion;
import com.example.ProyectoSpringBoot.entity.Usuario;
import com.example.ProyectoSpringBoot.enums.EstadoFactura;
import com.example.ProyectoSpringBoot.enums.EstadoSuscripcion;
import com.example.ProyectoSpringBoot.repository.FacturaRepository;
import com.example.ProyectoSpringBoot.repository.PlanRepository;
import com.example.ProyectoSpringBoot.repository.SuscripcionRepository;
import com.example.ProyectoSpringBoot.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para la lógica de negocio de Suscripción
 * Incluye: prorrateo al cambiar de plan, impuestos dinámicos por país
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlanRepository planRepository;
    private final FacturaRepository facturaRepository;
    private final ImpuestoService impuestoService;

    @Transactional(readOnly = true)
    public List<SuscripcionDTO> findAll() {
        return suscripcionRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<SuscripcionDTO> findById(Long id) {
        return suscripcionRepository.findById(id).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<SuscripcionDTO> findByUsuarioId(Long usuarioId) {
        return suscripcionRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SuscripcionDTO> findByEstado(EstadoSuscripcion estado) {
        return suscripcionRepository.findByEstado(estado).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<SuscripcionDTO> create(SuscripcionDTO dto) {
        Optional<Usuario> usuario = usuarioRepository.findById(dto.getUsuarioId());
        Optional<Plan> plan = planRepository.findById(dto.getPlanId());

        if (usuario.isEmpty() || plan.isEmpty()) {
            return Optional.empty();
        }

        // Calcular fecha próximo cobro (30 días después del inicio)
        LocalDate fechaProximoCobro = dto.getFechaProximoCobro() != null 
                ? dto.getFechaProximoCobro() 
                : dto.getFechaInicio().plusDays(30);

        Suscripcion suscripcion = Suscripcion.builder()
                .usuario(usuario.get())
                .plan(plan.get())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .fechaProximoCobro(fechaProximoCobro)
                .estado(dto.getEstado() != null ? dto.getEstado() : EstadoSuscripcion.ACTIVA)
                .renovacionAutomatica(dto.getRenovacionAutomatica() != null ? dto.getRenovacionAutomatica() : true)
                .precioActual(dto.getPrecioActual() != null ? dto.getPrecioActual() : plan.get().getPrecioMensual())
                .build();

        Suscripcion saved = suscripcionRepository.save(suscripcion);
        return Optional.of(toDTO(saved));
    }

    public Optional<SuscripcionDTO> update(Long id, SuscripcionDTO dto) {
        return suscripcionRepository.findById(id).map(existing -> {
            if (dto.getPlanId() != null) {
                planRepository.findById(dto.getPlanId()).ifPresent(existing::setPlan);
            }
            if (dto.getFechaInicio() != null) existing.setFechaInicio(dto.getFechaInicio());
            if (dto.getFechaFin() != null) existing.setFechaFin(dto.getFechaFin());
            if (dto.getEstado() != null) existing.setEstado(dto.getEstado());
            if (dto.getRenovacionAutomatica() != null) existing.setRenovacionAutomatica(dto.getRenovacionAutomatica());
            if (dto.getPrecioActual() != null) existing.setPrecioActual(dto.getPrecioActual());

            Suscripcion saved = suscripcionRepository.save(existing);
            return toDTO(saved);
        });
    }

    /**
     * Cambiar estado de suscripción (ACTIVA, CANCELADA, MOROSA)
     */
    public Optional<SuscripcionDTO> cambiarEstado(Long id, EstadoSuscripcion nuevoEstado) {
        return suscripcionRepository.findById(id).map(suscripcion -> {
            suscripcion.setEstado(nuevoEstado);
            Suscripcion saved = suscripcionRepository.save(suscripcion);
            return toDTO(saved);
        });
    }

    /**
     * Cambiar de plan con cálculo de prorrateo
     * Si el nuevo plan es más caro, genera una factura de prorrateo
     * @param suscripcionId ID de la suscripción
     * @param nuevoPlanId ID del nuevo plan
     * @return DTO de la suscripción actualizada
     */
    public Optional<SuscripcionDTO> cambiarPlan(Long suscripcionId, Long nuevoPlanId) {
        Optional<Suscripcion> suscripcionOpt = suscripcionRepository.findById(suscripcionId);
        Optional<Plan> nuevoPlanOpt = planRepository.findById(nuevoPlanId);

        if (suscripcionOpt.isEmpty() || nuevoPlanOpt.isEmpty()) {
            return Optional.empty();
        }

        Suscripcion suscripcion = suscripcionOpt.get();
        Plan planActual = suscripcion.getPlan();
        Plan nuevoPlan = nuevoPlanOpt.get();

        // Si el nuevo plan es más caro, calcular y cobrar prorrateo
        if (nuevoPlan.getPrecioMensual().compareTo(planActual.getPrecioMensual()) > 0) {
            BigDecimal prorrateo = calcularProrrateo(suscripcion, planActual, nuevoPlan);
            
            if (prorrateo.compareTo(BigDecimal.ZERO) > 0) {
                generarFacturaProrrateo(suscripcion, planActual, nuevoPlan, prorrateo);
            }
        }

        // Actualizar suscripción con el nuevo plan
        suscripcion.setPlan(nuevoPlan);
        suscripcion.setPrecioActual(nuevoPlan.getPrecioMensual());
        
        Suscripcion saved = suscripcionRepository.save(suscripcion);
        log.info("Suscripción {} cambió de plan {} a plan {}", suscripcionId, planActual.getNombre(), nuevoPlan.getNombre());
        
        return Optional.of(toDTO(saved));
    }

    /**
     * Calcula el prorrateo al cambiar de un plan barato a uno más caro
     * Fórmula: (PrecioNuevo - PrecioAnterior) * (DíasRestantes / 30)
     * @return Monto del prorrateo (subtotal sin impuestos)
     */
    public BigDecimal calcularProrrateo(Suscripcion suscripcion, Plan planAnterior, Plan planNuevo) {
        LocalDate hoy = LocalDate.now();
        LocalDate proximoCobro = suscripcion.getFechaProximoCobro();
        
        // Días restantes hasta el próximo cobro
        long diasRestantes = ChronoUnit.DAYS.between(hoy, proximoCobro);
        
        if (diasRestantes <= 0) {
            return BigDecimal.ZERO;
        }

        // Diferencia de precios
        BigDecimal diferenciaPrecio = planNuevo.getPrecioMensual().subtract(planAnterior.getPrecioMensual());
        
        // Prorrateo = diferencia * (días restantes / 30)
        BigDecimal prorrateo = diferenciaPrecio
                .multiply(BigDecimal.valueOf(diasRestantes))
                .divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
        
        log.info("Prorrateo calculado: {} (días restantes: {}, diferencia: {})", 
                prorrateo, diasRestantes, diferenciaPrecio);
        
        return prorrateo;
    }

    /**
     * Genera una factura de prorrateo por el cambio de plan
     * Usa impuestos dinámicos según el país del usuario
     */
    private Factura generarFacturaProrrateo(Suscripcion suscripcion, Plan planAnterior, Plan planNuevo, BigDecimal subtotal) {
        // Obtener país del usuario para calcular impuestos
        String paisUsuario = obtenerPaisUsuario(suscripcion);
        BigDecimal tasaImpuesto = impuestoService.obtenerTasaImpuesto(paisUsuario);
        BigDecimal impuestos = impuestoService.calcularImpuesto(subtotal, paisUsuario);
        BigDecimal total = subtotal.add(impuestos);
        
        Factura factura = Factura.builder()
                .numeroFactura("PRO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .suscripcion(suscripcion)
                .fechaEmision(LocalDate.now())
                .fechaVencimiento(LocalDate.now().plusDays(7))
                .subtotal(subtotal)
                .porcentajeImpuestos(tasaImpuesto)
                .montoImpuestos(impuestos)
                .total(total)
                .estado(EstadoFactura.PENDIENTE)
                .concepto("Prorrateo cambio de plan: " + planAnterior.getNombre() + " → " + planNuevo.getNombre())
                .esProrrateo(true)
                .notas("Ajuste por upgrade de plan. País: " + paisUsuario + ", Impuesto: " + tasaImpuesto + "%")
                .build();
        
        Factura saved = facturaRepository.save(factura);
        log.info("Factura de prorrateo generada: {} - Total: €{} (País: {}, Impuesto: {}%)", 
                saved.getNumeroFactura(), total, paisUsuario, tasaImpuesto);
        
        return saved;
    }

    /**
     * Genera factura mensual para una suscripción
     * Calcula impuestos según el país del usuario
     */
    public Factura generarFacturaMensual(Suscripcion suscripcion) {
        BigDecimal subtotal = suscripcion.getPrecioActual();
        
        // Obtener país del usuario para calcular impuestos dinámicos
        String paisUsuario = obtenerPaisUsuario(suscripcion);
        BigDecimal tasaImpuesto = impuestoService.obtenerTasaImpuesto(paisUsuario);
        BigDecimal impuestos = impuestoService.calcularImpuesto(subtotal, paisUsuario);
        BigDecimal total = subtotal.add(impuestos);
        
        Factura factura = Factura.builder()
                .numeroFactura("FAC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .suscripcion(suscripcion)
                .fechaEmision(LocalDate.now())
                .fechaVencimiento(LocalDate.now().plusDays(15))
                .subtotal(subtotal)
                .porcentajeImpuestos(tasaImpuesto)
                .montoImpuestos(impuestos)
                .total(total)
                .estado(EstadoFactura.PENDIENTE)
                .concepto("Suscripción mensual - Plan " + suscripcion.getPlan().getNombre())
                .esProrrateo(false)
                .notas("País: " + paisUsuario + ", Impuesto aplicado: " + tasaImpuesto + "%")
                .build();
        
        Factura saved = facturaRepository.save(factura);
        
        // Actualizar fecha de próximo cobro (+30 días)
        suscripcion.setFechaProximoCobro(suscripcion.getFechaProximoCobro().plusDays(30));
        suscripcionRepository.save(suscripcion);
        
        log.info("Factura mensual generada: {} para suscripción {} - Total: €{} (País: {}, Impuesto: {}%)", 
                saved.getNumeroFactura(), suscripcion.getId(), total, paisUsuario, tasaImpuesto);
        
        return saved;
    }

    /**
     * Obtiene el país del usuario de una suscripción
     */
    private String obtenerPaisUsuario(Suscripcion suscripcion) {
        if (suscripcion.getUsuario() != null && 
            suscripcion.getUsuario().getPerfil() != null &&
            suscripcion.getUsuario().getPerfil().getPais() != null) {
            return suscripcion.getUsuario().getPerfil().getPais();
        }
        return "ES"; // España por defecto
    }

    /**
     * Obtiene suscripciones que necesitan facturación (fecha próximo cobro <= hoy)
     */
    @Transactional(readOnly = true)
    public List<Suscripcion> obtenerSuscripcionesParaFacturar() {
        return suscripcionRepository.findSuscripcionesParaRenovar(LocalDate.now());
    }

    public boolean delete(Long id) {
        if (suscripcionRepository.existsById(id)) {
            suscripcionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ===== CONVERSIONES Entity <-> DTO =====

    public SuscripcionDTO toDTO(Suscripcion entity) {
        String usuarioNombre = null;
        
        // Obtener nombre del perfil del usuario
        if (entity.getUsuario() != null && entity.getUsuario().getPerfil() != null) {
            var perfil = entity.getUsuario().getPerfil();
            usuarioNombre = perfil.getNombre();
            if (perfil.getApellidos() != null) {
                usuarioNombre += " " + perfil.getApellidos();
            }
        }
        
        return SuscripcionDTO.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuario() != null ? entity.getUsuario().getId() : null)
                .usuarioNombre(usuarioNombre)
                .planId(entity.getPlan() != null ? entity.getPlan().getId() : null)
                .planNombre(entity.getPlan() != null ? entity.getPlan().getNombre() : null)
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .fechaProximoCobro(entity.getFechaProximoCobro())
                .estado(entity.getEstado())
                .renovacionAutomatica(entity.getRenovacionAutomatica())
                .precioActual(entity.getPrecioActual())
                .build();
    }
}
