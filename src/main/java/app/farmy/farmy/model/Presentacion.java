package app.farmy.farmy.model;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Presentacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Boolean estado;   

    @ManyToOne
    @JoinColumn(name = "id_farmacia", foreignKey = @ForeignKey(name = "FK_Farmacia_Presentacion"))
    private Farmacia farmacia;

    Presentacion(String nombre, Boolean estado) {
        this.nombre = nombre;
        this.estado = (estado==null)?true:estado;
    }
    
}
