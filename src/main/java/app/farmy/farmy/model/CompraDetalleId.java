package app.farmy.farmy.model;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraDetalleId implements Serializable {

    private int numeroFactura;

    private int idProducto;

    private int idLote;

}
