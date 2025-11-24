package app.farmy.farmy.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int idCategoria;  

    @Column(length = 20)
    private String nombreCategoria;

    private String descripcion;

    private String estado;

    private LocalDate fechaRegistro;

    @PrePersist
    public void preGuardado() {
        if (estado == null)
            estado = "Activo";
        fechaRegistro = LocalDate.now();
    } 
}
