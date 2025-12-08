package app.farmy.farmy.controllers;

import app.farmy.farmy.dto.UsuarioRegistroDTO;

import app.farmy.farmy.model.Farmacia;
import app.farmy.farmy.model.Usuario;
import app.farmy.farmy.services.FarmaciaService;
import app.farmy.farmy.services.UsuarioService;
import app.farmy.farmy.services.FarmaciaService.SalidaFarmaciaDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/superadmin/farmacias")
public class SuperAdminApiController {

    @Autowired
    private FarmaciaService farmaciaService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<SalidaFarmaciaDTO> listar() {
        return farmaciaService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalidaFarmaciaDTO> obtener(@PathVariable Long id) {
        SalidaFarmaciaDTO f = farmaciaService.obtenerPorId(id);
        return f != null ? ResponseEntity.ok(f) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Farmacia> guardar(@RequestBody Farmacia farmacia) {
        return ResponseEntity.ok(farmaciaService.guardar(farmacia));
    }

    @PostMapping("/{id}/cambiar-estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id) {
        farmaciaService.cambiarEstado(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/crear-admin-farmacia")
    public ResponseEntity<?> crearAdminFarmacia(@RequestBody UsuarioRegistroDTO dto) {
        try {
            usuarioService.registrarUsuario(dto, Usuario.SuperRol.ADMIN_FARMACIA);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}