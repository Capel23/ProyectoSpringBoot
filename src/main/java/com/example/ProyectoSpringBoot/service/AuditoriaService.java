package com.example.ProyectoSpringBoot.service;

import com.example.ProyectoSpringBoot.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para consultar el historial de auditoría mediante Hibernate Envers.
 * Proporciona acceso al Panel de Auditoría para administradores.
 * 
 * Cumple criterio Parte 2: "Panel de Auditoría (Admin): Una vista especial 
 * para ver el historial de cambios en las suscripciones o facturas usando Envers."
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuditoriaService {

    @PersistenceContext
    private EntityManager entityManager;

    // Mapa de entidades auditables
    private static final Map<String, Class<?>> ENTIDADES_AUDITABLES = Map.of(
            "Usuario", Usuario.class,
            "Suscripcion", Suscripcion.class,
            "Factura", Factura.class,
            "Plan", Plan.class,
            "Perfil", Perfil.class
    );

    /**
     * Obtiene el historial completo de una entidad específica
     */
    @SuppressWarnings("unchecked")
    public List<RegistroAuditoria> obtenerHistorialEntidad(String tipoEntidad, Long entityId) {
        Class<?> entityClass = ENTIDADES_AUDITABLES.get(tipoEntidad);
        if (entityClass == null) {
            throw new IllegalArgumentException("Tipo de entidad no auditable: " + tipoEntidad);
        }

        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<Number> revisions = reader.getRevisions(entityClass, entityId);

        List<RegistroAuditoria> historial = new ArrayList<>();
        for (Number revisionNumber : revisions) {
            Object entity = reader.find(entityClass, entityId, revisionNumber);
            Date revisionDate = reader.getRevisionDate(revisionNumber);
            RevisionType revisionType = (RevisionType) reader.createQuery()
                    .forRevisionsOfEntity(entityClass, false, true)
                    .add(AuditEntity.id().eq(entityId))
                    .add(AuditEntity.revisionNumber().eq(revisionNumber))
                    .getSingleResult();

            historial.add(new RegistroAuditoria(
                    revisionNumber.longValue(),
                    toLocalDateTime(revisionDate),
                    tipoEntidad,
                    entityId,
                    mapRevisionType(revisionType),
                    extraerDetallesEntidad(entity)
            ));
        }

        return historial;
    }

    /**
     * Obtiene todos los cambios recientes en el sistema (últimas N revisiones)
     */
    @SuppressWarnings("unchecked")
    public List<RegistroAuditoria> obtenerCambiosRecientes(int limite) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<RegistroAuditoria> todosLosCambios = new ArrayList<>();

        for (Map.Entry<String, Class<?>> entry : ENTIDADES_AUDITABLES.entrySet()) {
            String tipoEntidad = entry.getKey();
            Class<?> entityClass = entry.getValue();

            try {
                AuditQuery query = reader.createQuery()
                        .forRevisionsOfEntity(entityClass, false, true)
                        .addOrder(AuditEntity.revisionNumber().desc())
                        .setMaxResults(limite);

                List<Object[]> results = query.getResultList();
                
                for (Object[] result : results) {
                    Object entity = result[0];
                    Object revisionEntity = result[1];
                    RevisionType revisionType = (RevisionType) result[2];

                    // Extraer información de revisión
                    Long revisionNumber = extractRevisionNumber(revisionEntity);
                    Date revisionDate = extractRevisionDate(revisionEntity);
                    Long entityId = extractEntityId(entity);

                    todosLosCambios.add(new RegistroAuditoria(
                            revisionNumber,
                            toLocalDateTime(revisionDate),
                            tipoEntidad,
                            entityId,
                            mapRevisionType(revisionType),
                            extraerDetallesEntidad(entity)
                    ));
                }
            } catch (Exception e) {
                log.warn("Error consultando auditoría para {}: {}", tipoEntidad, e.getMessage());
            }
        }

        // Ordenar por fecha descendente y limitar
        return todosLosCambios.stream()
                .sorted(Comparator.comparing(RegistroAuditoria::fechaCambio).reversed())
                .limit(limite)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el historial de cambios de suscripciones
     */
    @SuppressWarnings("unchecked")
    public List<RegistroAuditoria> obtenerHistorialSuscripciones(int limite) {
        return obtenerHistorialPorTipo("Suscripcion", Suscripcion.class, limite);
    }

    /**
     * Obtiene el historial de cambios de facturas
     */
    @SuppressWarnings("unchecked")
    public List<RegistroAuditoria> obtenerHistorialFacturas(int limite) {
        return obtenerHistorialPorTipo("Factura", Factura.class, limite);
    }

    /**
     * Obtiene el historial de cambios de usuarios
     */
    @SuppressWarnings("unchecked")
    public List<RegistroAuditoria> obtenerHistorialUsuarios(int limite) {
        return obtenerHistorialPorTipo("Usuario", Usuario.class, limite);
    }

    /**
     * Compara dos revisiones de una entidad
     */
    public ComparacionRevisiones compararRevisiones(String tipoEntidad, Long entityId, 
                                                      Long revisionAnterior, Long revisionActual) {
        Class<?> entityClass = ENTIDADES_AUDITABLES.get(tipoEntidad);
        if (entityClass == null) {
            throw new IllegalArgumentException("Tipo de entidad no auditable: " + tipoEntidad);
        }

        AuditReader reader = AuditReaderFactory.get(entityManager);
        
        Object entityAnterior = reader.find(entityClass, entityId, revisionAnterior);
        Object entityActual = reader.find(entityClass, entityId, revisionActual);

        Map<String, String> valoresAnteriores = extraerDetallesEntidad(entityAnterior);
        Map<String, String> valoresActuales = extraerDetallesEntidad(entityActual);

        List<CambioCampo> cambios = new ArrayList<>();
        Set<String> todosCampos = new HashSet<>();
        todosCampos.addAll(valoresAnteriores.keySet());
        todosCampos.addAll(valoresActuales.keySet());

        for (String campo : todosCampos) {
            String valorAnterior = valoresAnteriores.get(campo);
            String valorActual = valoresActuales.get(campo);
            
            if (!Objects.equals(valorAnterior, valorActual)) {
                cambios.add(new CambioCampo(campo, valorAnterior, valorActual));
            }
        }

        return new ComparacionRevisiones(
                tipoEntidad,
                entityId,
                revisionAnterior,
                revisionActual,
                cambios
        );
    }

    /**
     * Obtiene estadísticas de auditoría
     */
    public EstadisticasAuditoria obtenerEstadisticas() {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        
        Map<String, Long> conteosPorEntidad = new HashMap<>();
        Map<String, Long> conteosPorOperacion = new HashMap<>();
        conteosPorOperacion.put("CREACION", 0L);
        conteosPorOperacion.put("MODIFICACION", 0L);
        conteosPorOperacion.put("ELIMINACION", 0L);

        long totalRevisiones = 0;

        for (Map.Entry<String, Class<?>> entry : ENTIDADES_AUDITABLES.entrySet()) {
            String tipoEntidad = entry.getKey();
            Class<?> entityClass = entry.getValue();

            try {
                @SuppressWarnings("unchecked")
                List<Object[]> results = reader.createQuery()
                        .forRevisionsOfEntity(entityClass, false, true)
                        .getResultList();

                long count = results.size();
                conteosPorEntidad.put(tipoEntidad, count);
                totalRevisiones += count;

                for (Object[] result : results) {
                    RevisionType revType = (RevisionType) result[2];
                    String operacion = mapRevisionType(revType);
                    conteosPorOperacion.merge(operacion, 1L, Long::sum);
                }
            } catch (Exception e) {
                log.warn("Error obteniendo estadísticas para {}: {}", tipoEntidad, e.getMessage());
                conteosPorEntidad.put(tipoEntidad, 0L);
            }
        }

        return new EstadisticasAuditoria(
                totalRevisiones,
                conteosPorEntidad,
                conteosPorOperacion
        );
    }

    // ===== MÉTODOS AUXILIARES =====

    private List<RegistroAuditoria> obtenerHistorialPorTipo(String tipoEntidad, Class<?> entityClass, int limite) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<RegistroAuditoria> historial = new ArrayList<>();

        try {
            @SuppressWarnings("unchecked")
            List<Object[]> results = reader.createQuery()
                    .forRevisionsOfEntity(entityClass, false, true)
                    .addOrder(AuditEntity.revisionNumber().desc())
                    .setMaxResults(limite)
                    .getResultList();

            for (Object[] result : results) {
                Object entity = result[0];
                Object revisionEntity = result[1];
                RevisionType revisionType = (RevisionType) result[2];

                Long revisionNumber = extractRevisionNumber(revisionEntity);
                Date revisionDate = extractRevisionDate(revisionEntity);
                Long entityId = extractEntityId(entity);

                historial.add(new RegistroAuditoria(
                        revisionNumber,
                        toLocalDateTime(revisionDate),
                        tipoEntidad,
                        entityId,
                        mapRevisionType(revisionType),
                        extraerDetallesEntidad(entity)
                ));
            }
        } catch (Exception e) {
            log.error("Error obteniendo historial de {}: {}", tipoEntidad, e.getMessage());
        }

        return historial;
    }

    private String mapRevisionType(RevisionType revisionType) {
        return switch (revisionType) {
            case ADD -> "CREACION";
            case MOD -> "MODIFICACION";
            case DEL -> "ELIMINACION";
        };
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private Long extractRevisionNumber(Object revisionEntity) {
        try {
            var method = revisionEntity.getClass().getMethod("getId");
            return ((Number) method.invoke(revisionEntity)).longValue();
        } catch (Exception e) {
            return null;
        }
    }

    private Date extractRevisionDate(Object revisionEntity) {
        try {
            var method = revisionEntity.getClass().getMethod("getRevisionDate");
            return (Date) method.invoke(revisionEntity);
        } catch (Exception e) {
            try {
                var timestampMethod = revisionEntity.getClass().getMethod("getTimestamp");
                Long timestamp = (Long) timestampMethod.invoke(revisionEntity);
                return new Date(timestamp);
            } catch (Exception ex) {
                return new Date();
            }
        }
    }

    private Long extractEntityId(Object entity) {
        try {
            var method = entity.getClass().getMethod("getId");
            return ((Number) method.invoke(entity)).longValue();
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, String> extraerDetallesEntidad(Object entity) {
        Map<String, String> detalles = new LinkedHashMap<>();
        
        if (entity == null) {
            return detalles;
        }

        try {
            if (entity instanceof Usuario u) {
                detalles.put("id", String.valueOf(u.getId()));
                detalles.put("email", u.getEmail());
                detalles.put("activo", String.valueOf(u.getActivo()));
            } else if (entity instanceof Suscripcion s) {
                detalles.put("id", String.valueOf(s.getId()));
                detalles.put("estado", s.getEstado() != null ? s.getEstado().name() : null);
                detalles.put("fechaInicio", s.getFechaInicio() != null ? s.getFechaInicio().toString() : null);
                detalles.put("fechaFin", s.getFechaFin() != null ? s.getFechaFin().toString() : null);
                detalles.put("renovacionAutomatica", String.valueOf(s.getRenovacionAutomatica()));
                detalles.put("planId", s.getPlan() != null ? String.valueOf(s.getPlan().getId()) : null);
                detalles.put("usuarioId", s.getUsuario() != null ? String.valueOf(s.getUsuario().getId()) : null);
            } else if (entity instanceof Factura f) {
                detalles.put("id", String.valueOf(f.getId()));
                detalles.put("numeroFactura", f.getNumeroFactura());
                detalles.put("estado", f.getEstado() != null ? f.getEstado().name() : null);
                detalles.put("subtotal", f.getSubtotal() != null ? f.getSubtotal().toString() : null);
                detalles.put("montoImpuestos", f.getMontoImpuestos() != null ? f.getMontoImpuestos().toString() : null);
                detalles.put("total", f.getTotal() != null ? f.getTotal().toString() : null);
                detalles.put("fechaEmision", f.getFechaEmision() != null ? f.getFechaEmision().toString() : null);
            } else if (entity instanceof Plan p) {
                detalles.put("id", String.valueOf(p.getId()));
                detalles.put("nombre", p.getNombre());
                detalles.put("tipoPlan", p.getTipoPlan() != null ? p.getTipoPlan().name() : null);
                detalles.put("precioMensual", p.getPrecioMensual() != null ? p.getPrecioMensual().toString() : null);
                detalles.put("activo", String.valueOf(p.getActivo()));
            } else if (entity instanceof Perfil p) {
                detalles.put("id", String.valueOf(p.getId()));
                detalles.put("nombre", p.getNombre());
                detalles.put("apellidos", p.getApellidos());
                detalles.put("pais", p.getPais());
            }
        } catch (Exception e) {
            log.warn("Error extrayendo detalles de entidad: {}", e.getMessage());
        }

        return detalles;
    }

    // ===== RECORDS PARA RESPUESTAS =====

    public record RegistroAuditoria(
            Long numeroRevision,
            LocalDateTime fechaCambio,
            String tipoEntidad,
            Long entityId,
            String tipoOperacion,
            Map<String, String> detalles
    ) {}

    public record ComparacionRevisiones(
            String tipoEntidad,
            Long entityId,
            Long revisionAnterior,
            Long revisionActual,
            List<CambioCampo> cambios
    ) {}

    public record CambioCampo(
            String campo,
            String valorAnterior,
            String valorActual
    ) {}

    public record EstadisticasAuditoria(
            long totalRevisiones,
            Map<String, Long> revisionesPorEntidad,
            Map<String, Long> revisionesPorOperacion
    ) {}
}
