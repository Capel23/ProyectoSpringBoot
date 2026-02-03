package com.example.ProyectoSpringBoot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// Controlador principal para las vistas de la aplicacion
@Controller
public class HomeController {

    // Pagina principal - Dashboard
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("activePage", "inicio");
        return "index";
    }

    // Vista de planes
    @GetMapping("/planes")
    public String planes(Model model) {
        model.addAttribute("activePage", "planes");
        return "planes";
    }

    // Vista de usuarios
    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        model.addAttribute("activePage", "usuarios");
        return "usuarios";
    }

    // Vista de suscripciones
    @GetMapping("/suscripciones")
    public String suscripciones(Model model) {
        model.addAttribute("activePage", "suscripciones");
        return "suscripciones";
    }

    // Vista de facturas
    @GetMapping("/facturas")
    public String facturas(Model model) {
        model.addAttribute("activePage", "facturas");
        return "facturas";
    }
}
