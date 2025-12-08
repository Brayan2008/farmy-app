package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.Rol;
import java.util.List;


@Repository
public interface RolRepository extends JpaRepository<Rol, Integer>{
    
    List<Rol> findByNombreRol(String nombreRol);

}