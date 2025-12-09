package app.farmy.farmy.model;

import java.time.LocalDate;
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

    private String telefono;

    private LocalDate fechaRegistro;

    @OneToMany(mappedBy = "cliente")
    private final List<Ventas> ventas = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "id_farmacia", foreignKey = @ForeignKey(name = "FK_Farmacia_Cliente"))
    private Farmacia farmacia;

    @PrePersist
    public void preGuardado() {
        estado = "Activo";
        fechaRegistro = LocalDate.now();
    }

}


