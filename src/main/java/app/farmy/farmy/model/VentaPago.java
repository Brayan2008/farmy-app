package app.farmy.farmy.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
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
public class VentaPago {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int idVentaPago;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDateTime fechaPago;

    @ManyToOne
    @JoinColumn(name = "id_metodo_pago", foreignKey = @ForeignKey(name = "FK_MetodoPago_VentaPago"))
    private MetodoPago metodoPago;

    @ManyToOne
    @JoinColumn(name = "id_venta", foreignKey = @ForeignKey(name = "FK_Venta_VentaPago"))
    private Ventas venta;

    @PrePersist
    public void prePersist() {
            fechaPago = LocalDateTime.now();
    }

}
