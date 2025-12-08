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
public class Productos {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    int idProducto;

    @Column(nullable = false)
    String nombreProducto;
    
    String descripcion;
    
    String principioActivo;
    
    String concentracion;
    
    String formaFarmaceutica; 
    
    @Column(nullable = false)
    boolean requiereReceta;
    
    @Column(nullable = false)
    String estado;
    
    @Column(nullable = false)
    LocalDate fechaRegistro;
    
    Integer stock;

    String imgUrl;

    @ManyToOne
    @JoinColumn(name = "idPresentacion", foreignKey = @ForeignKey(name = "FK_Presentacion_Productos"))
    private Presentacion presentacion;

    @ManyToOne
    @JoinColumn(name = "idMarca", foreignKey = @ForeignKey(name = "FK_Marca_Productos"))
    private Marca marca;

    @ManyToOne
    @JoinColumn(name = "idCategoria", foreignKey = @ForeignKey(name = "FK_Categoria_Productos"))
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "idLaboratorio", foreignKey = @ForeignKey(name = "FK_Laboratorio_Producto"))
    private Laboratorio laboratorio;

    @OneToMany(mappedBy = "producto")
    private final List<Lote> lotes = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "idFarmacia", foreignKey = @ForeignKey(name = "FK_Farmacia_Producto"))
    private Farmacia farmacia;

    @PrePersist
    public void preGuardado() {
        fechaRegistro = LocalDate.now();
        estado = "Activo";
    }



}
