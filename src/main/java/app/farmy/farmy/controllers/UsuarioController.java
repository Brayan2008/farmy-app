package app.farmy.farmy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import app.farmy.farmy.model.Usuario;
import app.farmy.farmy.model.Usuario.SuperRol;
import app.farmy.farmy.repository.RolRepository;
import app.farmy.farmy.repository.UsuarioRepository;
import app.farmy.farmy.security.FarmySesion;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("usuarios")
public class UsuarioController implements FarmySesion{

    @Autowired
    private UsuarioRepository user;

    @Autowired
    private RolRepository rol;

    @GetMapping
    public String getUsuarios(Model model, HttpSession session) {
        model.addAttribute("listaUsers", user.findByFarmacia(getFarmaciaActual(session)));
        model.addAttribute("roles", rol.findAll());
        model.addAttribute("usuario", new Usuario());
        return "/home/usuarios/usuarios";
    }

    @PostMapping("/save")
    public String postUsuario(@ModelAttribute Usuario usuario, HttpSession session) {
        usuario.setSuperRol(SuperRol.ADMIN_FARMACIA);
        usuario.setFarmacia(getFarmaciaActual(session));
        user.save(usuario);
        return "redirect:/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable int id, Model model) {
        var uOpt = user.findById(id);

        if (uOpt.isPresent()) {
            model.addAttribute("usuarioEditar", uOpt.get());
            model.addAttribute("roles", rol.findAll());
            model.addAttribute("idUsuario", id);
            return "home/usuarios/usuarios_editar";
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/editar/{id}")
    public String editarUsuario(@PathVariable int id, @ModelAttribute Usuario usuarioEditar) {
        var ufetch = user.findById(id).orElseThrow();

        ufetch.setNombreUsuario(usuarioEditar.getNombreUsuario());
        ufetch.setNombreCompleto(usuarioEditar.getNombreCompleto());
        ufetch.setTelefono(usuarioEditar.getTelefono());

        ufetch.setSuperRol(usuarioEditar.getSuperRol());
        
        ufetch.setEstado(usuarioEditar.getEstado());

        user.save(ufetch);
        return "redirect:/usuarios";
    }

}
