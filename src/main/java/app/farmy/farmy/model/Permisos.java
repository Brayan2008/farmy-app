package app.farmy.farmy.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Permisos {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int idPermiso;

    private String nombrePermiso;
    
    private String Descripcion;

}
