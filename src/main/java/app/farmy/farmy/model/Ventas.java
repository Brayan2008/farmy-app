package app.farmy.farmy.model;

import java.math.BigDecimal;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Ventas {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int idVenta;

    @Column(nullable = false)
    private LocalDateTime fechaVenta;

    @Column(precision = 10, scale = 2)
    private BigDecimal igv;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal total;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal subTotal;

    @Column(nullable = false)
    private EstadoPago estadoPago;

    @Column(nullable = false)
    private TipoVenta tipoVenta;

    @Column(precision = 10, scale = 2)
    private BigDecimal saldoPendiente;

    private LocalDateTime fechaVencimientoPago;

    private String observaciones;

    private String motivoAnulacion;

    private String estado; // Activo / Anulado

    @ManyToOne
    @JoinColumn(name = "id_cliente", foreignKey = @ForeignKey(name = "FK_Cliente_Ventas"))
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_usuario", foreignKey = @ForeignKey(name = "FK_Usuario_Ventas"))
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_metodo_pago", foreignKey = @ForeignKey(name = "FK_MetodoPago_Ventas"))
    private MetodoPago metodoPago;

    @OneToMany(mappedBy = "venta")
    private final List<VentaDetalle> ventaDetalles = new ArrayList<>();

    @OneToMany(mappedBy = "venta")
    private final List<VentaPago> pagoVentas = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "id_farmacia", foreignKey = @ForeignKey(name = "FK_Farmacia_Ventas"))
    private Farmacia farmacia;

    @PrePersist
    public void preGuardado() {
        fechaVenta = LocalDateTime.now();
    }

}
