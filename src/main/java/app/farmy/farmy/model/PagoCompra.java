package app.farmy.farmy.model;

import java.time.LocalDateTime;
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
public class PagoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int idPagoCompra;

    @Column(nullable = false)
    private double montoPago;
    
    @Column(nullable = false)
    private LocalDateTime fechaPago;

    private String numeroRecibo;

    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "idUsuario", foreignKey = @ForeignKey(name = "FK_Usuario_PagoCompra"))
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "compra", foreignKey = @ForeignKey(name = "FK_Compra_PagoCompra"))
    private Compra compra;
    
    @ManyToOne
    @JoinColumn(name = "id_metodo_pago", foreignKey = @ForeignKey(name = "FK_MetodoPago_PagoCompra"))
    private MetodoPago metodoPago;


}

