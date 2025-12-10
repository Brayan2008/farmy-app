package app.farmy.farmy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.farmy.farmy.model.Caja;
import app.farmy.farmy.model.TipoMovimientoEnum;
import app.farmy.farmy.repository.CajaRepository;
import app.farmy.farmy.security.FarmySesion;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/arqueo")
public class Arqueo implements FarmySesion {

    public static final String SESSION_CAJA_ABIERTA = "estadoCajaAbierta";
    public static final String SESSION_MONTO_APERTURA = "montoAperturaCaja";

    @Autowired
    private CajaRepository cajaRepo;

    @GetMapping
    public String arqueo(Model model, HttpSession session) {
        boolean cajaAbierta = Boolean.TRUE.equals(session.getAttribute(SESSION_CAJA_ABIERTA));
        model.addAttribute("estado_caja_usuario", cajaAbierta);
        model.addAttribute("monto_apertura", session.getAttribute(SESSION_MONTO_APERTURA));
        return "home/caja/apertura_cierre_caja";
    }

    @PostMapping("/apertura")
    public String aperturarCaja(@RequestParam("monto") double monto,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (monto <= 0) {
            redirectAttributes.addFlashAttribute("mensajeError", "Ingrese un monto inicial mayor a cero.");
            return "redirect:/arqueo";
        }
        if (Boolean.TRUE.equals(session.getAttribute(SESSION_CAJA_ABIERTA))) {
            redirectAttributes.addFlashAttribute("mensajeError", "La caja ya está aperturada.");
            return "redirect:/arqueo";
        }

        // Registrar movimiento de apertura en la tabla de caja
        Caja apertura = new Caja();
        apertura.setTipo(TipoMovimientoEnum.Ingreso);
        apertura.setDescripcion("Apertura de caja");
        apertura.setMonto(monto);
        apertura.setCategoria("Arqueo");
        var usuario = getUsuarioActual(session);
        if (usuario != null) {
            apertura.setResponsable(usuario.getNombreCompleto());
            apertura.setFarmacia(usuario.getFarmacia());
        }

        apertura = cajaRepo.save(apertura);
        if (apertura.getCodigo() == null || apertura.getCodigo().isEmpty()) {
            apertura.setCodigo("CAJ" + String.format("%03d", apertura.getIdCaja()));
            cajaRepo.save(apertura);
        }

        session.setAttribute(SESSION_CAJA_ABIERTA, true);
        session.setAttribute(SESSION_MONTO_APERTURA, monto);
        redirectAttributes.addFlashAttribute("mensajeExito", "Caja aperturada correctamente.");
        return "redirect:/ventas";
    }

    @PostMapping("/cierre")
    public String cerrarCaja(@RequestParam("monto") double monto,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!Boolean.TRUE.equals(session.getAttribute(SESSION_CAJA_ABIERTA))) {
            redirectAttributes.addFlashAttribute("mensajeError", "No hay una caja abierta para cerrar.");
            return "redirect:/arqueo";
        }
        if (monto < 0) {
            redirectAttributes.addFlashAttribute("mensajeError", "Ingrese un monto válido para el cierre.");
            return "redirect:/arqueo";
        }

        // Registrar movimiento de cierre
        Caja cierre = new Caja();
        cierre.setTipo(TipoMovimientoEnum.Egreso);
        cierre.setDescripcion("Cierre de caja");
        cierre.setMonto(monto);
        cierre.setCategoria("Arqueo");
        var usuario = getUsuarioActual(session);
        if (usuario != null) {
            cierre.setResponsable(usuario.getNombreCompleto());
            cierre.setFarmacia(usuario.getFarmacia());
        }

        cierre = cajaRepo.save(cierre);
        if (cierre.getCodigo() == null || cierre.getCodigo().isEmpty()) {
            cierre.setCodigo("CAJ" + String.format("%03d", cierre.getIdCaja()));
            cajaRepo.save(cierre);
        }

        session.setAttribute(SESSION_CAJA_ABIERTA, false);
        session.removeAttribute(SESSION_MONTO_APERTURA);
        redirectAttributes.addFlashAttribute("mensajeExito", "Caja cerrada correctamente.");
        return "redirect:/ventas";
    }

}
