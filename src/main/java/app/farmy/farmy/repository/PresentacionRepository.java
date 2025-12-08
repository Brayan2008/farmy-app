package app.farmy.farmy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.Farmacia;
import app.farmy.farmy.model.Presentacion;

@Repository
public interface PresentacionRepository  extends JpaRepository<Presentacion, Long> {

    List<Presentacion> findByFarmacia(Farmacia farmacia);
}