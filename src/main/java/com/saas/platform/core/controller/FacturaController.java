package com.saas.platform.core.controller;

import com.saas.platform.core.service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("facturas", facturaService.listarTodas());
        return "facturas/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("factura", facturaService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada")));
        return "facturas/detalle";
    }
}
