package com.saas.platform.core.service;

import com.saas.platform.core.model.entity.*;
import com.saas.platform.core.model.enums.EstadoFactura;
import com.saas.platform.core.model.enums.EstadoSuscripcion;
import com.saas.platform.core.repository.FacturaRepository;
import com.saas.platform.core.repository.SuscripcionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuscripcionService - Tests Unitarios")
class SuscripcionServiceTest {

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private FacturaRepository facturaRepository;

    @InjectMocks
    private SuscripcionService suscripcionService;

    private Usuario usuario;
    private Plan planBasic;
    private Plan planPremium;
    private Plan planEnterprise;
    private Suscripcion suscripcion;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("García")
                .email("juan@ejemplo.com")
                .activo(true)
                .build();

        planBasic = Plan.builder()
                .id(1L)
                .nombre("Basic")
                .precioMensual(new BigDecimal("9.99"))
                .maxUsuarios(5)
                .activo(true)
                .build();

        planPremium = Plan.builder()
                .id(2L)
                .nombre("Premium")
                .precioMensual(new BigDecimal("29.99"))
                .maxUsuarios(25)
                .activo(true)
                .build();

        planEnterprise = Plan.builder()
                .id(3L)
                .nombre("Enterprise")
                .precioMensual(new BigDecimal("99.99"))
                .maxUsuarios(100)
                .activo(true)
                .build();

        suscripcion = Suscripcion.builder()
                .id(1L)
                .usuario(usuario)
                .plan(planBasic)
                .estado(EstadoSuscripcion.ACTIVA)
                .fechaInicio(LocalDate.now())
                .proximaFechaFacturacion(LocalDate.now().plusDays(30))
                .build();
    }

    @Test
    @DisplayName("Debe crear una suscripción y generar la primera factura")
    void crearSuscripcion() {
        when(suscripcionRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());
        when(suscripcionRepository.save(any(Suscripcion.class))).thenReturn(suscripcion);
        when(facturaRepository.save(any(Factura.class))).thenReturn(Factura.builder()
                .id(1L).monto(new BigDecimal("9.99")).estado(EstadoFactura.PENDIENTE).build());

        Suscripcion resultado = suscripcionService.crear(usuario, planBasic);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getPlan().getNombre()).isEqualTo("Basic");
        assertThat(resultado.getEstado()).isEqualTo(EstadoSuscripcion.ACTIVA);
        verify(facturaRepository).save(any(Factura.class)); // Primera factura generada
    }

    @Test
    @DisplayName("Debe lanzar excepción si usuario ya tiene suscripción activa")
    void crearSuscripcionDuplicada() {
        when(suscripcionRepository.findByUsuarioId(1L)).thenReturn(Optional.of(suscripcion));

        assertThatThrownBy(() -> suscripcionService.crear(usuario, planBasic))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya tiene una suscripción activa");
    }

    @Test
    @DisplayName("Debe cambiar de plan Basic a Premium y generar prorrateo")
    void cambiarPlanConProrrateo() {
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(suscripcion));
        when(suscripcionRepository.save(any(Suscripcion.class))).thenReturn(suscripcion);
        when(facturaRepository.save(any(Factura.class))).thenReturn(Factura.builder()
                .id(2L).monto(new BigDecimal("20.00")).esProrrateo(true).estado(EstadoFactura.PENDIENTE).build());

        Suscripcion resultado = suscripcionService.cambiarPlan(1L, planPremium);

        assertThat(resultado.getPlan()).isEqualTo(planPremium);
        verify(facturaRepository).save(any(Factura.class)); // Factura de prorrateo
    }

    @Test
    @DisplayName("No debe generar prorrateo al cambiar de plan caro a barato")
    void cambiarPlanSinProrrateo() {
        suscripcion.setPlan(planPremium); // Actualmente en Premium
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(suscripcion));
        when(suscripcionRepository.save(any(Suscripcion.class))).thenReturn(suscripcion);

        Suscripcion resultado = suscripcionService.cambiarPlan(1L, planBasic);

        assertThat(resultado.getPlan()).isEqualTo(planBasic);
        verify(facturaRepository, never()).save(any(Factura.class)); // Sin prorrateo
    }

    @Test
    @DisplayName("Debe lanzar excepción al cambiar plan de suscripción cancelada")
    void cambiarPlanSuscripcionCancelada() {
        suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(suscripcion));

        assertThatThrownBy(() -> suscripcionService.cambiarPlan(1L, planPremium))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se puede cambiar el plan de una suscripción activa");
    }

    @Test
    @DisplayName("Debe calcular el prorrateo correctamente")
    void calcularProrrateo() {
        // Con 30 días restantes, la diferencia completa
        suscripcion.setProximaFechaFacturacion(LocalDate.now().plusDays(30));

        BigDecimal prorrateo = suscripcionService.calcularProrrateo(suscripcion, planBasic, planPremium);

        // diferenciaDiaria = (29.99 - 9.99) / 30 = 0.6667
        // prorrateo = 0.6667 * 30 = 20.00
        assertThat(prorrateo).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("Debe calcular prorrateo con días parciales")
    void calcularProrrateoConDiasParciales() {
        suscripcion.setProximaFechaFacturacion(LocalDate.now().plusDays(15));

        BigDecimal prorrateo = suscripcionService.calcularProrrateo(suscripcion, planBasic, planPremium);

        // diferenciaDiaria = (29.99 - 9.99) / 30 = 0.6667
        // prorrateo = 0.6667 * 15 = 10.00
        assertThat(prorrateo).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("Debe generar facturas automáticas para suscripciones que lo requieren")
    void generarFacturasAutomaticas() {
        List<Suscripcion> suscripciones = List.of(suscripcion);
        when(suscripcionRepository.findByEstadoAndProximaFechaFacturacionLessThanEqual(
                eq(EstadoSuscripcion.ACTIVA), any(LocalDate.class))).thenReturn(suscripciones);
        when(facturaRepository.save(any(Factura.class))).thenReturn(Factura.builder()
                .id(3L).monto(new BigDecimal("9.99")).estado(EstadoFactura.PENDIENTE).build());
        when(suscripcionRepository.save(any(Suscripcion.class))).thenReturn(suscripcion);

        List<Factura> facturas = suscripcionService.generarFacturasAutomaticas();

        assertThat(facturas).hasSize(1);
        verify(facturaRepository).save(any(Factura.class));
        verify(suscripcionRepository).save(any(Suscripcion.class)); // Actualiza próxima fecha
    }

    @Test
    @DisplayName("Debe cancelar una suscripción")
    void cancelarSuscripcion() {
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(suscripcion));
        when(suscripcionRepository.save(any(Suscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        Suscripcion resultado = suscripcionService.cancelar(1L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoSuscripcion.CANCELADA);
        assertThat(resultado.getFechaFin()).isNotNull();
    }

    @Test
    @DisplayName("Debe cambiar de Basic directo a Enterprise con prorrateo")
    void cambiarDePlanBasicAEnterprise() {
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(suscripcion));
        when(suscripcionRepository.save(any(Suscripcion.class))).thenReturn(suscripcion);
        when(facturaRepository.save(any(Factura.class))).thenReturn(Factura.builder()
                .id(4L).monto(new BigDecimal("90.00")).esProrrateo(true).estado(EstadoFactura.PENDIENTE).build());

        suscripcionService.cambiarPlan(1L, planEnterprise);

        verify(facturaRepository).save(any(Factura.class)); // Prorrateo generado
    }
}
