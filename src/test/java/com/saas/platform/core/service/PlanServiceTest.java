package com.saas.platform.core.service;

import com.saas.platform.core.model.entity.Plan;
import com.saas.platform.core.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanService - Tests Unitarios")
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanService planService;

    private Plan planBasic;
    private Plan planPremium;
    private Plan planEnterprise;

    @BeforeEach
    void setUp() {
        planBasic = Plan.builder()
                .id(1L)
                .nombre("Basic")
                .descripcion("Plan básico")
                .precioMensual(new BigDecimal("9.99"))
                .maxUsuarios(5)
                .activo(true)
                .build();

        planPremium = Plan.builder()
                .id(2L)
                .nombre("Premium")
                .descripcion("Plan premium")
                .precioMensual(new BigDecimal("29.99"))
                .maxUsuarios(25)
                .activo(true)
                .build();

        planEnterprise = Plan.builder()
                .id(3L)
                .nombre("Enterprise")
                .descripcion("Plan empresarial")
                .precioMensual(new BigDecimal("99.99"))
                .maxUsuarios(100)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Debe crear un plan correctamente")
    void crearPlan() {
        when(planRepository.save(any(Plan.class))).thenReturn(planBasic);

        Plan resultado = planService.crear(planBasic);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Basic");
        assertThat(resultado.getPrecioMensual()).isEqualByComparingTo(new BigDecimal("9.99"));
        verify(planRepository, times(1)).save(any(Plan.class));
    }

    @Test
    @DisplayName("Debe listar todos los planes")
    void listarTodos() {
        when(planRepository.findAll()).thenReturn(Arrays.asList(planBasic, planPremium, planEnterprise));

        List<Plan> planes = planService.listarTodos();

        assertThat(planes).hasSize(3);
        assertThat(planes).extracting(Plan::getNombre)
                .containsExactly("Basic", "Premium", "Enterprise");
    }

    @Test
    @DisplayName("Debe listar solo planes activos")
    void listarActivos() {
        planEnterprise.setActivo(false);
        when(planRepository.findByActivoTrue()).thenReturn(Arrays.asList(planBasic, planPremium));

        List<Plan> activos = planService.listarActivos();

        assertThat(activos).hasSize(2);
    }

    @Test
    @DisplayName("Debe buscar plan por ID")
    void buscarPorId() {
        when(planRepository.findById(1L)).thenReturn(Optional.of(planBasic));

        Optional<Plan> resultado = planService.buscarPorId(1L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Basic");
    }

    @Test
    @DisplayName("Debe retornar vacío si plan no existe")
    void buscarPorIdNoExiste() {
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Plan> resultado = planService.buscarPorId(99L);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Debe buscar plan por nombre")
    void buscarPorNombre() {
        when(planRepository.findByNombre("Premium")).thenReturn(Optional.of(planPremium));

        Optional<Plan> resultado = planService.buscarPorNombre("Premium");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getPrecioMensual()).isEqualByComparingTo(new BigDecimal("29.99"));
    }

    @Test
    @DisplayName("Debe actualizar un plan")
    void actualizarPlan() {
        planBasic.setPrecioMensual(new BigDecimal("14.99"));
        when(planRepository.save(planBasic)).thenReturn(planBasic);

        Plan actualizado = planService.actualizar(planBasic);

        assertThat(actualizado.getPrecioMensual()).isEqualByComparingTo(new BigDecimal("14.99"));
        verify(planRepository).save(planBasic);
    }

    @Test
    @DisplayName("Debe eliminar un plan por ID")
    void eliminarPlan() {
        doNothing().when(planRepository).deleteById(1L);

        planService.eliminar(1L);

        verify(planRepository, times(1)).deleteById(1L);
    }
}
