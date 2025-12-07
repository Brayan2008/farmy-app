package app.farmy.farmy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import app.farmy.farmy.model.Caja;
import app.farmy.farmy.repository.CajaRepository;
import app.farmy.farmy.repository.MetodoPagoRepository;

@Controller
@RequestMapping("caja")
public class CajaController {

    @Autowired
    private CajaRepository cajaRepo;

    @Autowired
    private MetodoPagoRepository metodoPagoRepo;

    @GetMapping
    public String getCajas(Model model) {
        model.addAttribute("listaCajas", cajaRepo.findAll());
        model.addAttribute("caja", new Caja());
        model.addAttribute("metodosPago", metodoPagoRepo.findAll());
        
        // Calcular resumen
        double totalIngresos = cajaRepo.findAll().stream()
            .filter(c -> c.getTipo() != null && c.getTipo().name().equals("Ingreso") 
                && c.getEstado() != null && !c.getEstado().name().equals("Anulado"))
            .mapToDouble(Caja::getMonto)
            .sum();
        
        double totalEgresos = cajaRepo.findAll().stream()
            .filter(c -> c.getTipo() != null && c.getTipo().name().equals("Egreso")
                && c.getEstado() != null && !c.getEstado().name().equals("Anulado"))
            .mapToDouble(Caja::getMonto)
            .sum();
        
        double saldoDia = totalIngresos - totalEgresos;
        
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("totalEgresos", totalEgresos);
        model.addAttribute("saldoDia", saldoDia);
        
        return "/home/caja";
    }

    @PostMapping("/save")
    public String saveCaja(@ModelAttribute Caja caja, 
                           @RequestParam(required = false) Integer metodoPagoId) {
        // Si viene un ID de método de pago, buscar y asignar
        if (metodoPagoId != null && metodoPagoId > 0) {
            metodoPagoRepo.findById(metodoPagoId).ifPresent(caja::setMetodoPago);
        }
        
        caja = cajaRepo.save(caja);
        // Generar código después de guardar (cuando ya tenemos el ID)
        if (caja.getCodigo() == null || caja.getCodigo().isEmpty()) {
            caja.setCodigo("CAJ" + String.format("%03d", caja.getIdCaja()));
            cajaRepo.save(caja);
        }
        return "redirect:/caja";
    }

    @GetMapping("/editar/{id}")
    public String editarCaja(@PathVariable int id, Model model) {
        var opt = cajaRepo.findById(id);
        if (opt.isPresent()) {
            model.addAttribute("cajaEditar", opt.get());
            model.addAttribute("metodosPago", metodoPagoRepo.findAll());
            model.addAttribute("idCaja", id);
            return "home/caja/caja_editar";
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/editar/{id}")
    public String editarCaja(@PathVariable int id, 
                            @ModelAttribute Caja cajaEditar,
                            @RequestParam(required = false) Integer metodoPagoId) {
        var exists = cajaRepo.findById(id).orElseThrow();

        // Actualizar campos permitidos
        exists.setTipo(cajaEditar.getTipo());
        exists.setDescripcion(cajaEditar.getDescripcion());
        exists.setMonto(cajaEditar.getMonto());
        exists.setFecha(cajaEditar.getFecha());
        exists.setHora(cajaEditar.getHora());
        exists.setEstado(cajaEditar.getEstado());
        exists.setCategoria(cajaEditar.getCategoria());
        
        // Manejar método de pago
        if (metodoPagoId != null && metodoPagoId > 0) {
            metodoPagoRepo.findById(metodoPagoId).ifPresent(exists::setMetodoPago);
        } else {
            exists.setMetodoPago(null);
        }
        
        exists.setResponsable(cajaEditar.getResponsable());
        exists.setNumeroComprobante(cajaEditar.getNumeroComprobante());
        exists.setReferencia(cajaEditar.getReferencia());

        cajaRepo.save(exists);
        return "redirect:/caja";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarCaja(@PathVariable int id) {
        var caja = cajaRepo.findById(id).orElseThrow();
        // En lugar de eliminar físicamente, marcamos como anulado
        caja.setEstado(app.farmy.farmy.model.EstadoMovimientoEnum.Anulado);
        cajaRepo.save(caja);
        return "redirect:/caja";
    }
}

