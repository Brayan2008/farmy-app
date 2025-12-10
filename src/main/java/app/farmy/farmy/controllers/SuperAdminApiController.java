package app.farmy.farmy.controllers;

import app.farmy.farmy.dto.UsuarioRegistroDTO;

import app.farmy.farmy.model.Farmacia;
import app.farmy.farmy.model.Usuario;
import app.farmy.farmy.repository.FarmaciaRepository;
import app.farmy.farmy.services.FarmaciaService;
import app.farmy.farmy.services.UsuarioService;
import app.farmy.farmy.services.FarmaciaService.SalidaFarmaciaDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/superadmin/farmacias")
public class SuperAdminApiController {

    @Autowired
    private FarmaciaService farmaciaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private FarmaciaRepository farmaciaRepository;

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
    public ResponseEntity<Farmacia> guardar(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("nombreComercial") String nombreComercial,
            @RequestParam("ruc") String ruc,
            @RequestParam("direccion") String direccion,
            @RequestParam("telefono") String telefono,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen) {
        
        Farmacia farmacia;
        if (id != null) {
            // Modo Edición: Buscamos la existente usando el repositorio
            farmacia = farmaciaRepository.findById(id).orElse(null);
            if (farmacia == null) return ResponseEntity.notFound().build();
            
            // No necesitamos setear ID ni RUC manualmente si ya tenemos la entidad persistente
            // El RUC no se debe cambiar si es edición (o validarse si se permite)
            // farmacia.setRuc(ruc); // Opcional: Si quieres permitir cambiarlo, pero cuidado con duplicados
        } else {
            farmacia = new Farmacia();
            farmacia.setRuc(ruc); // Solo seteamos RUC si es nueva
        }

        farmacia.setNombreComercial(nombreComercial);
        // farmacia.setRuc(ruc); // Movemos esto al else o validamos
        if (id == null) farmacia.setRuc(ruc); // Aseguramos que solo se setee en creación o si decides permitir edición
        
        farmacia.setDireccion(direccion);
        farmacia.setTelefono(telefono);

        if (imagen != null && !imagen.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + imagen.getOriginalFilename();
                Path uploadPath = Paths.get("uploads");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imagen.getInputStream(), filePath);
                farmacia.setLogoUrl(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().build();
            }
        }

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