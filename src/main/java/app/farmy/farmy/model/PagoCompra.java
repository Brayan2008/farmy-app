package app.farmy.farmy.model;

import java.time.LocalDateTime;

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
public class PagoCompra {

    @EmbeddedId
    private PagoCompraId pagoCompraId;

    @ManyToOne
    @MapsId("numeroFactura")
    @JoinColumn(name = "numero_factura", foreignKey = @ForeignKey(name = "FK_Compra_PagoCompra"))
    private Compra numeroFactura;
    
    @ManyToOne
    @MapsId("idMetodoPago")
    @JoinColumn(name = "id_metodo_pago", foreignKey = @ForeignKey(name = "FK_MetodoPago_PagoCompra"))
    private MetodoPago metodoPago;

    @Column(nullable = false)
    private double montoPago;
    
    @Column(nullable = false)
    private LocalDateTime fechaPago;

    private String numeroRecibo;

    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "idUsuario", foreignKey = @ForeignKey(name = "FK_Usuario_PagoCompra"))
    private Usuario usuario;

}

