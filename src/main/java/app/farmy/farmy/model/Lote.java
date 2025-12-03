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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(name = "AK_Lote", columnNames = { "numeroLote" }))
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    int idLote;

    @Column(nullable = false)
    int numeroLote;

    LocalDate fechaFabricacion;

    @Column(nullable = false)
    LocalDate fechaVencimiento;

    int cantidadInicial;

    int cantidadActual;

    String estado;

    LocalDate fechaRegistro;

    @ManyToOne
    @JoinColumn(name = "idProducto", foreignKey = @ForeignKey(name="FK_Producto_Lote"))
    private Productos producto;

    @OneToMany(mappedBy = "lote")
    private final List<CompraDetalle> compra = new ArrayList<>();

    @PrePersist
    public void preSave() {
        fechaRegistro = LocalDate.now();
    }

}
