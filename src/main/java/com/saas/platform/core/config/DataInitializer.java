package com.saas.platform.core.config;

import com.saas.platform.core.model.entity.Plan;
import com.saas.platform.core.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PlanRepository planRepository;

    @Override
    public void run(String... args) {
        if (planRepository.count() == 0) {
            planRepository.save(Plan.builder()
                    .nombre("Basic")
                    .descripcion("Plan básico con funcionalidades esenciales")
                    .precioMensual(new BigDecimal("9.99"))
                    .maxUsuarios(5)
                    .activo(true)
                    .build());

            planRepository.save(Plan.builder()
                    .nombre("Premium")
                    .descripcion("Plan premium con funcionalidades avanzadas")
                    .precioMensual(new BigDecimal("29.99"))
                    .maxUsuarios(25)
                    .activo(true)
                    .build());

            planRepository.save(Plan.builder()
                    .nombre("Enterprise")
                    .descripcion("Plan empresarial con todas las funcionalidades")
                    .precioMensual(new BigDecimal("99.99"))
                    .maxUsuarios(100)
                    .activo(true)
                    .build());

            log.info("Planes iniciales creados: Basic, Premium, Enterprise");
        }
    }
}
