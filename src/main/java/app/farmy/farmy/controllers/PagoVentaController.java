package app.farmy.farmy.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.farmy.farmy.model.EstadoPago;
import app.farmy.farmy.model.VentaPago;
import app.farmy.farmy.model.Ventas;
import app.farmy.farmy.repository.VentaPagoRepository;
import app.farmy.farmy.repository.VentasRepository;

@Controller
@RequestMapping("/pagos/venta")
public class PagoVentaController {

    @Autowired
    private VentaPagoRepository ventaPagoRepository;

    @Autowired
    private VentasRepository ventasRepository;

    @PostMapping("/guardar")
    public String guardarPago(@ModelAttribute VentaPago ventaPago, RedirectAttributes redirectAttributes) {
        try {
            Ventas venta = ventasRepository.findById(ventaPago.getVenta().getIdVenta()).orElse(null);
            if (venta == null) {
                redirectAttributes.addFlashAttribute("error", "Venta no encontrada");
                return "redirect:/ventas";
            }
            
            ventaPago.setFechaPago(LocalDateTime.now());
            
            ventaPagoRepository.save(ventaPago);
            
            // Update Venta saldo
            BigDecimal nuevoSaldo = venta.getSaldoPendiente().subtract(ventaPago.getMonto());
            if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) nuevoSaldo = BigDecimal.ZERO;
            venta.setSaldoPendiente(nuevoSaldo);
            
            if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
                venta.setEstadoPago(EstadoPago.PAGADO);
            }
            
            ventasRepository.save(venta);

            redirectAttributes.addFlashAttribute("success", "Pago registrado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar pago: " + e.getMessage());
        }
        return "redirect:/ventas";
    }
}
