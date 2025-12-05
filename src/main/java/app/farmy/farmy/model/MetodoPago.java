package app.farmy.farmy.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

    @OneToMany(mappedBy = "metodoPago")
    private final List<Compra> compras = new ArrayList<>();

    @OneToMany(mappedBy = "metodoPago")
    private final List<Ventas> ventas = new ArrayList<>();

    @OneToMany(mappedBy = "metodoPago")
    private final List<PagoCompra> pagoCompras = new ArrayList<>();

    @OneToMany(mappedBy = "metodoPago")
    private final List<VentaPago> pagoVentas = new ArrayList<>();



    @PrePersist
    public void preSave(){
        estado = "Activo";
    }

}
