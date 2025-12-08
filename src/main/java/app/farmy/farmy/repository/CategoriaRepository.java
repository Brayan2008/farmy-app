package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.Categoria;
import app.farmy.farmy.model.Farmacia;

import java.util.List;


@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer>{

    List<Categoria> findByFarmacia(Farmacia farmacia);
    
}
