package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.Farmacia;
import app.farmy.farmy.model.Usuario;

import java.util.Optional;
import java.util.List;



@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{

    Optional<Usuario> findByEmail(String email);
    
    List<Usuario> findByFarmacia(Farmacia farmacia);
}
