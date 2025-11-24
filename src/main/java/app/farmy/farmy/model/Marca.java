package app.farmy.farmy.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Marca {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long idMarca;
    
    @Column(nullable = false, length = 30)
    private String nombreMarca;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(nullable = false)
    private Boolean estado;
    
    @OneToMany(mappedBy = "marca")
    private final List<Productos> listaProductos = new ArrayList<>();
    
    public Marca (String nombre, String descripcion, Boolean estado){
        nombreMarca=nombre;
        this.descripcion=descripcion;
        this.estado = (estado==null) ? true : estado;
    }



}
