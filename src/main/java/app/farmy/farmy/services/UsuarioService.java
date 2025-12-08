package app.farmy.farmy.services;

import app.farmy.farmy.dto.UsuarioRegistroDTO; // Asegúrate de tener este import
import app.farmy.farmy.model.Farmacia;
import app.farmy.farmy.model.Rol;
import app.farmy.farmy.model.Usuario;
import app.farmy.farmy.model.Usuario.SuperRol;
import app.farmy.farmy.repository.FarmaciaRepository;
import app.farmy.farmy.repository.RolRepository;
import app.farmy.farmy.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired // <--- ¡ESTO ERA LO QUE FALTABA O FALLABA!
    private FarmaciaRepository farmaciaRepository;

    // Lógica de Login
    public Usuario validarCredenciales(String email, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getPassword().equals(password)) {
                return usuario;
            }
        }
        return null;
    }

    // Lógica de Registro
    public Usuario registrarUsuario(UsuarioRegistroDTO dto, Usuario.SuperRol rol) {
        // 1. Validar email duplicado
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado en el sistema.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(dto.getNombreCompleto());
        usuario.setNombreUsuario(dto.getAlias());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());
        usuario.setSuperRol(rol);

        if(rol ==  SuperRol.ADMIN_FARMACIA) {
                rolRepository.findById(1).ifPresent(usuario::setRol);
        }

        // 2. Vincular Farmacia (Si el ID viene en el DTO)
        if (dto.getFarmaciaId() != null) {
            Farmacia f = farmaciaRepository.findById(dto.getFarmaciaId())
                    .orElseThrow(() -> new RuntimeException("Error: La farmacia seleccionada no existe."));
            usuario.setFarmacia(f);
        }

        return usuarioRepository.save(usuario);
    }
}