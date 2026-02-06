package com.saas.platform.core.controller;

import com.saas.platform.core.model.entity.Plan;
import com.saas.platform.core.model.entity.Usuario;
import com.saas.platform.core.service.PlanService;
import com.saas.platform.core.service.SuscripcionService;
import com.saas.platform.core.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PlanService planService;
    private final SuscripcionService suscripcionService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping("/registro")
    public String formularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("planes", planService.listarActivos());
        return "usuarios/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute Usuario usuario,
                            BindingResult bindingResult,
                            @RequestParam Long planId,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("planes", planService.listarActivos());
            return "usuarios/registro";
        }
        try {
            Usuario guardado = usuarioService.registrar(usuario);

            Plan plan = planService.buscarPorId(planId)
                    .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));

            suscripcionService.crear(guardado, plan);

            redirectAttributes.addFlashAttribute("mensaje",
                    "Usuario registrado con plan " + plan.getNombre());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/usuarios/registro";
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);

        suscripcionService.buscarPorUsuarioId(id).ifPresent(s -> {
            model.addAttribute("suscripcion", s);
        });

        return "usuarios/detalle";
    }
}
