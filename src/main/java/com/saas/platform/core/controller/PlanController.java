package com.saas.platform.core.controller;

import com.saas.platform.core.model.entity.Plan;
import com.saas.platform.core.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/planes")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("planes", planService.listarTodos());
        return "planes/lista";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("plan", new Plan());
        return "planes/formulario";
    }

    @PostMapping
    public String guardar(@ModelAttribute Plan plan, RedirectAttributes redirectAttributes) {
        planService.crear(plan);
        redirectAttributes.addFlashAttribute("mensaje", "Plan creado correctamente");
        return "redirect:/planes";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, Model model) {
        Plan plan = planService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));
        model.addAttribute("plan", plan);
        return "planes/formulario";
    }

    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute Plan plan, RedirectAttributes redirectAttributes) {
        planService.actualizar(plan);
        redirectAttributes.addFlashAttribute("mensaje", "Plan actualizado correctamente");
        return "redirect:/planes";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        planService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Plan eliminado correctamente");
        return "redirect:/planes";
    }
}
