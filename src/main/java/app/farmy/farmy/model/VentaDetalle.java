package app.farmy.farmy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class VentaDetalle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int idVentaDetalle;

    @Column(nullable = false)
    private int cantidad;

    @ManyToOne
    @JoinColumn(name = "id_lote", foreignKey = @ForeignKey(name = "FK_Lote_VentaDetalle"))
    private Lote lote;

    @ManyToOne
    @JoinColumn(name = "id_venta", foreignKey = @ForeignKey(name = "FK_Venta_VentaDetalle"))
    private Ventas venta;

}
