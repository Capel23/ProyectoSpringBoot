package com.example.ProyectoSpringBoot.entity;

import com.example.ProyectoSpringBoot.enums.TipoPlan;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la entidad Plan
 * Valida que los planes (BASIC, PREMIUM, ENTERPRISE) se crean correctamente
 */
class PlanEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Tests de Creación de Planes")
    class CreacionPlanesTests {

        @Test
        @DisplayName("Debe crear Plan BASIC correctamente")
        void testCrearPlanBasic() {
            // Given & When
            Plan plan = Plan.builder()
                    .nombre("Plan Básico")
                    .tipoPlan(TipoPlan.BASIC)
                    .precioMensual(new BigDecimal("9.99"))
                    .descripcion("Plan ideal para comenzar")
                    .maxUsuarios(1)
                    .almacenamientoGb(5)
                    .soportePrioritario(false)
                    .activo(true)
                    .build();

            // Then
            assertEquals("Plan Básico", plan.getNombre());
            assertEquals(TipoPlan.BASIC, plan.getTipoPlan());
            assertEquals(new BigDecimal("9.99"), plan.getPrecioMensual());
            assertEquals(1, plan.getMaxUsuarios());
            assertEquals(5, plan.getAlmacenamientoGb());
            assertFalse(plan.getSoportePrioritario());
            assertTrue(plan.getActivo());
        }

        @Test
        @DisplayName("Debe crear Plan PREMIUM correctamente")
        void testCrearPlanPremium() {
            // Given & When
            Plan plan = Plan.builder()
                    .nombre("Plan Premium")
                    .tipoPlan(TipoPlan.PREMIUM)
                    .precioMensual(new BigDecimal("29.99"))
                    .descripcion("Para equipos en crecimiento")
                    .maxUsuarios(5)
                    .almacenamientoGb(50)
                    .soportePrioritario(true)
                    .activo(true)
                    .build();

            // Then
            assertEquals(TipoPlan.PREMIUM, plan.getTipoPlan());
            assertEquals(new BigDecimal("29.99"), plan.getPrecioMensual());
            assertEquals(5, plan.getMaxUsuarios());
            assertTrue(plan.getSoportePrioritario());
        }

        @Test
        @DisplayName("Debe crear Plan ENTERPRISE correctamente")
        void testCrearPlanEnterprise() {
            // Given & When
            Plan plan = Plan.builder()
                    .nombre("Plan Enterprise")
                    .tipoPlan(TipoPlan.ENTERPRISE)
                    .precioMensual(new BigDecimal("99.99"))
                    .descripcion("Solución empresarial completa")
                    .maxUsuarios(null) // Ilimitado
                    .almacenamientoGb(500)
                    .soportePrioritario(true)
                    .activo(true)
                    .build();

            // Then
            assertEquals(TipoPlan.ENTERPRISE, plan.getTipoPlan());
            assertEquals(new BigDecimal("99.99"), plan.getPrecioMensual());
            assertNull(plan.getMaxUsuarios()); // Usuarios ilimitados
            assertEquals(500, plan.getAlmacenamientoGb());
        }
    }

    @Nested
    @DisplayName("Tests de Validación de Campos")
    class ValidacionCamposTests {

        @Test
        @DisplayName("Debe fallar validación si nombre está vacío")
        void testValidacion_NombreVacio() {
            // Given
            Plan plan = Plan.builder()
                    .nombre("")
                    .tipoPlan(TipoPlan.BASIC)
                    .precioMensual(new BigDecimal("9.99"))
                    .build();

            // When
            Set<ConstraintViolation<Plan>> violations = validator.validate(plan);

            // Then
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("nombre")));
        }

        @Test
        @DisplayName("Debe fallar validación si tipo plan es null")
        void testValidacion_TipoPlanNull() {
            // Given
            Plan plan = Plan.builder()
                    .nombre("Plan Test")
                    .tipoPlan(null)
                    .precioMensual(new BigDecimal("9.99"))
                    .build();

            // When
            Set<ConstraintViolation<Plan>> violations = validator.validate(plan);

            // Then
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("tipoPlan")));
        }

        @Test
        @DisplayName("Debe fallar validación si precio es null")
        void testValidacion_PrecioNull() {
            // Given
            Plan plan = Plan.builder()
                    .nombre("Plan Test")
                    .tipoPlan(TipoPlan.BASIC)
                    .precioMensual(null)
                    .build();

            // When
            Set<ConstraintViolation<Plan>> violations = validator.validate(plan);

            // Then
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("precioMensual")));
        }

        @Test
        @DisplayName("Debe fallar validación si precio es negativo")
        void testValidacion_PrecioNegativo() {
            // Given
            Plan plan = Plan.builder()
                    .nombre("Plan Test")
                    .tipoPlan(TipoPlan.BASIC)
                    .precioMensual(new BigDecimal("-5.00"))
                    .build();

            // When
            Set<ConstraintViolation<Plan>> violations = validator.validate(plan);

            // Then
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Plan válido no debe tener violaciones")
        void testValidacion_PlanValido() {
            // Given
            Plan plan = Plan.builder()
                    .nombre("Plan Test")
                    .tipoPlan(TipoPlan.BASIC)
                    .precioMensual(new BigDecimal("9.99"))
                    .build();

            // When
            Set<ConstraintViolation<Plan>> violations = validator.validate(plan);

            // Then
            assertTrue(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("Tests de Comparación de Precios entre Planes")
    class ComparacionPreciosTests {

        @Test
        @DisplayName("Plan PREMIUM debe ser más caro que BASIC")
        void testPremiumMasCaroQueBasic() {
            // Given
            Plan basic = Plan.builder()
                    .tipoPlan(TipoPlan.BASIC)
                    .precioMensual(new BigDecimal("9.99"))
                    .build();

            Plan premium = Plan.builder()
                    .tipoPlan(TipoPlan.PREMIUM)
                    .precioMensual(new BigDecimal("29.99"))
                    .build();

            // Then
            assertTrue(premium.getPrecioMensual().compareTo(basic.getPrecioMensual()) > 0);
        }

        @Test
        @DisplayName("Plan ENTERPRISE debe ser más caro que PREMIUM")
        void testEnterpriseMasCaroQuePremium() {
            // Given
            Plan premium = Plan.builder()
                    .tipoPlan(TipoPlan.PREMIUM)
                    .precioMensual(new BigDecimal("29.99"))
                    .build();

            Plan enterprise = Plan.builder()
                    .tipoPlan(TipoPlan.ENTERPRISE)
                    .precioMensual(new BigDecimal("99.99"))
                    .build();

            // Then
            assertTrue(enterprise.getPrecioMensual().compareTo(premium.getPrecioMensual()) > 0);
        }
    }
}
