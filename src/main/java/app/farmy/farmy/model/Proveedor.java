package app.farmy.farmy.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(name = "AK_Proveedor", columnNames = {"ruc"})
    }
)
@Getter
@Setter
public class Proveedor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int idProveedor;

    @Column(length = 20)
    private String codigo;

    @Column(length = 100, nullable = false)
    private String razonSocial;

    @Column(length = 11, nullable = false)
    private String ruc;

    @Column(length = 20)
    private String telefono;

    @Column(length = 50)
    private String email;

    private TipoProveedorEnum tipo;

    @Column(length = 100)
    private String direccion;

    private String estado;

    private LocalDate fechaRegistro;

    @PrePersist
    public void preGuardado() {
        estado = "Activo";
        fechaRegistro = LocalDate.now();
    }

}

