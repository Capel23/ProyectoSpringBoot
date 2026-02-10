package com.example.ProyectoSpringBoot.repository;

import com.example.ProyectoSpringBoot.entity.Factura;
import com.example.ProyectoSpringBoot.enums.EstadoFactura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Repositorio de Factura
@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    // Por número de factura
    Optional<Factura> findByNumeroFactura(String numeroFactura);

    // Por estado
    List<Factura> findByEstado(EstadoFactura estado);

    // Por suscripción
    List<Factura> findBySuscripcionId(Long suscripcionId);
    List<Factura> findBySuscripcionIdOrderByFechaEmisionDesc(Long suscripcionId);

    // Por usuario (a través de suscripción)
    @Query("SELECT f FROM Factura f WHERE f.suscripcion.usuario.id = :usuarioId ORDER BY f.fechaEmision DESC")
    List<Factura> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    // Con paginación
    @Query("SELECT f FROM Factura f WHERE f.suscripcion.usuario.id = :usuarioId")
    Page<Factura> findByUsuarioIdPaginado(@Param("usuarioId") Long usuarioId, Pageable pageable);

    // Por fechas
    List<Factura> findByFechaEmision(LocalDate fecha);
    List<Factura> findByFechaEmisionBetween(LocalDate inicio, LocalDate fin);

    // Por monto
    List<Factura> findByTotalBetween(BigDecimal min, BigDecimal max);
    List<Factura> findByTotalGreaterThan(BigDecimal monto);

    // Vencidas
    @Query("SELECT f FROM Factura f WHERE f.estado = 'PENDIENTE' AND f.fechaVencimiento < :fecha")
    List<Factura> findVencidas(@Param("fecha") LocalDate fecha);

    // Prorrateos
    List<Factura> findByEsProrrateoTrue();

    // Conteo y sumas
    long countByEstado(EstadoFactura estado);

    @Query("SELECT COALESCE(SUM(f.total), 0) FROM Factura f WHERE f.estado = 'PENDIENTE'")
    BigDecimal sumTotalPendientes();

    // ===== FILTROS AVANZADOS PARA PARTE 2 =====
    
    // Por rango de fechas con estado
    @Query("SELECT f FROM Factura f WHERE f.fechaEmision BETWEEN :inicio AND :fin " +
           "AND (:estado IS NULL OR f.estado = :estado) ORDER BY f.fechaEmision DESC")
    List<Factura> findByFechaEmisionBetweenAndEstado(
            @Param("inicio") LocalDate inicio, 
            @Param("fin") LocalDate fin,
            @Param("estado") EstadoFactura estado);

    // Por rango de monto con estado
    @Query("SELECT f FROM Factura f WHERE f.total BETWEEN :minimo AND :maximo " +
           "AND (:estado IS NULL OR f.estado = :estado) ORDER BY f.total DESC")
    List<Factura> findByTotalBetweenAndEstado(
            @Param("minimo") BigDecimal minimo, 
            @Param("maximo") BigDecimal maximo,
            @Param("estado") EstadoFactura estado);

    // Búsqueda combinada con paginación
    @Query("SELECT f FROM Factura f WHERE " +
           "(:fechaInicio IS NULL OR f.fechaEmision >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR f.fechaEmision <= :fechaFin) AND " +
           "(:montoMinimo IS NULL OR f.total >= :montoMinimo) AND " +
           "(:montoMaximo IS NULL OR f.total <= :montoMaximo) AND " +
           "(:estado IS NULL OR f.estado = :estado) AND " +
           "(:usuarioId IS NULL OR f.suscripcion.usuario.id = :usuarioId)")
    Page<Factura> buscarConFiltros(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("montoMinimo") BigDecimal montoMinimo,
            @Param("montoMaximo") BigDecimal montoMaximo,
            @Param("estado") EstadoFactura estado,
            @Param("usuarioId") Long usuarioId,
            Pageable pageable);

    // Total facturado por periodo
    @Query("SELECT COALESCE(SUM(f.total), 0) FROM Factura f WHERE " +
           "f.estado = 'PAGADA' AND f.fechaEmision BETWEEN :inicio AND :fin")
    BigDecimal sumTotalFacturadoPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    // Total de impuestos recaudados
    @Query("SELECT COALESCE(SUM(f.montoImpuestos), 0) FROM Factura f WHERE " +
           "f.estado = 'PAGADA' AND f.fechaEmision BETWEEN :inicio AND :fin")
    BigDecimal sumImpuestosPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    // Facturas por país del usuario
    @Query("SELECT f FROM Factura f WHERE f.suscripcion.usuario.perfil.pais = :pais ORDER BY f.fechaEmision DESC")
    List<Factura> findByPaisUsuario(@Param("pais") String pais);

    // Resumen por estado
    @Query("SELECT f.estado, COUNT(f), COALESCE(SUM(f.total), 0) FROM Factura f GROUP BY f.estado")
    List<Object[]> getResumenPorEstado();
}
