package app.farmy.farmy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.Farmacia;
import app.farmy.farmy.model.Productos;

@Repository
public interface ProductosRepository extends JpaRepository<Productos, Integer> {

    List<Productos> findByFarmacia(Farmacia farmacia);

}
