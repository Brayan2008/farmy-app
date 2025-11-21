package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.ModulosEnum;
import app.farmy.farmy.model.Permisos;
import java.util.Optional;

@Repository
public interface PermisosRepository extends JpaRepository<Permisos, Integer> {

    Optional<Permisos> findByModulo(ModulosEnum modulo);
}
