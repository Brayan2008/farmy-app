package app.farmy.farmy.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import app.farmy.farmy.model.Marca;
import app.farmy.farmy.repository.MarcaRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;




@Controller
@RequestMapping("productos")
public class MarcaController {
    
    @Autowired
    private MarcaRepository marcaRepository;

    @GetMapping("/marcas")
    public String panel_marcas() {
        return "/home/productos/marcas";
    }
    
    @GetMapping("/tabla_marcas")
    @ResponseBody
    public List<Marca> getmarcas() {
        return marcaRepository.findAll();
    }
    
   @PostMapping("/add_marca")
    public ResponseEntity<String> add_marca(@RequestBody Marca marca) {  // ← @RequestBody para JSON
        try {
            marcaRepository.save(marca);
            return ResponseEntity.ok("Marca agregada exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al guardar marca");
        }
    }
    
     @PutMapping("/update_marca")
    public ResponseEntity<String> updateMarca(@RequestBody Marca marca) {
        try {
            if (marcaRepository.existsById(marca.getIdMarca())) {
                marcaRepository.save(marca);
                return ResponseEntity.ok("Marca actualizada exitosamente");
            } else {
                return ResponseEntity.badRequest().body("Marca no encontrada");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar marca");
        }
    }

}
