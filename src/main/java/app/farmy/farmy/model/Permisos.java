package app.farmy.farmy.model;

import java.util.ArrayList;
import java.util.List;

//import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Permisos {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int idPermiso;

    /*
     * @Column(length = 10, unique = true)
     * private String nombrePermiso;
     * 
     * private String descripcion;
     */

    @Enumerated(EnumType.STRING)
    ModulosEnum modulo;

    @ManyToMany(mappedBy = "permisos")
    private final List<Rol> roles = new ArrayList<>();

}