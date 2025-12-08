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
import org.springframework.web.server.ResponseStatusException;

import app.farmy.farmy.model.Proveedor;
import app.farmy.farmy.repository.ProveedorRepository;
import app.farmy.farmy.security.FarmySesion;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("proveedores")
public class ProveedorController implements FarmySesion{

    @Autowired
    private ProveedorRepository proveedorRepo;

    private String rucInvalidoMensage = "RUC inválido. Debe tener 11 dígitos.";

    @GetMapping
    public String getProveedores(Model model, HttpSession session) {
        model.addAttribute("listaProveedores", proveedorRepo.findByFarmacia(getFarmaciaActual(session)));
        model.addAttribute("proveedor", new Proveedor());
        return "/home/proveedores/proveedores";
    }

    @PostMapping("/save")
    public String saveProveedor(@ModelAttribute Proveedor proveedor, HttpSession session) {
        // validar longitud de RUC antes de guardar
        if (!validarRUC(proveedor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, rucInvalidoMensage);
        }

        proveedor.setFarmacia(getFarmaciaActual(session));
        proveedor = proveedorRepo.save(proveedor);
        return "redirect:/proveedores";
    }

    @GetMapping("/editar/{id}")
    public String editarProveedor(@PathVariable int id, Model model) {
        var opt = proveedorRepo.findById(id);
        if (opt.isPresent()) {
            model.addAttribute("proveedorEditar", opt.get());
            model.addAttribute("idProveedor", id);
            return "home/proveedores/proveedores_editar";
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/editar/{id}")
    public String editarProveedor(@PathVariable int id, @ModelAttribute Proveedor proveedorEditar) {
        var exists = proveedorRepo.findById(id).orElseThrow();
        // validar longitud de RUC antes de actualizar
        if (!validarRUC(proveedorEditar)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, rucInvalidoMensage);
        }

        // Actualizar campos permitidos; preservar fechaRegistro y codigo
        exists.setRazonSocial(proveedorEditar.getRazonSocial());
        exists.setRuc(proveedorEditar.getRuc());
        exists.setTelefono(proveedorEditar.getTelefono());
        exists.setEmail(proveedorEditar.getEmail());
        exists.setTipo(proveedorEditar.getTipo());
        exists.setDireccion(proveedorEditar.getDireccion());
        exists.setEstado(proveedorEditar.getEstado());

        proveedorRepo.save(exists);
        return "redirect:/proveedores";
    }

    /**
     * Valida que el RUC tenga exactamente 11 dígitos.
     */
    private boolean validarRUC(Proveedor p) {
        if (p == null || p.getRuc() == null) {
            return false;
        }

        String ruc = p.getRuc().trim();
        return ruc.length() == 11 && ruc.matches("[0-9]+");
    }
}

