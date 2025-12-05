package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.InventarioMovimiento;

@Repository
public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Integer> {

}
