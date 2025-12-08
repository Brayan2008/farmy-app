package app.farmy.farmy.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    // Entorno 1: SuperAdmin
    @GetMapping("/superadmin/dashboard")
    public String superAdminDashboard(HttpSession session) {
        return "superadmin/dashboard";
    }

    // Entorno 2: Gestión Farmacia
    @GetMapping("/home")
    public String gestionDashboard() {
        return "/home/dashboard";
    }

    // Entorno 3: Tienda Cliente
    @GetMapping("/tienda/home")
    public String tiendaHome() {
        return "tienda/home";
    }

    // Redirección raíz
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/superadmin/usuarios")
    public String superAdminUsuarios() {
        return "superadmin/usuarios"; // Apunta al archivo HTML que crearemos
    }
}