package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.Farmacia;
import app.farmy.farmy.model.Proveedor;
import java.util.List;


@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    List<Proveedor> findByFarmacia(Farmacia farmacia);

}

