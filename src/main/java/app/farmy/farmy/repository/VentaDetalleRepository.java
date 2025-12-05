package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.VentaDetalle;

@Repository
public interface VentaDetalleRepository extends JpaRepository<VentaDetalle, Integer> {

}
