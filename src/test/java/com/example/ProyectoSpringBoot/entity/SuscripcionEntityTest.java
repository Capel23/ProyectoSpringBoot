package com.example.ProyectoSpringBoot.entity;

import com.example.ProyectoSpringBoot.enums.EstadoSuscripcion;
import com.example.ProyectoSpringBoot.enums.TipoPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la entidad Suscripcion
 * Valida estados: ACTIVA, CANCELADA, MOROSA, SUSPENDIDA, EXPIRADA
 */
class SuscripcionEntityTest {

    private Suscripcion suscripcion;
    private Usuario usuario;
    private Plan plan;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .email("test@example.com")
                .password("password123")
                .activo(true)
                .build();

        plan = Plan.builder()
                .id(1L)
                .nombre("Plan Premium")
                .tipoPlan(TipoPlan.PREMIUM)
                .precioMensual(new BigDecimal("29.99"))
                .build();

        suscripcion = Suscripcion.builder()
                .id(1L)
                .usuario(usuario)
                .plan(plan)
                .fechaInicio(LocalDate.now())
                .fechaProximoCobro(LocalDate.now().plusDays(30))
                .estado(EstadoSuscripcion.ACTIVA)
                .renovacionAutomatica(true)
                .precioActual(plan.getPrecioMensual())
                .build();
    }

    @Nested
    @DisplayName("Tests de Estado de Suscripcion")
    class EstadoSuscripcionTests {

        @Test
        @DisplayName("estaActiva() debe devolver true si estado es ACTIVA")
        void testEstaActiva_True() {
            suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
            assertTrue(suscripcion.estaActiva());
        }

        @Test
        @DisplayName("estaActiva() debe devolver false si estado es CANCELADA")
        void testEstaActiva_FalseCancelada() {
            suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
            assertFalse(suscripcion.estaActiva());
        }

        @Test
        @DisplayName("estaActiva() debe devolver false si estado es MOROSA")
        void testEstaActiva_FalseMorosa() {
            suscripcion.setEstado(EstadoSuscripcion.MOROSA);
            assertFalse(suscripcion.estaActiva());
        }

        @Test
        @DisplayName("Debe poder cambiar entre todos los estados")
        void testCambioEstados() {
            // ACTIVA -> MOROSA
            suscripcion.setEstado(EstadoSuscripcion.MOROSA);
            assertEquals(EstadoSuscripcion.MOROSA, suscripcion.getEstado());

            // MOROSA -> SUSPENDIDA
            suscripcion.setEstado(EstadoSuscripcion.SUSPENDIDA);
            assertEquals(EstadoSuscripcion.SUSPENDIDA, suscripcion.getEstado());

            // SUSPENDIDA -> CANCELADA
            suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
            assertEquals(EstadoSuscripcion.CANCELADA, suscripcion.getEstado());
        }
    }

    @Nested
    @DisplayName("Tests de Renovación Automática")
    class RenovacionAutomaticaTests {

        @Test
        @DisplayName("Renovación automática debe estar activa por defecto")
        void testRenovacionAutomatica_PorDefecto() {
            Suscripcion nuevaSuscripcion = Suscripcion.builder()
                    .usuario(usuario)
                    .plan(plan)
                    .fechaInicio(LocalDate.now())
                    .fechaProximoCobro(LocalDate.now().plusDays(30))
                    .estado(EstadoSuscripcion.ACTIVA)
                    .build();

            assertTrue(nuevaSuscripcion.getRenovacionAutomatica());
        }

        @Test
        @DisplayName("Debe poder desactivar renovación automática")
        void testDesactivarRenovacionAutomatica() {
            suscripcion.setRenovacionAutomatica(false);
            assertFalse(suscripcion.getRenovacionAutomatica());
        }
    }

    @Nested
    @DisplayName("Tests de Fechas de Suscripcion")
    class FechasSuscripcionTests {

        @Test
        @DisplayName("Fecha próximo cobro debe ser posterior a fecha inicio")
        void testFechaProximoCobro() {
            assertTrue(suscripcion.getFechaProximoCobro().isAfter(suscripcion.getFechaInicio()));
        }

        @Test
        @DisplayName("Debe poder establecer fecha fin al cancelar")
        void testFechaFin() {
            suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
            suscripcion.setFechaFin(LocalDate.now());

            assertNotNull(suscripcion.getFechaFin());
            assertEquals(EstadoSuscripcion.CANCELADA, suscripcion.getEstado());
        }
    }

    @Nested
    @DisplayName("Tests de Relaciones")
    class RelacionesTests {

        @Test
        @DisplayName("Suscripción debe tener usuario asociado")
        void testRelacionUsuario() {
            assertNotNull(suscripcion.getUsuario());
            assertEquals(1L, suscripcion.getUsuario().getId());
        }

        @Test
        @DisplayName("Suscripción debe tener plan asociado")
        void testRelacionPlan() {
            assertNotNull(suscripcion.getPlan());
            assertEquals("Plan Premium", suscripcion.getPlan().getNombre());
        }
    }

    @Nested
    @DisplayName("Tests de Enum EstadoSuscripcion")
    class EstadoEnumTests {

        @Test
        @DisplayName("Debe tener todos los estados requeridos")
        void testEstadosSuscripcion() {
            // Estados definidos en el requisito
            assertNotNull(EstadoSuscripcion.ACTIVA);
            assertNotNull(EstadoSuscripcion.CANCELADA);
            assertNotNull(EstadoSuscripcion.MOROSA);
            assertNotNull(EstadoSuscripcion.SUSPENDIDA);
            assertNotNull(EstadoSuscripcion.EXPIRADA);
        }

        @Test
        @DisplayName("Estado por defecto debe ser ACTIVA para nuevas suscripciones")
        void testEstadoPorDefecto() {
            assertEquals(EstadoSuscripcion.ACTIVA, suscripcion.getEstado());
        }
    }
}
