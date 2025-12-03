package app.farmy.farmy.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CompraDetalle {

    @EmbeddedId
    private CompraDetalleId compraDetalleId = new CompraDetalleId();   

    @ManyToOne
    @MapsId("numeroFactura")
    @JoinColumn(name = "numero_factura", foreignKey = @ForeignKey(name = "FK_Compra_CompraDetalle"))
    private Compra compra;
    
    @ManyToOne
    @MapsId("idLote")
    @JoinColumn(name = "id_lote", foreignKey = @ForeignKey(name = "FK_Lote_CompraDetalle"))
    private Lote lote;

    @ManyToOne
    @MapsId("idProducto")
    @JoinColumn(name = "id_producto", foreignKey = @ForeignKey(name = "FK_Productov_CompraDetalle"))
    private Productos producto;

    @Column(nullable = false)
    private int cantidad;
    
    @Column(nullable = false)
    private double precioCompra;
    
    @Column(nullable = false)
    private double precioVenta;

}

