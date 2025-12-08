package app.farmy.farmy.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "caja")
@Getter
@Setter
public class Caja {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int idCaja;

    @Column(length = 20)
    private String codigo;

    private TipoMovimientoEnum tipo;

    @Column(length = 500)
    private String descripcion;

    @Column(columnDefinition = "DECIMAL(10,2)")
    private double monto;

    private LocalDate fecha;

    private LocalTime hora;

    private EstadoMovimientoEnum estado;

    @Column(length = 50)
    private String categoria;

    @ManyToOne
    @JoinColumn(name = "id_metodo_pago", foreignKey = @ForeignKey(name = "FK_MetodoPago_Caja"))
    private MetodoPago metodoPago;

    @Column(length = 100)
    private String responsable;

    @Column(length = 50)
    private String numeroComprobante;

    @Column(length = 50)
    private String referencia;

    @ManyToOne
    @JoinColumn(name = "id_farmacia", foreignKey = @ForeignKey(name = "FK_Farmacia_Caja"))
    private Farmacia farmacia;

    @PrePersist
    public void preGuardado() {
        if (fecha == null) {
            fecha = LocalDate.now();
        }
        if (hora == null) {
            hora = LocalTime.now();
        }
        if (estado == null) {
            estado = EstadoMovimientoEnum.Confirmado;
        }
    }
}

