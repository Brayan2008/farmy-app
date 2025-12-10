package app.farmy.farmy.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import app.farmy.farmy.dto.LoginRequest;
import app.farmy.farmy.model.Usuario;
import app.farmy.farmy.repository.UsuarioRepository;
import app.farmy.farmy.services.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    Logger logger = Logger.getLogger(LoginController.class.getName());

    // 1. Servir la vista del Login (HTML)
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Busca login.html en templates
    }

    // 2. Servir la vista de Registro (Opcional por ahora)
    @GetMapping("/registro")
    public String showRegisterPage() {
        return "registro";
    }

    // 3. API para procesar el Login (Recibe JSON del AJAX)
    @PostMapping("/api/login")
    public ResponseEntity<?> procesarLogin(@RequestBody LoginRequest request, HttpSession session) {
        Usuario usuario = usuarioService.validarCredenciales(request.getEmail(), request.getPassword());

        if (usuario != null) {
            if ("Inactivo".equalsIgnoreCase(usuario.getEstado())) {
                return ResponseEntity.status(401).body("El usuario se encuentra inactivo. Contacte al administrador.");
            }

            logger.info("Usuario Logueado: "  + usuario.getEmail());

            usuario.setFechaUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(usuario);
            // Guardamos el usuario en la sesión del servidor
            session.setAttribute("usuarioLogueado", usuario);

            var farmacia = usuario.getFarmacia();
            logger.info("Farmacia obtenida: " + farmacia);

            if (farmacia != null) {
                session.setAttribute("farmaciaActual", farmacia);
            } 

            // Preparamos la respuesta JSON
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("rol", usuario.getSuperRol().toString());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }
    }

    // 4. Cerrar Sesión
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Destruye la sesión
        return "redirect:/login";
    }

    @GetMapping("/api/session/info")
    public ResponseEntity<Map<String, String>> getSessionInfo(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, String> data = new HashMap<>();
        data.put("nombre", usuario.getNombreCompleto());
        data.put("rol", usuario.getSuperRol().toString());

        if (usuario.getFarmacia() != null) {
            data.put("farmacia", usuario.getFarmacia().getNombreComercial());
        } else {
            data.put("farmacia", "Global System"); // Para el SuperAdmin
        }

        return ResponseEntity.ok(data);
    }
}
