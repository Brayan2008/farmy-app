package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.Compra;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer> {

}
