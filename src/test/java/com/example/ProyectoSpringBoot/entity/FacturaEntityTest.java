package com.example.ProyectoSpringBoot.entity;

import com.example.ProyectoSpringBoot.enums.EstadoFactura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la entidad Factura
 * Valida la lógica de facturas: pendientes, pagadas, vencidas, prorrateadas
 */
class FacturaEntityTest {

    private Factura factura;

    @BeforeEach
    void setUp() {
        factura = Factura.builder()
                .numeroFactura("FAC-001")
                .fechaEmision(LocalDate.now())
                .fechaVencimiento(LocalDate.now().plusDays(15))
                .subtotal(new BigDecimal("29.99"))
                .porcentajeImpuestos(new BigDecimal("21.00"))
                .montoImpuestos(new BigDecimal("6.30"))
                .total(new BigDecimal("36.29"))
                .estado(EstadoFactura.PENDIENTE)
                .concepto("Suscripción mensual - Plan Premium")
                .esProrrateo(false)
                .build();
    }

    @Nested
    @DisplayName("Tests de Estado de Factura")
    class EstadoFacturaTests {

        @Test
        @DisplayName("estaPendiente() debe devolver true si estado es PENDIENTE")
        void testEstaPendiente_True() {
            factura.setEstado(EstadoFactura.PENDIENTE);
            assertTrue(factura.estaPendiente());
        }

        @Test
        @DisplayName("estaPendiente() debe devolver false si estado es PAGADA")
        void testEstaPendiente_False() {
            factura.setEstado(EstadoFactura.PAGADA);
            assertFalse(factura.estaPendiente());
        }

        @Test
        @DisplayName("estaVencida() debe devolver true si está pendiente y fecha pasó")
        void testEstaVencida_True() {
            factura.setEstado(EstadoFactura.PENDIENTE);
            factura.setFechaVencimiento(LocalDate.now().minusDays(1));
            assertTrue(factura.estaVencida());
        }

        @Test
        @DisplayName("estaVencida() debe devolver false si aún no venció")
        void testEstaVencida_False() {
            factura.setEstado(EstadoFactura.PENDIENTE);
            factura.setFechaVencimiento(LocalDate.now().plusDays(10));
            assertFalse(factura.estaVencida());
        }

        @Test
        @DisplayName("estaVencida() debe devolver false si ya está pagada")
        void testEstaVencida_FalseSiPagada() {
            factura.setEstado(EstadoFactura.PAGADA);
            factura.setFechaVencimiento(LocalDate.now().minusDays(1));
            assertFalse(factura.estaVencida());
        }
    }

    @Nested
    @DisplayName("Tests de Cálculos de Factura")
    class CalculosFacturaTests {

        @Test
        @DisplayName("Total debe ser mayor que subtotal (incluye impuestos)")
        void testTotalMayorQueSubtotal() {
            assertTrue(factura.getTotal().compareTo(factura.getSubtotal()) > 0);
        }

        @Test
        @DisplayName("IVA debe ser 21%")
        void testPorcentajeIVA() {
            assertEquals(new BigDecimal("21.00"), factura.getPorcentajeImpuestos());
        }

        @Test
        @DisplayName("Total = Subtotal + Impuestos")
        void testCalculoTotal() {
            BigDecimal totalCalculado = factura.getSubtotal().add(factura.getMontoImpuestos());
            assertEquals(totalCalculado, factura.getTotal());
        }
    }

    @Nested
    @DisplayName("Tests de Factura de Prorrateo")
    class FacturaProrrateoTests {

        @Test
        @DisplayName("Factura normal no debe ser prorrateo")
        void testFacturaNormal_NoProrrateo() {
            assertFalse(factura.getEsProrrateo());
        }

        @Test
        @DisplayName("Debe poder marcar factura como prorrateo")
        void testFacturaProrrateo() {
            Factura facturaProrrateo = Factura.builder()
                    .numeroFactura("PRO-001")
                    .fechaEmision(LocalDate.now())
                    .fechaVencimiento(LocalDate.now().plusDays(7))
                    .subtotal(new BigDecimal("10.00"))
                    .porcentajeImpuestos(new BigDecimal("21.00"))
                    .montoImpuestos(new BigDecimal("2.10"))
                    .total(new BigDecimal("12.10"))
                    .estado(EstadoFactura.PENDIENTE)
                    .concepto("Prorrateo cambio de plan: Basic → Premium")
                    .esProrrateo(true)
                    .build();

            assertTrue(facturaProrrateo.getEsProrrateo());
            assertTrue(facturaProrrateo.getConcepto().contains("Prorrateo"));
        }
    }

    @Nested
    @DisplayName("Tests de Estados de Factura Enum")
    class EstadosEnumTests {

        @Test
        @DisplayName("Debe tener todos los estados requeridos")
        void testEstadosFactura() {
            // Los estados definidos en el requisito
            assertNotNull(EstadoFactura.PENDIENTE);
            assertNotNull(EstadoFactura.PAGADA);
            assertNotNull(EstadoFactura.VENCIDA);
            assertNotNull(EstadoFactura.CANCELADA);
        }
    }
}
