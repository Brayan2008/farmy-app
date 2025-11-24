package app.farmy.farmy.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    String nombreProducto;

    String descripcion;

    String principioActivo;

    String concentracion;

    String formaFarmaceutica; // Ojo esto puede ser un enum o separarlo en una tabla

    boolean requiereReceta;

    String estado;

    LocalDate fechaRegistro;

    @ManyToOne
    @JoinColumn(name = "idPresentacion", foreignKey = @ForeignKey(name = "FK_Presentacion_Productos"))
    private Presentacion presentacion;

    @ManyToOne
    @JoinColumn(name = "idMarca", foreignKey = @ForeignKey(name = "FK_Marca_Productos"))
    private Marca marca;

    @ManyToOne
    @JoinColumn(name = "idCategoria", foreignKey = @ForeignKey(name = "FK_Categoria_Productos"))
    private Categoria categoria;

    @PrePersist
    public void preGuardado() {
        fechaRegistro = LocalDate.now();
        estado = "Activo";
    }

    // TODO relacionarlo con Laboratorio
    // TODO realizar el frontend del producto

    // TODO llevarlo a Lote e InventarioMovimiento

}
