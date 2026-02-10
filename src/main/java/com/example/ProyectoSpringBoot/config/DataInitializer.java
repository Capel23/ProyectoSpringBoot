package com.example.ProyectoSpringBoot.config;

import com.example.ProyectoSpringBoot.entity.Perfil;
import com.example.ProyectoSpringBoot.entity.Plan;
import com.example.ProyectoSpringBoot.entity.Usuario;
import com.example.ProyectoSpringBoot.enums.RolUsuario;
import com.example.ProyectoSpringBoot.enums.TipoPlan;
import com.example.ProyectoSpringBoot.repository.PlanRepository;
import com.example.ProyectoSpringBoot.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(PlanRepository planRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Solo crear datos si no existen
            if (planRepository.count() == 0) {
                // Plan BASIC
                Plan basic = new Plan();
                basic.setNombre("Plan Básico");
                basic.setTipoPlan(TipoPlan.BASIC);
                basic.setPrecioMensual(new BigDecimal("9.99"));
                basic.setAlmacenamientoGb(10);
                basic.setMaxUsuarios(5);
                basic.setDescripcion("Plan básico ideal para empezar");
                basic.setSoportePrioritario(false);
                basic.setActivo(true);
                planRepository.save(basic);

                // Plan PREMIUM
                Plan premium = new Plan();
                premium.setNombre("Plan Premium");
                premium.setTipoPlan(TipoPlan.PREMIUM);
                premium.setPrecioMensual(new BigDecimal("29.99"));
                premium.setAlmacenamientoGb(50);
                premium.setMaxUsuarios(20);
                premium.setDescripcion("Plan premium con más capacidad y soporte prioritario");
                premium.setSoportePrioritario(true);
                premium.setActivo(true);
                planRepository.save(premium);

                // Plan ENTERPRISE
                Plan enterprise = new Plan();
                enterprise.setNombre("Plan Empresarial");
                enterprise.setTipoPlan(TipoPlan.ENTERPRISE);
                enterprise.setPrecioMensual(new BigDecimal("99.99"));
                enterprise.setAlmacenamientoGb(200);
                enterprise.setMaxUsuarios(100);
                enterprise.setDescripcion("Plan empresarial con capacidad ilimitada y soporte 24/7");
                enterprise.setSoportePrioritario(true);
                enterprise.setActivo(true);
                planRepository.save(enterprise);

                System.out.println("✓ Datos de prueba cargados: 3 planes creados");
            }

            // Crear usuario admin si no existe
            if (!usuarioRepository.existsByEmail("admin@saas.com")) {
                Usuario admin = Usuario.builder()
                        .email("admin@saas.com")
                        .password(passwordEncoder.encode("admin123"))
                        .activo(true)
                        .emailVerificado(true)
                        .rol(RolUsuario.ADMIN)
                        .build();
                
                Perfil perfilAdmin = Perfil.builder()
                        .nombre("Administrador")
                        .apellidos("Sistema")
                        .pais("ES")
                        .usuario(admin)
                        .build();
                admin.setPerfil(perfilAdmin);
                
                usuarioRepository.save(admin);
                System.out.println("✓ Usuario admin creado: admin@saas.com / admin123");
            }
        };
    }
}
