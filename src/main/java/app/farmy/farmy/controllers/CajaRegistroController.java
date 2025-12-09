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

import app.farmy.farmy.model.CajaRegistro;
import app.farmy.farmy.repository.CajaRegistroRepository;
import app.farmy.farmy.repository.UsuarioRepository;
import app.farmy.farmy.security.FarmySesion;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("cajas-registro")
public class CajaRegistroController implements FarmySesion {

    @Autowired
    private CajaRegistroRepository cajaRegistroRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @GetMapping
    public String getCajasRegistro(Model model, HttpSession session) {
        var farmacia = getFarmaciaActual(session);
        model.addAttribute("listaCajas", cajaRegistroRepo.findByFarmacia(farmacia));
        model.addAttribute("cajaRegistro", new CajaRegistro());
        model.addAttribute("usuarios", usuarioRepo.findByFarmacia(farmacia));
        return "/home/cajas-registro/cajas-registro";
    }

    @PostMapping("/save")
    public String saveCajaRegistro(@ModelAttribute CajaRegistro cajaRegistro,
                                   @RequestParam(required = false) Integer usuarioId,
                                   HttpSession session) {
        var farmacia = getFarmaciaActual(session);
        cajaRegistro.setFarmacia(farmacia);

        // Asignar usuario si se proporciona
        if (usuarioId != null && usuarioId > 0) {
            usuarioRepo.findById(usuarioId).ifPresent(cajaRegistro::setUsuario);
        }

        // Generar código temporal si no existe (necesario porque la BD requiere NOT NULL)
        // Se generará el código definitivo después de guardar cuando tengamos el ID
        boolean codigoTemporal = false;
        if (cajaRegistro.getCodigo() == null || cajaRegistro.getCodigo().trim().isEmpty()) {
            // Generar código temporal basado en timestamp para evitar conflictos
            cajaRegistro.setCodigo("TMP" + System.currentTimeMillis());
            codigoTemporal = true;
        }

        // Guardar para obtener el ID
        cajaRegistro = cajaRegistroRepo.save(cajaRegistro);

        // Generar código definitivo después de guardar (cuando ya tenemos el ID)
        if (codigoTemporal || cajaRegistro.getCodigo().startsWith("TMP")) {
            cajaRegistro.setCodigo("CR" + String.format("%03d", cajaRegistro.getIdCajaRegistro()));
            cajaRegistroRepo.save(cajaRegistro);
        }

        return "redirect:/cajas-registro";
    }

    @GetMapping("/editar/{id}")
    public String editarCajaRegistro(@PathVariable int id, Model model, HttpSession session) {
        var opt = cajaRegistroRepo.findById(id);
        if (opt.isPresent()) {
            var farmacia = getFarmaciaActual(session);
            model.addAttribute("cajaRegistroEditar", opt.get());
            model.addAttribute("usuarios", usuarioRepo.findByFarmacia(farmacia));
            model.addAttribute("idCajaRegistro", id);
            return "home/cajas-registro/cajas-registro_editar";
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/editar/{id}")
    public String editarCajaRegistro(@PathVariable int id,
                                    @ModelAttribute CajaRegistro cajaRegistroEditar,
                                    @RequestParam(required = false) Integer usuarioId,
                                    HttpSession session) {
        var exists = cajaRegistroRepo.findById(id).orElseThrow();

        // Actualizar campos permitidos
        exists.setNombre(cajaRegistroEditar.getNombre());
        exists.setDescripcion(cajaRegistroEditar.getDescripcion());
        exists.setEstado(cajaRegistroEditar.getEstado());

        // Manejar asignación de usuario
        if (usuarioId != null && usuarioId > 0) {
            usuarioRepo.findById(usuarioId).ifPresent(exists::setUsuario);
        } else {
            exists.setUsuario(null); // Desasignar usuario
        }

        cajaRegistroRepo.save(exists);
        return "redirect:/cajas-registro";
    }

    @PostMapping("/deshabilitar/{id}")
    public String deshabilitarCajaRegistro(@PathVariable int id) {
        var caja = cajaRegistroRepo.findById(id).orElseThrow();
        caja.setEstado("Inactivo");
        caja.setUsuario(null); // Desasignar usuario al deshabilitar
        cajaRegistroRepo.save(caja);
        return "redirect:/cajas-registro";
    }

    @PostMapping("/habilitar/{id}")
    public String habilitarCajaRegistro(@PathVariable int id) {
        var caja = cajaRegistroRepo.findById(id).orElseThrow();
        caja.setEstado("Activo");
        cajaRegistroRepo.save(caja);
        return "redirect:/cajas-registro";
    }
}
