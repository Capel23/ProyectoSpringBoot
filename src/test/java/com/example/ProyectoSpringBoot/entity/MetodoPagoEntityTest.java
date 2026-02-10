package com.example.ProyectoSpringBoot.entity;

import com.example.ProyectoSpringBoot.enums.TipoMetodoPago;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la herencia de MetodoPago
 * Valida la herencia de tablas: TarjetaCredito, PayPal, Transferencia
 */
class MetodoPagoEntityTest {

    @Nested
    @DisplayName("Tests de TarjetaCredito")
    class TarjetaCreditoTests {

        @Test
        @DisplayName("Debe crear TarjetaCredito correctamente")
        void testCrearTarjetaCredito() {
            TarjetaCredito tarjeta = TarjetaCredito.builder()
                    .nombreTitular("Juan García")
                    .numeroTarjeta("4111111111111111")
                    .mesExpiracion(12)
                    .anioExpiracion(2027)
                    .marcaTarjeta("VISA")
                    .build();

            assertEquals("Juan García", tarjeta.getNombreTitular());
            assertEquals(12, tarjeta.getMesExpiracion());
            assertEquals(2027, tarjeta.getAnioExpiracion());
            assertEquals(TipoMetodoPago.TARJETA_CREDITO, tarjeta.getTipoMetodoPago());
        }

        @Test
        @DisplayName("getMascarado() debe ocultar número excepto últimos 4 dígitos")
        void testGetMascarado() {
            TarjetaCredito tarjeta = TarjetaCredito.builder()
                    .numeroTarjeta("4111111111111111")
                    .build();

            String mascarado = tarjeta.getMascarado();
            assertEquals("**** **** **** 1111", mascarado);
        }

        @Test
        @DisplayName("esValido() debe devolver true si tarjeta no expiró")
        void testEsValido_NoExpirada() {
            TarjetaCredito tarjeta = TarjetaCredito.builder()
                    .mesExpiracion(12)
                    .anioExpiracion(2030)
                    .build();

            assertTrue(tarjeta.esValido());
        }

        @Test
        @DisplayName("esValido() debe devolver false si tarjeta expiró")
        void testEsValido_Expirada() {
            TarjetaCredito tarjeta = TarjetaCredito.builder()
                    .mesExpiracion(1)
                    .anioExpiracion(2020)
                    .build();

            assertFalse(tarjeta.esValido());
        }
    }

    @Nested
    @DisplayName("Tests de PayPal")
    class PayPalTests {

        @Test
        @DisplayName("Debe crear PayPal correctamente")
        void testCrearPayPal() {
            PayPal paypal = PayPal.builder()
                    .emailPaypal("usuario@ejemplo.com")
                    .paypalId("PAYPAL123")
                    .cuentaVerificada(true)
                    .build();

            assertEquals("usuario@ejemplo.com", paypal.getEmailPaypal());
            assertEquals("PAYPAL123", paypal.getPaypalId());
            assertTrue(paypal.getCuentaVerificada());
            assertEquals(TipoMetodoPago.PAYPAL, paypal.getTipoMetodoPago());
        }

        @Test
        @DisplayName("getMascarado() debe ocultar parte del email")
        void testGetMascarado() {
            PayPal paypal = PayPal.builder()
                    .emailPaypal("usuario@ejemplo.com")
                    .build();

            String mascarado = paypal.getMascarado();
            assertTrue(mascarado.contains("@"));
            assertTrue(mascarado.contains("***"));
            // Debe mostrar primeros 2 caracteres + ***@dominio
            assertEquals("us***@ejemplo.com", mascarado);
        }

        @Test
        @DisplayName("esValido() debe devolver true si email tiene formato correcto")
        void testEsValido_EmailCorrecto() {
            PayPal paypal = PayPal.builder()
                    .emailPaypal("test@example.com")
                    .build();

            assertTrue(paypal.esValido());
        }

        @Test
        @DisplayName("esValido() debe devolver false si email no tiene @")
        void testEsValido_EmailIncorrecto() {
            PayPal paypal = PayPal.builder()
                    .emailPaypal("emailsindominio")
                    .build();

            assertFalse(paypal.esValido());
        }
    }

    @Nested
    @DisplayName("Tests de Transferencia")
    class TransferenciaTests {

        @Test
        @DisplayName("Debe crear Transferencia correctamente")
        void testCrearTransferencia() {
            Transferencia transferencia = Transferencia.builder()
                    .nombreBanco("BBVA")
                    .iban("ES9121000418450200051332")
                    .codigoSwift("BBVAESMMXXX")
                    .nombreTitular("María López")
                    .paisBanco("España")
                    .build();

            assertEquals("BBVA", transferencia.getNombreBanco());
            assertEquals("ES9121000418450200051332", transferencia.getIban());
            assertEquals("BBVAESMMXXX", transferencia.getCodigoSwift());
            assertEquals(TipoMetodoPago.TRANSFERENCIA, transferencia.getTipoMetodoPago());
        }

        @Test
        @DisplayName("getMascarado() debe ocultar IBAN excepto últimos 4 dígitos")
        void testGetMascarado() {
            Transferencia transferencia = Transferencia.builder()
                    .iban("ES9121000418450200051332")
                    .build();

            String mascarado = transferencia.getMascarado();
            assertTrue(mascarado.startsWith("****"));
            assertTrue(mascarado.endsWith("1332"));
        }

        @Test
        @DisplayName("esValido() debe devolver true para IBAN válido")
        void testEsValido_IbanValido() {
            Transferencia transferencia = Transferencia.builder()
                    .iban("ES9121000418450200051332") // IBAN español válido
                    .build();

            assertTrue(transferencia.esValido());
        }

        @Test
        @DisplayName("esValido() debe devolver false para IBAN muy corto")
        void testEsValido_IbanMuyCorto() {
            Transferencia transferencia = Transferencia.builder()
                    .iban("ES91") // Muy corto
                    .build();

            assertFalse(transferencia.esValido());
        }
    }

    @Nested
    @DisplayName("Tests de Polimorfismo - Herencia de Tablas")
    class PolimorfismoTests {

        @Test
        @DisplayName("Todos los métodos de pago deben ser instancias de MetodoPago")
        void testHerencia() {
            TarjetaCredito tarjeta = new TarjetaCredito();
            PayPal paypal = new PayPal();
            Transferencia transferencia = new Transferencia();

            assertInstanceOf(MetodoPago.class, tarjeta);
            assertInstanceOf(MetodoPago.class, paypal);
            assertInstanceOf(MetodoPago.class, transferencia);
        }

        @Test
        @DisplayName("Cada tipo debe devolver su TipoMetodoPago correcto")
        void testTipoMetodoPago() {
            TarjetaCredito tarjeta = new TarjetaCredito();
            PayPal paypal = new PayPal();
            Transferencia transferencia = new Transferencia();

            assertEquals(TipoMetodoPago.TARJETA_CREDITO, tarjeta.getTipoMetodoPago());
            assertEquals(TipoMetodoPago.PAYPAL, paypal.getTipoMetodoPago());
            assertEquals(TipoMetodoPago.TRANSFERENCIA, transferencia.getTipoMetodoPago());
        }
    }
}
