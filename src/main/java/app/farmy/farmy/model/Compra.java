package app.farmy.farmy.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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

    LocalDate fechaVencimientoPago;

    @PrePersist
    public void preSave() {
        fechaFactura = LocalDateTime.now();
        estado = "Pendiente"; //TODO Revisar: Esto hara que por defecto se haga a pendiente
    }

    //TODO Relacionarlo con Proveedor, Usuario y Metodo de pago
    //TODO Relacionarlo con CompraDetalle (M:M)
}

//Se mantiene en ese orden, caso contrario se necesita de la anotacion @Enumarated
enum TipoCompra {CONTADO, CREDITO} 
enum EstadoPago {PAGADO, PENDIENTE, ANULADO}