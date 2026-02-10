package com.example.ProyectoSpringBoot.service;

import com.example.ProyectoSpringBoot.dto.SuscripcionDTO;
import com.example.ProyectoSpringBoot.entity.Plan;
import com.example.ProyectoSpringBoot.entity.Suscripcion;
import com.example.ProyectoSpringBoot.entity.Usuario;
import com.example.ProyectoSpringBoot.enums.EstadoSuscripcion;
import com.example.ProyectoSpringBoot.enums.TipoPlan;
import com.example.ProyectoSpringBoot.repository.FacturaRepository;
import com.example.ProyectoSpringBoot.repository.PlanRepository;
import com.example.ProyectoSpringBoot.repository.SuscripcionRepository;
import com.example.ProyectoSpringBoot.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para SuscripcionService
 * Cumple requisito: "Todo debe estar garantizado por pruebas unitarias"
 */
@ExtendWith(MockitoExtension.class)
class SuscripcionServiceTest {

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private ImpuestoService impuestoService;

    @InjectMocks
    private SuscripcionService suscripcionService;

    private Usuario usuario;
    private Plan planBasic;
    private Plan planPremium;
    private Plan planEnterprise;
    private Suscripcion suscripcion;

    @BeforeEach
    void setUp() {
        // Usuario de prueba
        usuario = Usuario.builder()
                .id(1L)
                .email("test@example.com")
                .password("password123")
                .activo(true)
                .build();

        // Planes de prueba
        planBasic = Plan.builder()
                .id(1L)
                .nombre("Plan Básico")
                .tipoPlan(TipoPlan.BASIC)
                .precioMensual(new BigDecimal("9.99"))
                .activo(true)
                .build();

        planPremium = Plan.builder()
                .id(2L)
                .nombre("Plan Premium")
                .tipoPlan(TipoPlan.PREMIUM)
                .precioMensual(new BigDecimal("29.99"))
                .activo(true)
                .build();

        planEnterprise = Plan.builder()
                .id(3L)
                .nombre("Plan Enterprise")
                .tipoPlan(TipoPlan.ENTERPRISE)
                .precioMensual(new BigDecimal("99.99"))
                .activo(true)
                .build();

        // Suscripción de prueba (con Plan Basic, 15 días para próximo cobro)
        suscripcion = Suscripcion.builder()
                .id(1L)
                .usuario(usuario)
                .plan(planBasic)
                .fechaInicio(LocalDate.now().minusDays(15))
                .fechaProximoCobro(LocalDate.now().plusDays(15))
                .estado(EstadoSuscripcion.ACTIVA)
                .renovacionAutomatica(true)
                .precioActual(planBasic.getPrecioMensual())
                .build();
    }

    @Nested
    @DisplayName("Tests de Prorrateo al Cambiar de Plan")
    class ProrrateoTests {

        @Test
        @DisplayName("Debe calcular prorrateo correctamente al cambiar de Basic a Premium")
        void testCalcularProrrateo_BasicAPremium() {
            // Given: 15 días restantes, diferencia de €20
            BigDecimal diferencia = planPremium.getPrecioMensual().subtract(planBasic.getPrecioMensual());
            // Prorrateo esperado: 20 * (15/30) = €10
            
            // When
            BigDecimal prorrateo = suscripcionService.calcularProrrateo(suscripcion, planBasic, planPremium);
            
            // Then
            assertNotNull(prorrateo);
            assertTrue(prorrateo.compareTo(BigDecimal.ZERO) > 0);
            assertEquals(new BigDecimal("10.00"), prorrateo);
        }

        @Test
        @DisplayName("Debe calcular prorrateo correctamente al cambiar de Basic a Enterprise")
        void testCalcularProrrateo_BasicAEnterprise() {
            // Given: 15 días restantes, diferencia de €90
            // Prorrateo esperado: 90 * (15/30) = €45
            
            // When
            BigDecimal prorrateo = suscripcionService.calcularProrrateo(suscripcion, planBasic, planEnterprise);
            
            // Then
            assertNotNull(prorrateo);
            assertEquals(new BigDecimal("45.00"), prorrateo);
        }

        @Test
        @DisplayName("Debe devolver prorrateo cero cuando no quedan días del periodo")
        void testCalcularProrrateo_SinDiasRestantes() {
            // Given: Próximo cobro es hoy o ya pasó
            suscripcion.setFechaProximoCobro(LocalDate.now());
            
            // When
            BigDecimal prorrateo = suscripcionService.calcularProrrateo(suscripcion, planBasic, planPremium);
            
            // Then
            assertEquals(BigDecimal.ZERO, prorrateo);
        }

        @Test
        @DisplayName("Debe generar factura de prorrateo al cambiar a plan más caro")
        void testCambiarPlan_DeberiaGenerarFacturaProrrateo() {
            // Given
            when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(suscripcion));
            when(planRepository.findById(2L)).thenReturn(Optional.of(planPremium));
            when(suscripcionRepository.save(any(Suscripcion.class))).thenReturn(suscripcion);
            when(facturaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            // Mock del servicio de impuestos (IVA 21% por defecto para España)
            when(impuestoService.obtenerTasaImpuesto(anyString())).thenReturn(new BigDecimal("21.00"));
            when(impuestoService.calcularImpuesto(any(BigDecimal.class), anyString()))
                    .thenAnswer(inv -> {
                        BigDecimal subtotal = inv.getArgument(0);
                        return subtotal.multiply(new BigDecimal("0.21")).setScale(2, java.math.RoundingMode.HALF_UP);
                    });

            // When
            Optional<SuscripcionDTO> resultado = suscripcionService.cambiarPlan(1L, 2L);

            // Then
            assertTrue(resultado.isPresent());
            verify(facturaRepository, times(1)).save(any()); // Se genera factura de prorrateo
        }

        @Test
        @DisplayName("No debe generar prorrateo al cambiar a plan más barato")
        void testCambiarPlan_NoProrrateoSiPlanMasBarato() {
            // Given: Usuario con plan Premium quiere cambiar a Basic
            suscripcion.setPlan(planPremium);
            suscripcion.setPrecioActual(planPremium.getPrecioMensual());
            
            when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(suscripcion));
            when(planRepository.findById(1L)).thenReturn(Optional.of(planBasic));
            when(suscripcionRepository.save(any(Suscripcion.class))).thenReturn(suscripcion);

            // When
            Optional<SuscripcionDTO> resultado = suscripcionService.cambiarPlan(1L, 1L);

            // Then
            assertTrue(resultado.isPresent());
            verify(facturaRepository, never()).save(any()); // NO se genera factura de prorrateo
        }
    }

    @Nested
    @DisplayName("Tests de Cambio de Estado")
    class CambioEstadoTests {

        @Test
        @DisplayName("Debe cambiar estado de ACTIVA a CANCELADA")
        void testCambiarEstado_ActivaACancelada() {
            // Given
            when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(suscripcion));
            when(suscripcionRepository.save(any(Suscripcion.class))).thenReturn(suscripcion);

            // When
            Optional<SuscripcionDTO> resultado = suscripcionService.cambiarEstado(1L, EstadoSuscripcion.CANCELADA);

            // Then
            assertTrue(resultado.isPresent());
            assertEquals(EstadoSuscripcion.CANCELADA, suscripcion.getEstado());
        }

        @Test
        @DisplayName("Debe cambiar estado de ACTIVA a MOROSA")
        void testCambiarEstado_ActivaAMorosa() {
            // Given
            when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(suscripcion));
            when(suscripcionRepository.save(any(Suscripcion.class))).thenReturn(suscripcion);

            // When
            Optional<SuscripcionDTO> resultado = suscripcionService.cambiarEstado(1L, EstadoSuscripcion.MOROSA);

            // Then
            assertTrue(resultado.isPresent());
            assertEquals(EstadoSuscripcion.MOROSA, suscripcion.getEstado());
        }

        @Test
        @DisplayName("Debe devolver vacío si la suscripción no existe")
        void testCambiarEstado_SuscripcionNoExiste() {
            // Given
            when(suscripcionRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<SuscripcionDTO> resultado = suscripcionService.cambiarEstado(999L, EstadoSuscripcion.CANCELADA);

            // Then
            assertTrue(resultado.isEmpty());
        }
    }

    @Nested
    @DisplayName("Tests de Creación de Suscripción")
    class CreacionSuscripcionTests {

        @Test
        @DisplayName("Debe crear suscripción correctamente con estado ACTIVA por defecto")
        void testCrearSuscripcion_EstadoActivaPorDefecto() {
            // Given
            SuscripcionDTO dto = SuscripcionDTO.builder()
                    .usuarioId(1L)
                    .planId(1L)
                    .fechaInicio(LocalDate.now())
                    .build();

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(planRepository.findById(1L)).thenReturn(Optional.of(planBasic));
            when(suscripcionRepository.save(any(Suscripcion.class))).thenAnswer(inv -> {
                Suscripcion s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });

            // When
            Optional<SuscripcionDTO> resultado = suscripcionService.create(dto);

            // Then
            assertTrue(resultado.isPresent());
            assertEquals(EstadoSuscripcion.ACTIVA, resultado.get().getEstado());
        }

        @Test
        @DisplayName("Debe asignar precio del plan si no se especifica")
        void testCrearSuscripcion_PrecioDelPlanPorDefecto() {
            // Given
            SuscripcionDTO dto = SuscripcionDTO.builder()
                    .usuarioId(1L)
                    .planId(1L)
                    .fechaInicio(LocalDate.now())
                    .build();

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(planRepository.findById(1L)).thenReturn(Optional.of(planBasic));
            when(suscripcionRepository.save(any(Suscripcion.class))).thenAnswer(inv -> {
                Suscripcion s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });

            // When
            Optional<SuscripcionDTO> resultado = suscripcionService.create(dto);

            // Then
            assertTrue(resultado.isPresent());
            assertEquals(planBasic.getPrecioMensual(), resultado.get().getPrecioActual());
        }

        @Test
        @DisplayName("Debe devolver vacío si el usuario no existe")
        void testCrearSuscripcion_UsuarioNoExiste() {
            // Given
            SuscripcionDTO dto = SuscripcionDTO.builder()
                    .usuarioId(999L)
                    .planId(1L)
                    .fechaInicio(LocalDate.now())
                    .build();

            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());
            when(planRepository.findById(1L)).thenReturn(Optional.of(planBasic));

            // When
            Optional<SuscripcionDTO> resultado = suscripcionService.create(dto);

            // Then
            assertTrue(resultado.isEmpty());
        }
    }

    @Nested
    @DisplayName("Tests de Facturación Mensual")
    class FacturacionMensualTests {

        @Test
        @DisplayName("Debe generar factura mensual con IVA correcto")
        void testGenerarFacturaMensual_ConIVA() {
            // Given
            when(facturaRepository.save(any())).thenAnswer(inv -> {
                var factura = inv.getArgument(0);
                return factura;
            });
            when(suscripcionRepository.save(any())).thenReturn(suscripcion);
            // Mock del servicio de impuestos (IVA 21% por defecto para España)
            when(impuestoService.obtenerTasaImpuesto(anyString())).thenReturn(new BigDecimal("21.00"));
            when(impuestoService.calcularImpuesto(any(BigDecimal.class), anyString()))
                    .thenAnswer(inv -> {
                        BigDecimal subtotal = inv.getArgument(0);
                        return subtotal.multiply(new BigDecimal("0.21")).setScale(2, java.math.RoundingMode.HALF_UP);
                    });

            // When
            var factura = suscripcionService.generarFacturaMensual(suscripcion);

            // Then
            assertNotNull(factura);
            assertEquals(new BigDecimal("9.99"), factura.getSubtotal());
            assertEquals(new BigDecimal("21.00"), factura.getPorcentajeImpuestos());
            assertTrue(factura.getTotal().compareTo(factura.getSubtotal()) > 0); // Total > Subtotal
        }

        @Test
        @DisplayName("Debe actualizar fecha de próximo cobro tras generar factura")
        void testGenerarFacturaMensual_ActualizaFechaProximoCobro() {
            // Given
            LocalDate fechaOriginal = suscripcion.getFechaProximoCobro();
            when(facturaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(suscripcionRepository.save(any())).thenReturn(suscripcion);
            // Mock del servicio de impuestos (IVA 21% por defecto para España)
            when(impuestoService.obtenerTasaImpuesto(anyString())).thenReturn(new BigDecimal("21.00"));
            when(impuestoService.calcularImpuesto(any(BigDecimal.class), anyString()))
                    .thenAnswer(inv -> {
                        BigDecimal subtotal = inv.getArgument(0);
                        return subtotal.multiply(new BigDecimal("0.21")).setScale(2, java.math.RoundingMode.HALF_UP);
                    });

            // When
            suscripcionService.generarFacturaMensual(suscripcion);

            // Then
            assertEquals(fechaOriginal.plusDays(30), suscripcion.getFechaProximoCobro());
        }
    }
}
