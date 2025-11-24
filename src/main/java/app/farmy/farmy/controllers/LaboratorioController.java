package app.farmy.farmy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import app.farmy.farmy.model.Laboratorio;
import app.farmy.farmy.repository.LaboratorioRepository;

@Controller
@RequestMapping("laboratorios")
public class LaboratorioController {

    @Autowired
    private LaboratorioRepository laboratorioRepo;

    private static final String RUC_INVALIDO = "RUC inválido. Debe tener 11 dígitos.";

    @GetMapping
    public String getLaboratorios(Model model) {
        model.addAttribute("listaLaboratorios", laboratorioRepo.findAll());
        model.addAttribute("laboratorio", new Laboratorio());
        return "/home/laboratorios/laboratorios";
    }

    @PostMapping("/save")
    public String saveLaboratorio(@NonNull @ModelAttribute Laboratorio laboratorio) {
        if (!validarRuc(laboratorio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, RUC_INVALIDO);
        }

        var savedLaboratorio = laboratorioRepo.save(laboratorio);
        if (savedLaboratorio.getCodigo() == null || savedLaboratorio.getCodigo().trim().isEmpty()) {
            savedLaboratorio.setCodigo("LAB" + String.format("%03d", savedLaboratorio.getIdLaboratorio()));
            laboratorioRepo.save(savedLaboratorio);
        }
        return "redirect:/laboratorios";
    }

    @GetMapping("/editar/{id}")
    public String editarLaboratorio(@PathVariable int id, Model model) {
        var opt = laboratorioRepo.findById(id);
        if (opt.isPresent()) {
            model.addAttribute("laboratorioEditar", opt.get());
            model.addAttribute("idLaboratorio", id);
            return "home/laboratorios/laboratorios_editar";
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/editar/{id}")
    public String editarLaboratorio(@PathVariable int id, @NonNull @ModelAttribute Laboratorio laboratorioEditar) {
        var exists = laboratorioRepo.findById(id).orElseThrow();
        if (!validarRuc(laboratorioEditar)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, RUC_INVALIDO);
        }

        exists.setNombre(laboratorioEditar.getNombre());
        exists.setRuc(laboratorioEditar.getRuc());
        exists.setTelefono(laboratorioEditar.getTelefono());
        exists.setEmail(laboratorioEditar.getEmail());
        exists.setDireccion(laboratorioEditar.getDireccion());
        exists.setEstado(laboratorioEditar.getEstado());

        laboratorioRepo.save(exists);
        return "redirect:/laboratorios";
    }

    private boolean validarRuc(Laboratorio laboratorio) {
        if (laboratorio == null || laboratorio.getRuc() == null) {
            return false;
        }

        String ruc = laboratorio.getRuc().trim();
        return ruc.length() == 11 && ruc.matches("[0-9]+");
    }
}


