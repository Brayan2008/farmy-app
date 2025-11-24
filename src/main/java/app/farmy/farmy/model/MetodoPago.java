package app.farmy.farmy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"nombreMetodoPago"}, name = "AK_MetodoPago"))
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int idMetodoPago;

    private String nombreMetodoPago;

    private String descripcion;

    private String estado;

    @PrePersist
    public void preSave(){
        estado = "Activo";
    }

}
