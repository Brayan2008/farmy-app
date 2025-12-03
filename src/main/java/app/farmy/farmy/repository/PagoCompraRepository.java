package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.PagoCompra;
import app.farmy.farmy.model.PagoCompraId;

@Repository
public interface PagoCompraRepository extends JpaRepository<PagoCompra, PagoCompraId> {

}
