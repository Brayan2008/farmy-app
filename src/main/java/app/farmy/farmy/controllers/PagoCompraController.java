package app.farmy.farmy.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.farmy.farmy.model.Compra;
import app.farmy.farmy.model.EstadoPago;
import app.farmy.farmy.model.PagoCompra;
import app.farmy.farmy.repository.CompraRepository;
import app.farmy.farmy.repository.PagoCompraRepository;

@Controller
@RequestMapping("/pagos")
public class PagoCompraController {

    @Autowired
    private PagoCompraRepository pagoCompraRepository;

    @Autowired
    private CompraRepository compraRepository;

    @PostMapping("/guardar")
    public String guardarPago(@ModelAttribute PagoCompra pagoCompra, RedirectAttributes redirectAttributes) {
        try {
            Compra compra = compraRepository.findById(pagoCompra.getCompra().getNumeroFactura()).orElse(null);
            if (compra == null) {
                redirectAttributes.addFlashAttribute("error", "Compra no encontrada");
                return "redirect:/compras";
            }
            
            pagoCompra.setFechaPago(LocalDateTime.now());
            // Assuming user is handled or passed. For now, maybe null or handled if in form.
            
            pagoCompraRepository.save(pagoCompra);
            
            // Update Compra saldo
            BigDecimal nuevoSaldo = compra.getSaldoPendiente().subtract(pagoCompra.getMontoPago());
            if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) nuevoSaldo = BigDecimal.ZERO;
            compra.setSaldoPendiente(nuevoSaldo);
            
            if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
                compra.setEstadoPago(EstadoPago.PAGADO);
            }
            
            compraRepository.save(compra);

            redirectAttributes.addFlashAttribute("success", "Pago registrado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar pago: " + e.getMessage());
        }
        return "redirect:/compras";
    }
}
