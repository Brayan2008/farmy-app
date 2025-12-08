package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.Farmacia;
import app.farmy.farmy.model.Laboratorio;
import java.util.List;


@Repository
public interface LaboratorioRepository extends JpaRepository<Laboratorio, Integer> {

    List<Laboratorio> findByFarmacia(Farmacia farmacia);

}


