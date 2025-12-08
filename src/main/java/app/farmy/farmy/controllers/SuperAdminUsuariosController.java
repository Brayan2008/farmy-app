package app.farmy.farmy.controllers;

import app.farmy.farmy.dto.UsuarioResponseDTO;
import app.farmy.farmy.model.Usuario;
import app.farmy.farmy.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/superadmin/usuarios")
public class SuperAdminUsuariosController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. LISTAR (Ya lo tenías, asegúrate de agregar el estado al DTO si quieres mostrarlo)
    @GetMapping
    public ResponseEntity<?> listarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            List<UsuarioResponseDTO> dtos = new ArrayList<>();
            for (Usuario u : usuarios) {
                UsuarioResponseDTO dto = new UsuarioResponseDTO();
                dto.setId((long) u.getIdUsuario());
                dto.setNombreCompleto(u.getNombreCompleto());
                dto.setEmail(u.getEmail());
                dto.setRol(u.getSuperRol() != null ? u.getSuperRol().toString() : "SIN_ROL");
                dto.setCreatedAt(u.getFechaCreacion());
                dto.setEstado(u.getEstado());
                
                if (u.getFarmacia() != null) {
                    dto.setNombreFarmacia(u.getFarmacia().getNombreComercial());
                } else {
                    dto.setNombreFarmacia("N/A (Global)");
                }
                dtos.add(dto);
            }
            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // 2. OBTENER UNO (Para editar)
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuario(@PathVariable int id) {
        Usuario u = usuarioRepository.findById(id).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();
        
        // Devolvemos solo lo necesario para editar
        return ResponseEntity.ok(u); // Aquí Jackson serializará. Si falla por farmacia, usa DTO. 
        // Si tienes JsonIgnoreProperties en Usuario.java funcionará bien.
    }

    // 3. ACTUALIZAR INFORMACIÓN
    @PutMapping
    public ResponseEntity<?> actualizarUsuario(@RequestBody Usuario usuarioEditado) {
        Usuario u = usuarioRepository.findById(usuarioEditado.getIdUsuario()).orElse(null);
        
        if (u == null) return ResponseEntity.notFound().build();

        // Actualizamos datos básicos
        u.setNombreCompleto(usuarioEditado.getNombreCompleto());
        u.setEmail(usuarioEditado.getEmail());
        
        // LÓGICA DE CONTRASEÑA:
        // Solo la cambiamos si el campo NO está vacío y NO es nulo.
        if (usuarioEditado.getPassword() != null && !usuarioEditado.getPassword().trim().isEmpty()) {
            u.setPassword(usuarioEditado.getPassword());
        }
        
        usuarioRepository.save(u);
        return ResponseEntity.ok().build();
    }

    // 4. CAMBIAR ESTADO (Bloquear/Desbloquear)
    @PostMapping("/{id}/cambiar-estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable int id) {
        Usuario u = usuarioRepository.findById(id).orElse(null);
        if (u != null) {
            // Lógica de inversión: Si es true pasa a false, si es false pasa a true.
            // Si es nulo, pasa a false (bloqueado).
            boolean estadoActual = UsuarioResponseDTO.convertirEstado(u.getEstado());
            u.setEstado(UsuarioResponseDTO.convertirEstado(!estadoActual)); // <--- Aquí ocurre la magia
            
            usuarioRepository.save(u);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}