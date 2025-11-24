package app.farmy.farmy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Marca {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long idMarca;
    
    @Column(nullable = false, length = 30)
    private String nombreMarca;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(nullable = false)
    private Boolean estado ;
    
    public Marca (){}

    public Marca (String nombre, String descripcion, Boolean estado)
    {
        nombreMarca=nombre;
        this.descripcion=descripcion;
        this.estado = (estado==null) ? true : estado;
    }

    public Long getIdMarca() {
        return idMarca;
    }

    public String getNombreMarca() {
        return nombreMarca;
    }

    public void setNombreMarca(String nombreMarca) {
        this.nombreMarca = nombreMarca;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean isEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = (estado==null) ? true : estado;
    }
    
    
    

}
