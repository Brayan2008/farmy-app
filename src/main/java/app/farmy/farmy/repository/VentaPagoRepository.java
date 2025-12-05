package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.VentaPago;

@Repository
public interface VentaPagoRepository extends JpaRepository<VentaPago, Integer> {

}
