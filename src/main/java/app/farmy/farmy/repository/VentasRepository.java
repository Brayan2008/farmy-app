package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.Ventas;
import app.farmy.farmy.model.Farmacia;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentasRepository extends JpaRepository<Ventas, Integer> {
    List<Ventas> findByFarmaciaAndFechaVentaBetween(Farmacia farmacia, LocalDateTime start, LocalDateTime end);
    List<Ventas> findTop5ByFarmaciaOrderByFechaVentaDesc(Farmacia farmacia);
}
