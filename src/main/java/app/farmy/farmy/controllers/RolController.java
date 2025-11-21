package app.farmy.farmy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import app.farmy.farmy.dto.RolDTO;
import app.farmy.farmy.model.ModulosEnum;
import app.farmy.farmy.model.Rol;
import app.farmy.farmy.repository.PermisosRepository;
import app.farmy.farmy.repository.RolRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("roles")
public class RolController {

    @Autowired
    RolRepository rol;

    @Autowired
    PermisosRepository permisos;

    @GetMapping
    public String getRoles(Model model) {
        model.addAttribute("permisos", permisos.findAll().stream()
                .map((a) -> a.getModulo())
                .toList());
        model.addAttribute("lista_roles", rol.findAll());
        model.addAttribute("rol", new RolDTO("", null, "activo", null));

        return "/home/roles/roles";
    }

    @PostMapping
    public String postMethodName(@ModelAttribute RolDTO rol) {
        var roldao = preRol(rol);
        this.rol.save(roldao);
        return "redirect:/roles";
    }

    @GetMapping("editar/{id}")
    public String editarRol(@PathVariable int id, Model model) {
        var rolfetch = rol.findById(id);

        if (rolfetch.isPresent()) {
            var rol = rolfetch.get();
            RolDTO roldto = new RolDTO(rol.getNombreRol(), rol.getDescripcion(), rol.getEstado(),
                    rol.getPermisos().stream().map(r -> r.getModulo().name()).toList());

            model.addAttribute("permisos", permisos.findAll().stream()
                    .map((a) -> a.getModulo())
                    .toList());
            model.addAttribute("idRol", id);
            model.addAttribute("rolEditar", roldto);
            return "home/roles/roles_editar";
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("editar/{id}")
    public String editarRol(@PathVariable int id, @ModelAttribute RolDTO rolEditar) {
        var roldao = preRol(rolEditar);
        var rolfetch = rol.findById(id).orElseThrow();

        roldao.setFechaCreación(rolfetch.getFechaCreación());
        roldao.setIdRol(id);
        
        rol.save(roldao);        
        
        return "redirect:/roles";
    }

    public Rol preRol(RolDTO rol) {
        Rol roldao = rol.toRol(rol);

        if (roldao.getPermisos() != null) {
            rol.permisos().forEach(r -> {
                permisos.findByModulo(ModulosEnum.valueOf(r))
                        .ifPresent(modulo -> roldao.getPermisos().add(modulo));
            });
        }

        return roldao;
    }

}