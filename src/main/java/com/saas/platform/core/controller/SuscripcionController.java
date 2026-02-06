package com.saas.platform.core.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.saas.platform.core.model.entity.Plan;
import com.saas.platform.core.model.entity.Suscripcion;
import com.saas.platform.core.service.FacturaService;
import com.saas.platform.core.service.PlanService;
import com.saas.platform.core.service.SuscripcionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/suscripciones")
@RequiredArgsConstructor
public class SuscripcionController {

    private final SuscripcionService suscripcionService;
    private final PlanService planService;
    private final FacturaService facturaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("suscripciones", suscripcionService.listarTodas());
        return "suscripciones/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Suscripcion suscripcion = suscripcionService.buscarPorId(id)
                .orElse(null);
        if (suscripcion == null) {
            return "redirect:/suscripciones";
        }
        model.addAttribute("suscripcion", suscripcion);
        model.addAttribute("facturas", facturaService.listarPorSuscripcion(suscripcion.getId()));
        model.addAttribute("planes", planService.listarActivos());
        return "suscripciones/detalle";
    }

    @PostMapping("/{id}/cambiar-plan")
    public String cambiarPlan(@PathVariable Long id,
                            @RequestParam Long nuevoPlanId,
                            RedirectAttributes redirectAttributes) {
        try {
            Plan nuevoPlan = planService.buscarPorId(nuevoPlanId)
                    .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));

            suscripcionService.cambiarPlan(id, nuevoPlan);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Plan cambiado a " + nuevoPlan.getNombre());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/suscripciones/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        suscripcionService.cancelar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Suscripción cancelada");
        return "redirect:/suscripciones";
    }

    @PostMapping("/generar-facturas")
    public String generarFacturas(RedirectAttributes redirectAttributes) {
        var facturas = suscripcionService.generarFacturasAutomaticas();
        redirectAttributes.addFlashAttribute("mensaje",
                "Se generaron " + facturas.size() + " facturas automáticas");
        return "redirect:/suscripciones";
    }
}
