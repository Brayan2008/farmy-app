package app.farmy.farmy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.farmy.farmy.model.Presentacion;

@Repository
public interface PresentacionRepository  extends JpaRepository<Presentacion, Long> {
}