package app.farmy.farmy.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Getter
@Setter
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "UK_Email_Usuario"),
})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idUsuario;

    @Column(nullable = false)
    private String nombreUsuario;

    @Column(nullable = false, length = 16)
    private String password;

    @Column(nullable = false)
    private String nombreCompleto;

    @Column(nullable = false)
    private String email;

    @Column(length = 9)
    private String telefono;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaUltimoAcceso;

    private String Estado;

    @ManyToOne
    @JoinColumn(name = "idRol", foreignKey = @ForeignKey(name = "FK_Rol_Usuario"))
    private Rol rol;

    @Enumerated(EnumType.STRING)
    private SuperRol superRol; // Enum que crearemos abajo

    @OneToMany(mappedBy = "usuario")
    private final List<Compra> compras = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private final List<PagoCompra> pagoCompras = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private final List<InventarioMovimiento> inventarioMovimientos = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private final List<Ventas> ventas = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private final List<CajaRegistro> cajasRegistro = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idFarmacia", foreignKey = @ForeignKey(name = "FK_Farmacia_Usuario"))
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Farmacia farmacia;

    @PrePersist
    public void fechaCreacion() {
        fechaCreacion = LocalDateTime.now();
        Estado = "Activo";
    }

    public enum SuperRol {
        SUPER_ADMIN,
        ADMIN_FARMACIA,
        EMPLEADO
    }

    /*
     * @PreUpdate
     * public void fechaActualizacion(){
     * fechaUltimoAcceso = LocalDate.now();
     * }
     */

}