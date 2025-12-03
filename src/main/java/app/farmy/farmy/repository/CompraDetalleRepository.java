package app.farmy.farmy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.CompraDetalle;
import app.farmy.farmy.model.CompraDetalleId;

@Repository
public interface CompraDetalleRepository extends JpaRepository<CompraDetalle, CompraDetalleId> {

    List<CompraDetalle> findByCompra_NumeroFactura(int numeroFactura);

}
