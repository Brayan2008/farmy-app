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
        @UniqueConstraint(name = "AK_Cliente", columnNames = {"numeroDocumento", "tipoDocumento"})
    }
)
@Getter
@Setter
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int idCliente;

    @Column(length = 11) 
    private String numeroDocumento;

    private TipoDocumentoEnum tipoDocumento;

    @Column(length = 13)
    private String nombre;
    
    @Column(length = 30)
    private String apellidos;
    
    @Column(length = 30)
    private String direccion;
    
    @Column(length = 30)
    private String email;

    private String estado;

    private LocalDate fechaRegistro;

    @PrePersist
    public void preGuardado() {
        estado = "Activo";
        fechaRegistro = LocalDate.now();
    }

}


