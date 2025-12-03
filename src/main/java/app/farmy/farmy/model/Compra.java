package app.farmy.farmy.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class Compra {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    int numeroFactura;

    LocalDateTime fechaFactura;

    double subtotal;

    double igv;

    double total;

    String estado;

    String observaciones;  

    TipoCompra tipoCompra;

    EstadoPago estadoPago;

    double saldoPendiente;

    LocalDate fechaVencimientoPago; //Esto es por si el TipoCompra = CREDITO

    String motivoAnulacion;

    @PrePersist
    public void preSave() {
        fechaFactura = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "idProveedor", foreignKey = @ForeignKey(name =  "FK_Proveedor_Compra"))
    private Proveedor proveedor;

    @ManyToOne
    @JoinColumn(name = "idUsuario", foreignKey = @ForeignKey(name = "FK_Usuarios_Compra"))
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "idMetodoPago", foreignKey = @ForeignKey(name = "FK_MetodoPago_Compra"))
    private MetodoPago metodoPago;

    @OneToMany(mappedBy = "compra")
    private final List<CompraDetalle> compra = new ArrayList<>();

    @OneToMany(mappedBy = "numeroFactura")
    private final List<PagoCompra> pagoCompras = new ArrayList<>();

}