package app.farmy.farmy.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("/home")
public class HomeController {
    
    @GetMapping()
    public String goHome() {
        return "/home/dashboard";
    }
    
    @GetMapping("/{modulo}")
    public String goUsers(@PathVariable String modulo) {
        return "/home/" + modulo;
    }    
}
