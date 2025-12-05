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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int idUsuario;

    @Column(nullable = false)
    private String nombreUsuario;

    @Column(nullable = false, length = 16)
    private String contraseña;

    @Column(nullable = false)
    private String nombreCompleto;

    @Column(nullable = false)
    private String email;

    @Column(length = 9)
    private String telefono;

    private LocalDate fechaCreacion;

    private LocalDate fechaUltimoAcceso;

    private String Estado;

    @ManyToOne
    @JoinColumn(name = "idRol", foreignKey = @ForeignKey(name = "FK_Rol_Usuario"))
    private Rol rol;

    @OneToMany(mappedBy = "usuario")
    private final List<Compra> compras = new ArrayList<>();
    
    @OneToMany(mappedBy = "usuario")
    private final List<PagoCompra> pagoCompras = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private final List<InventarioMovimiento> inventarioMovimientos = new ArrayList<>();

    @PrePersist
    public void fechaCreacion() {
        fechaCreacion = LocalDate.now();
        Estado = "Activo";
    }
    
    /*
     * @PreUpdate
     * public void fechaActualizacion(){
     * fechaUltimoAcceso = LocalDate.now();
     * }
     */

}