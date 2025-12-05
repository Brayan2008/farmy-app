package app.farmy.farmy.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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

    @Column(precision = 10, scale = 2)
    BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    BigDecimal igv;

    @Column(precision = 10, scale = 2)
    BigDecimal total;

    String estado;

    String observaciones;  

    TipoCompra tipoCompra;

    EstadoPago estadoPago;

    @Column(precision = 10, scale = 2)
    BigDecimal saldoPendiente;
    
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

    @ManyToMany
    @JoinTable(
        name = "compra_lote",
            joinColumns = @JoinColumn(name = "numeroFactura", nullable = false, foreignKey = @ForeignKey(name = "FK_Compra_CompraLote")),
            inverseJoinColumns = @JoinColumn(name="idLote", nullable = false, foreignKey = @ForeignKey(name = "FK_Lote_CompraLote"))
    )
    private final List<Lote> lotes = new ArrayList<>();

    @OneToMany(mappedBy = "compra")
    private final List<PagoCompra> pagoCompras = new ArrayList<>();

}