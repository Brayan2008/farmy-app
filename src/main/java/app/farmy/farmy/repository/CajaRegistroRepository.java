package app.farmy.farmy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.CajaRegistro;
import app.farmy.farmy.model.Farmacia;

@Repository
public interface CajaRegistroRepository extends JpaRepository<CajaRegistro, Integer> {
    
    List<CajaRegistro> findByFarmacia(Farmacia farmacia);
    
    List<CajaRegistro> findByEstado(String estado);
    
    List<CajaRegistro> findByFarmaciaAndEstado(Farmacia farmacia, String estado);
    
    @Query("SELECT COUNT(c) FROM CajaRegistro c WHERE c.farmacia = :farmacia")
    long countByFarmacia(@Param("farmacia") Farmacia farmacia);
}
