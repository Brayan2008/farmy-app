package app.farmy.farmy.model;

import java.time.LocalDate;
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
import jakarta.persistence.OneToMany;
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

    @OneToMany(mappedBy = "categoria")
    private List<Productos> productos = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "id_farmacia", foreignKey = @ForeignKey(name = "FK_Farmacia_Categoria"))
    private Farmacia farmacia;

    @PrePersist
    public void preGuardado() {
        if (estado == null)
            estado = "Activo";
        fechaRegistro = LocalDate.now();
    } 
}
