package app.farmy.farmy.repository;

import app.farmy.farmy.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    
    Optional<Reporte> findByCodigo(String codigo);
    
    List<Reporte> findAllByOrderByFechaGeneracionDesc();
    
    List<Reporte> findByTipo(String tipo);
    
    List<Reporte> findByEstado(String estado);
    
    void deleteByCodigo(String codigo);
    
    long count();
}