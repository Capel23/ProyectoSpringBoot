package com.example.ProyectoSpringBoot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para calcular impuestos según el país del usuario.
 * Implementa tasas de IVA/VAT para diferentes países.
 */
@Service
@Slf4j
public class ImpuestoService {

    // Mapa de países con sus tasas de IVA (en porcentaje)
    private static final Map<String, BigDecimal> TASAS_IVA = new ConcurrentHashMap<>();
    
    static {
        // Europa - IVA
        TASAS_IVA.put("ES", new BigDecimal("21.00")); // España
        TASAS_IVA.put("ESPAÑA", new BigDecimal("21.00"));
        TASAS_IVA.put("SPAIN", new BigDecimal("21.00"));
        TASAS_IVA.put("DE", new BigDecimal("19.00")); // Alemania
        TASAS_IVA.put("GERMANY", new BigDecimal("19.00"));
        TASAS_IVA.put("ALEMANIA", new BigDecimal("19.00"));
        TASAS_IVA.put("FR", new BigDecimal("20.00")); // Francia
        TASAS_IVA.put("FRANCE", new BigDecimal("20.00"));
        TASAS_IVA.put("FRANCIA", new BigDecimal("20.00"));
        TASAS_IVA.put("IT", new BigDecimal("22.00")); // Italia
        TASAS_IVA.put("ITALY", new BigDecimal("22.00"));
        TASAS_IVA.put("ITALIA", new BigDecimal("22.00"));
        TASAS_IVA.put("PT", new BigDecimal("23.00")); // Portugal
        TASAS_IVA.put("PORTUGAL", new BigDecimal("23.00"));
        TASAS_IVA.put("GB", new BigDecimal("20.00")); // Reino Unido
        TASAS_IVA.put("UK", new BigDecimal("20.00"));
        TASAS_IVA.put("UNITED KINGDOM", new BigDecimal("20.00"));
        TASAS_IVA.put("REINO UNIDO", new BigDecimal("20.00"));
        TASAS_IVA.put("NL", new BigDecimal("21.00")); // Países Bajos
        TASAS_IVA.put("NETHERLANDS", new BigDecimal("21.00"));
        TASAS_IVA.put("HOLANDA", new BigDecimal("21.00"));
        TASAS_IVA.put("BE", new BigDecimal("21.00")); // Bélgica
        TASAS_IVA.put("BELGIUM", new BigDecimal("21.00"));
        TASAS_IVA.put("BÉLGICA", new BigDecimal("21.00"));
        TASAS_IVA.put("AT", new BigDecimal("20.00")); // Austria
        TASAS_IVA.put("AUSTRIA", new BigDecimal("20.00"));
        TASAS_IVA.put("SE", new BigDecimal("25.00")); // Suecia
        TASAS_IVA.put("SWEDEN", new BigDecimal("25.00"));
        TASAS_IVA.put("SUECIA", new BigDecimal("25.00"));
        TASAS_IVA.put("DK", new BigDecimal("25.00")); // Dinamarca
        TASAS_IVA.put("DENMARK", new BigDecimal("25.00"));
        TASAS_IVA.put("DINAMARCA", new BigDecimal("25.00"));
        TASAS_IVA.put("PL", new BigDecimal("23.00")); // Polonia
        TASAS_IVA.put("POLAND", new BigDecimal("23.00"));
        TASAS_IVA.put("POLONIA", new BigDecimal("23.00"));
        TASAS_IVA.put("IE", new BigDecimal("23.00")); // Irlanda
        TASAS_IVA.put("IRELAND", new BigDecimal("23.00"));
        TASAS_IVA.put("IRLANDA", new BigDecimal("23.00"));
        TASAS_IVA.put("CH", new BigDecimal("7.70"));  // Suiza
        TASAS_IVA.put("SWITZERLAND", new BigDecimal("7.70"));
        TASAS_IVA.put("SUIZA", new BigDecimal("7.70"));
        
        // América
        TASAS_IVA.put("MX", new BigDecimal("16.00")); // México
        TASAS_IVA.put("MEXICO", new BigDecimal("16.00"));
        TASAS_IVA.put("MÉXICO", new BigDecimal("16.00"));
        TASAS_IVA.put("AR", new BigDecimal("21.00")); // Argentina
        TASAS_IVA.put("ARGENTINA", new BigDecimal("21.00"));
        TASAS_IVA.put("CL", new BigDecimal("19.00")); // Chile
        TASAS_IVA.put("CHILE", new BigDecimal("19.00"));
        TASAS_IVA.put("CO", new BigDecimal("19.00")); // Colombia
        TASAS_IVA.put("COLOMBIA", new BigDecimal("19.00"));
        TASAS_IVA.put("PE", new BigDecimal("18.00")); // Perú
        TASAS_IVA.put("PERU", new BigDecimal("18.00"));
        TASAS_IVA.put("PERÚ", new BigDecimal("18.00"));
        TASAS_IVA.put("BR", new BigDecimal("17.00")); // Brasil (ICMS promedio)
        TASAS_IVA.put("BRAZIL", new BigDecimal("17.00"));
        TASAS_IVA.put("BRASIL", new BigDecimal("17.00"));
        TASAS_IVA.put("US", new BigDecimal("0.00"));  // EE.UU. (varía por estado, 0 federal)
        TASAS_IVA.put("USA", new BigDecimal("0.00"));
        TASAS_IVA.put("UNITED STATES", new BigDecimal("0.00"));
        TASAS_IVA.put("ESTADOS UNIDOS", new BigDecimal("0.00"));
        TASAS_IVA.put("CA", new BigDecimal("5.00"));  // Canadá (GST federal)
        TASAS_IVA.put("CANADA", new BigDecimal("5.00"));
        TASAS_IVA.put("CANADÁ", new BigDecimal("5.00"));
    }

    // Tasa por defecto cuando no se conoce el país
    private static final BigDecimal TASA_DEFAULT = new BigDecimal("21.00");

    /**
     * Obtiene la tasa de impuesto para un país específico.
     * @param pais Nombre o código ISO del país
     * @return Porcentaje de impuesto (ej: 21.00 para 21%)
     */
    public BigDecimal obtenerTasaImpuesto(String pais) {
        if (pais == null || pais.isBlank()) {
            log.debug("País no especificado, usando tasa por defecto: {}%", TASA_DEFAULT);
            return TASA_DEFAULT;
        }
        
        String paisNormalizado = pais.toUpperCase().trim();
        BigDecimal tasa = TASAS_IVA.getOrDefault(paisNormalizado, TASA_DEFAULT);
        
        log.debug("Tasa de impuesto para {}: {}%", pais, tasa);
        return tasa;
    }

    /**
     * Calcula el monto de impuesto dado un subtotal y un país.
     * @param subtotal Monto base sin impuestos
     * @param pais País del usuario
     * @return Monto del impuesto
     */
    public BigDecimal calcularImpuesto(BigDecimal subtotal, String pais) {
        BigDecimal tasa = obtenerTasaImpuesto(pais);
        BigDecimal impuesto = subtotal.multiply(tasa)
                .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        
        log.debug("Impuesto calculado: {} ({}% de {})", impuesto, tasa, subtotal);
        return impuesto;
    }

    /**
     * Calcula el total (subtotal + impuesto) para un país.
     * @param subtotal Monto base sin impuestos
     * @param pais País del usuario
     * @return Total incluyendo impuestos
     */
    public BigDecimal calcularTotal(BigDecimal subtotal, String pais) {
        return subtotal.add(calcularImpuesto(subtotal, pais));
    }

    /**
     * Obtiene información detallada de impuesto para un país.
     * @param pais País
     * @return Información del impuesto
     */
    public ImpuestoInfo obtenerInfoImpuesto(String pais) {
        BigDecimal tasa = obtenerTasaImpuesto(pais);
        String nombreImpuesto = determinarNombreImpuesto(pais);
        return new ImpuestoInfo(pais, tasa, nombreImpuesto);
    }

    private String determinarNombreImpuesto(String pais) {
        if (pais == null || pais.isBlank()) return "IVA";
        
        String paisUpper = pais.toUpperCase().trim();
        
        // USA/Canadá usan diferentes nombres
        if (paisUpper.equals("US") || paisUpper.equals("USA") || 
            paisUpper.contains("UNITED STATES") || paisUpper.contains("ESTADOS UNIDOS")) {
            return "Sales Tax";
        }
        if (paisUpper.equals("CA") || paisUpper.equals("CANADA") || paisUpper.equals("CANADÁ")) {
            return "GST";
        }
        if (paisUpper.equals("GB") || paisUpper.equals("UK") || 
            paisUpper.contains("UNITED KINGDOM") || paisUpper.contains("REINO UNIDO")) {
            return "VAT";
        }
        if (paisUpper.equals("BR") || paisUpper.equals("BRAZIL") || paisUpper.equals("BRASIL")) {
            return "ICMS";
        }
        
        // Por defecto, IVA para países hispanohablantes y europeos
        return "IVA";
    }

    /**
     * Verifica si un país tiene configurada una tasa de impuesto.
     */
    public boolean tieneTasaConfigurada(String pais) {
        if (pais == null || pais.isBlank()) return false;
        return TASAS_IVA.containsKey(pais.toUpperCase().trim());
    }

    /**
     * Obtiene todas las tasas de impuesto configuradas.
     */
    public Map<String, BigDecimal> obtenerTodasLasTasas() {
        return Map.copyOf(TASAS_IVA);
    }

    /**
     * Record con información del impuesto.
     */
    public record ImpuestoInfo(String pais, BigDecimal tasa, String nombreImpuesto) {}
}
