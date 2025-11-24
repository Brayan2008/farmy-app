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

import app.farmy.farmy.model.MetodoPago;
import app.farmy.farmy.repository.MetodoPagoRepository;

@Controller
@RequestMapping("metodos-pago")
public class MetodoPagoController {

    @Autowired
    private MetodoPagoRepository metodoRepo;

    @GetMapping
    public String getMetodos(Model model) {
        model.addAttribute("listaMetodos", metodoRepo.findAll());
        model.addAttribute("metodoPago", new MetodoPago());
        return "/home/metodos/metodos";
    }

    @PostMapping("/save")
    public String saveMetodo(@ModelAttribute MetodoPago metodo) {
        metodoRepo.save(metodo);
        return "redirect:/metodos-pago";
    }

    @GetMapping("/editar/{id}")
    public String editarMetodo(@PathVariable int id, Model model) {
        var opt = metodoRepo.findById(id);
        if (opt.isPresent()) {
            model.addAttribute("metodoEditar", opt.get());
            model.addAttribute("idMetodo", id);
            return "home/metodos/metodos_editar";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/editar/{id}")
    public String editarMetodo(@PathVariable int id, @ModelAttribute MetodoPago metodoEditar) {
        var exists = metodoRepo.findById(id).orElseThrow();
        exists.setNombreMetodoPago(metodoEditar.getNombreMetodoPago());
        exists.setDescripcion(metodoEditar.getDescripcion());
        exists.setEstado(metodoEditar.getEstado());
        metodoRepo.save(exists);
        return "redirect:/metodos-pago";
    }

}
