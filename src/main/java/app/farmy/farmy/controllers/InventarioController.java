package app.farmy.farmy.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.farmy.farmy.model.InventarioMovimiento;
import app.farmy.farmy.model.InventarioMovimiento.TipoMovimiento;
import app.farmy.farmy.model.Lote;
import app.farmy.farmy.model.Usuario;
import app.farmy.farmy.repository.InventarioMovimientoRepository;
import app.farmy.farmy.repository.LoteRepository;
import app.farmy.farmy.repository.UsuarioRepository;
import app.farmy.farmy.security.FarmySesion;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/inventario")
public class InventarioController implements FarmySesion {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private InventarioMovimientoRepository inventarioMovimientoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String listarInventario(Model model, HttpSession session) {
        List<Lote> lotes = loteRepository.findAll().stream().filter(arg0 -> arg0.getProducto().getFarmacia().getId() == getFarmaciaActual(session).getId()).toList();
        List<Usuario> usuarios = usuarioRepository.findByFarmacia(getFarmaciaActual(session));
        
        model.addAttribute("lotes", lotes);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("movimiento", new InventarioMovimiento());
        model.addAttribute("tiposMovimiento", TipoMovimiento.values());
        
        return "home/inventario/inventario";
    }

    @GetMapping("/movimientos")
    public String listarMovimientos(Model model, HttpSession session) {
        List<InventarioMovimiento> movimientos = inventarioMovimientoRepository.findAll().stream()
                .filter(mov -> mov.getUsuario().getFarmacia().getId() == getFarmaciaActual(session).getId())
                .toList();

                System.out.println(movimientos.size() + "\n".repeat(10));
        model.addAttribute("movimientos", movimientos);
        return "home/inventario/movimientos";
    }

    @PostMapping("/movimiento/guardar")
    public String guardarMovimiento(@ModelAttribute InventarioMovimiento movimiento, RedirectAttributes redirectAttributes, HttpSession session) {
        try {
            Lote lote = loteRepository.findById(movimiento.getLote().getIdLote()).orElse(null);
            if (lote == null) {
                redirectAttributes.addFlashAttribute("error", "Lote no encontrado");
                return "redirect:/inventario";
            }

            // Set stock anterior
            movimiento.setStockAnterior(lote.getCantidadActual());

            // Calculate new stock
            int nuevoStock = lote.getCantidadActual();
            if (movimiento.getTipoMovimiento() == TipoMovimiento.ENTRADA) {
                nuevoStock += movimiento.getCantidad();
            } else if (movimiento.getTipoMovimiento() == TipoMovimiento.SALIDA) {
                if (movimiento.getCantidad() > lote.getCantidadActual()) {
                    redirectAttributes.addFlashAttribute("error", "Stock insuficiente para realizar la salida");
                    return "redirect:/inventario";
                }
                nuevoStock -= movimiento.getCantidad();
            }

            movimiento.setStockNuevo(nuevoStock);
            movimiento.setFechaMovimiento(LocalDateTime.now());
            movimiento.setUsuario(getUsuarioActual(session));
            // Update Lote
            lote.setCantidadActual(nuevoStock);
            loteRepository.save(lote);

            // Save Movimiento
            inventarioMovimientoRepository.save(movimiento);

            redirectAttributes.addFlashAttribute("success", "Movimiento registrado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar movimiento: " + e.getMessage());
        }
        return "redirect:/inventario";
    }
}
