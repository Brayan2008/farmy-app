package app.farmy.farmy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.Cliente;
import app.farmy.farmy.model.Farmacia;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    List<Cliente> findByFarmacia(Farmacia farmacia);

}
