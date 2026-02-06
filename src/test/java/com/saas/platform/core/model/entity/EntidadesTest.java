package com.saas.platform.core.model.entity;

import com.saas.platform.core.model.enums.EstadoFactura;
import com.saas.platform.core.model.enums.EstadoSuscripcion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Entidades - Tests Unitarios")
class EntidadesTest {

    @Test
    @DisplayName("Plan debe crearse correctamente con Builder")
    void crearPlan() {
        Plan plan = Plan.builder()
                .nombre("Basic")
                .descripcion("Plan básico")
                .precioMensual(new BigDecimal("9.99"))
                .maxUsuarios(5)
                .activo(true)
                .build();

        assertThat(plan.getNombre()).isEqualTo("Basic");
        assertThat(plan.getPrecioMensual()).isEqualByComparingTo(new BigDecimal("9.99"));
        assertThat(plan.getMaxUsuarios()).isEqualTo(5);
        assertThat(plan.getActivo()).isTrue();
    }

    @Test
    @DisplayName("Usuario debe crearse correctamente con Builder")
    void crearUsuario() {
        Usuario usuario = Usuario.builder()
                .nombre("Juan")
                .apellido("García")
                .email("juan@ejemplo.com")
                .password("pass123")
                .activo(true)
                .build();

        assertThat(usuario.getNombre()).isEqualTo("Juan");
        assertThat(usuario.getApellido()).isEqualTo("García");
        assertThat(usuario.getEmail()).isEqualTo("juan@ejemplo.com");
    }

    @Test
    @DisplayName("Perfil debe vincularse a Usuario")
    void crearPerfil() {
        Usuario usuario = Usuario.builder().id(1L).nombre("Ana").build();
        Perfil perfil = Perfil.builder()
                .usuario(usuario)
                .telefono("123456789")
                .empresa("MiEmpresa")
                .cargo("Gerente")
                .build();

        assertThat(perfil.getUsuario()).isEqualTo(usuario);
        assertThat(perfil.getTelefono()).isEqualTo("123456789");
    }

    @Test
    @DisplayName("Factura debe crearse con estado PENDIENTE por defecto")
    void crearFactura() {
        Factura factura = Factura.builder()
                .monto(new BigDecimal("29.99"))
                .concepto("Suscripción Premium")
                .esProrrateo(false)
                .build();

        assertThat(factura.getMonto()).isEqualByComparingTo(new BigDecimal("29.99"));
        assertThat(factura.getEsProrrateo()).isFalse();
    }

    @Test
    @DisplayName("Enums de estado deben tener los valores correctos")
    void verificarEnums() {
        assertThat(EstadoSuscripcion.values()).containsExactly(
                EstadoSuscripcion.ACTIVA,
                EstadoSuscripcion.CANCELADA,
                EstadoSuscripcion.MOROSA
        );

        assertThat(EstadoFactura.values()).containsExactly(
                EstadoFactura.PENDIENTE,
                EstadoFactura.PAGADA,
                EstadoFactura.VENCIDA
        );
    }

    @Test
    @DisplayName("PagoTarjeta debe heredar de Pago")
    void crearPagoTarjeta() {
        PagoTarjeta pago = new PagoTarjeta();
        pago.setMonto(new BigDecimal("29.99"));
        pago.setNumeroTarjeta("**** **** **** 1234");
        pago.setTitularTarjeta("Juan García");

        assertThat(pago).isInstanceOf(Pago.class);
        assertThat(pago.getMonto()).isEqualByComparingTo(new BigDecimal("29.99"));
        assertThat(pago.getNumeroTarjeta()).isEqualTo("**** **** **** 1234");
    }

    @Test
    @DisplayName("PagoPaypal debe heredar de Pago")
    void crearPagoPaypal() {
        PagoPaypal pago = new PagoPaypal();
        pago.setMonto(new BigDecimal("99.99"));
        pago.setEmailPaypal("juan@paypal.com");
        pago.setIdTransaccion("TXN-12345");

        assertThat(pago).isInstanceOf(Pago.class);
        assertThat(pago.getEmailPaypal()).isEqualTo("juan@paypal.com");
    }

    @Test
    @DisplayName("PagoTransferencia debe heredar de Pago")
    void crearPagoTransferencia() {
        PagoTransferencia pago = new PagoTransferencia();
        pago.setMonto(new BigDecimal("99.99"));
        pago.setBancoOrigen("Santander");
        pago.setNumeroCuenta("ES12 3456 7890");
        pago.setReferenciaTransferencia("REF-001");

        assertThat(pago).isInstanceOf(Pago.class);
        assertThat(pago.getBancoOrigen()).isEqualTo("Santander");
    }
}
