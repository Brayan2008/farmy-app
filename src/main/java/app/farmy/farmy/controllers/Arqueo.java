package app.farmy.farmy.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/arqueo")
public class Arqueo {

    @GetMapping
    public String arqueo(Model model) {

        model.addAttribute("estado_caja_usuario", false); // Cambia esto según la lógica de tu aplicación

        return "home/caja/apertura_cierre_caja";
    }

}
