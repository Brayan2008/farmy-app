package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.Lote;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Integer> {

}
