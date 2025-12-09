package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.Caja;
import app.farmy.farmy.model.Farmacia;
import java.util.List;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Integer> {
    List<Caja> findByFarmacia(Farmacia farmacia);
    List<Caja> findTop5ByFarmaciaOrderByFechaDescHoraDesc(Farmacia farmacia);
}

