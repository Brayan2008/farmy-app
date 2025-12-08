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

import app.farmy.farmy.model.Categoria;
import app.farmy.farmy.repository.CategoriaRepository;
import app.farmy.farmy.security.FarmySesion;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/categorias")
public class CategoriaController implements FarmySesion{

    @Autowired
    private CategoriaRepository categoriaRepo;

    private final String mensajeNombreInvalido =
            "El nombre de la categoría debe tener entre 3 y 20 caracteres.";

    @GetMapping
    public String listarCategorias(Model model, HttpSession session) {
        model.addAttribute("listaCategorias", categoriaRepo.findByFarmacia(getFarmaciaActual(session)));
        model.addAttribute("categoria", new Categoria());
        return "/home/categorias/categorias"; 
    }

    @PostMapping("/save")
    public String guardarCategoria(@ModelAttribute Categoria categoria, HttpSession session) {
        if (!validarCategoria(categoria)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensajeNombreInvalido);
        }
        categoria.setFarmacia(getFarmaciaActual(session));
        categoriaRepo.save(categoria);
        return "redirect:/categorias";
    }

    @GetMapping("/editar/{id}")
    public String editarCategoria(@PathVariable int id, Model model) {
        var opt = categoriaRepo.findById(id);
        if (opt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAttribute("categoriaEditar", opt.get());
        model.addAttribute("idCategoria", id);
        return "/home/categorias/categoria_editar";
    }

    @PostMapping("/editar/{id}")
    public String actualizarCategoria(
            @PathVariable int id,
            @ModelAttribute Categoria categoriaEditar) {

        var exists = categoriaRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!validarCategoria(categoriaEditar)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensajeNombreInvalido);
        }
        exists.setNombreCategoria(categoriaEditar.getNombreCategoria());
        exists.setDescripcion(categoriaEditar.getDescripcion());
        exists.setEstado(categoriaEditar.getEstado());

        categoriaRepo.save(exists);
        return "redirect:/categorias";
    }

    /** Validaciones */
    private boolean validarCategoria(Categoria c) {
        if (c == null || c.getNombreCategoria() == null) return false;

        String nombre = c.getNombreCategoria().trim();
        return nombre.length() >= 3 && nombre.length() <= 20;
    }



}
