package app.farmy.farmy.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import app.farmy.farmy.model.Presentacion;
import app.farmy.farmy.repository.PresentacionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
@RequestMapping("productos")
public class PresentacionController {
    
    @Autowired
    private PresentacionRepository presentacionRepository;

    @GetMapping("/presentacion")
    public String get_panel_presentaciones() {
        return "home/productos/presentacion";
    }
    
    @GetMapping("/tabla_presentaciones")
    @ResponseBody
    public List<Presentacion> getPresentaciones() {
        return presentacionRepository.findAll();
    }
    
   @PostMapping("/add_presentacion")
    public ResponseEntity<String> add_presentacion(@RequestBody Presentacion presentacion) {  // ← @RequestBody para JSON
        try {
            presentacionRepository.save(presentacion);
            return ResponseEntity.ok("Presentacion agregada exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al guardar presentacion");
        }
    }
    
     @PutMapping("/update_presentacion")
    public ResponseEntity<String> updatePresentacion(@RequestBody Presentacion presentacion) {
        try {
            if (presentacionRepository.existsById(presentacion.getId())) {
                presentacionRepository.save(presentacion);
                return ResponseEntity.ok("Presentacion actualizada exitosamente");
            } else {
                return ResponseEntity.badRequest().body("Presentacion no encontrada");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar presentacion");
        }
    }

}
