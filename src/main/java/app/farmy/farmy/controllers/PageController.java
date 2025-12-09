package app.farmy.farmy.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;
import app.farmy.farmy.model.Lote;
import app.farmy.farmy.repository.LoteRepository;
import app.farmy.farmy.repository.FarmaciaRepository;
import app.farmy.farmy.model.Farmacia;

@Controller
public class PageController {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private FarmaciaRepository farmaciaRepository;

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
    @GetMapping("/tienda/{idFarmacia}/home")
    public String tiendaHome(@PathVariable Long idFarmacia, Model model) {
        // Fetch products with stock > 0 and belonging to the specific pharmacy
        List<Lote> productosDisponibles = loteRepository.findAll().stream()
                .filter(l -> l.getCantidadActual() > 0)
                .filter(l -> l.getProducto().getFarmacia() != null && l.getProducto().getFarmacia().getId().equals(idFarmacia))
                .collect(Collectors.toList());
        
        Farmacia farmacia = farmaciaRepository.findById(idFarmacia).orElse(null);
        model.addAttribute("farmacia", farmacia);

        model.addAttribute("productos", productosDisponibles);
        model.addAttribute("idFarmacia", idFarmacia);
        return "web/client-web";
    }

    @GetMapping("/tienda/{idFarmacia}/checkout")
    public String tiendaCheckout(@PathVariable Long idFarmacia, Model model) {
        Farmacia farmacia = farmaciaRepository.findById(idFarmacia).orElse(null);
        model.addAttribute("farmacia", farmacia);
        model.addAttribute("idFarmacia", idFarmacia);
        return "web/tu";
    }

    @GetMapping("/tienda/{idFarmacia}/producto/{idLote}")
    public String productoDetalle(@PathVariable Long idFarmacia, @PathVariable Integer idLote, Model model) {
        Lote lote = loteRepository.findById(idLote).orElse(null);
        if (lote == null) {
            return "redirect:/tienda/" + idFarmacia + "/home";
        }
        Farmacia farmacia = farmaciaRepository.findById(idFarmacia).orElse(null);
        model.addAttribute("farmacia", farmacia);

        model.addAttribute("producto", lote);
        model.addAttribute("idFarmacia", idFarmacia);
        return "web/product-detail";
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