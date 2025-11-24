package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.Marca;

@Repository
public interface MarcaRepository extends JpaRepository<Marca,Long> {
    
}
