package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.Caja;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Integer> {

}

