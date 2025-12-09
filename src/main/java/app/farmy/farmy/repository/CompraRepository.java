package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.Compra;
import app.farmy.farmy.model.Farmacia;
import app.farmy.farmy.model.EstadoPago;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer> {
    List<Compra> findByUsuario_FarmaciaAndEstadoPago(Farmacia farmacia, EstadoPago estadoPago);
}
