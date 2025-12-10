package app.farmy.farmy.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;

import app.farmy.farmy.model.Marca;
import app.farmy.farmy.repository.MarcaRepository;
import app.farmy.farmy.security.FarmySesion;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@Controller
@RequestMapping("productos")
public class MarcaController implements FarmySesion{
    
    @Autowired
    private MarcaRepository marcaRepository;

    @GetMapping("/marcas")
    public String panel_marcas() {
        return "/home/productos/marcas";
    }
    
    @GetMapping("/tabla_marcas")
    @ResponseBody
    public List<Marca> getmarcas(HttpSession session) {
        List<Marca> marcas;

        marcas = marcaRepository.findByFarmacia(getFarmaciaActual(session));
        return marcas;
    }
    
   @PostMapping("/add_marca")
    public ResponseEntity<String> add_marca(@RequestBody Marca marca, HttpSession session) {  // ← @RequestBody para JSON
        try {
            if (marca.getNombreMarca() != null || !marca.getNombreMarca().isEmpty() || !marca.getNombreMarca().isBlank()) {
                marca.setFarmacia(getFarmaciaActual(session));
                marcaRepository.save(marca);
                return ResponseEntity.ok("Marca agregada exitosamente");
            } else {
                return ResponseEntity.badRequest().body("El nombre de la marca es requerido");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al guardar marca");
        }
    }
    
    @PutMapping("/update_marca")
    public ResponseEntity<String> updateMarca(@RequestBody Marca marca) {
        try {
            var getMarca = marcaRepository.findById(marca.getIdMarca()).get();
            if (getMarca != null) {
                getMarca.setNombreMarca(marca.getNombreMarca());
                getMarca.setDescripcion(marca.getDescripcion());
                getMarca.setEstado(marca.getEstado());
                marcaRepository.save(getMarca);
                return ResponseEntity.ok("Marca actualizada exitosamente");
            } else {
                return ResponseEntity.badRequest().body("Marca no encontrada");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar marca");
        }
    }

}
