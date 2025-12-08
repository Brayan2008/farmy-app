package app.farmy.farmy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "farmacias")
public class Farmacia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_comercial", nullable = false, length = 150)
    private String nombreComercial;

    @Column(nullable = false, unique = true, length = 20)
    private String ruc;

    private String direccion;
    private String telefono;
    
    private Boolean estado = true; // true = Activo

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "farmacia")
    private final List<Productos> productos = new ArrayList<>();

    @OneToMany(mappedBy = "farmacia")
    private final List<Marca> marcas = new ArrayList<>();

    @OneToMany(mappedBy = "farmacia")
    private final List<Laboratorio> laboratorios = new ArrayList<>();

    @OneToMany(mappedBy = "farmacia")
    private final List<Presentacion> presentaciones = new ArrayList<>();

    @OneToMany(mappedBy = "farmacia")
    private final List<Categoria> categorias = new ArrayList<>();

    @OneToMany(mappedBy = "farmacia")
    private final List<Proveedor> proveedores = new ArrayList<>();

    @OneToMany(mappedBy = "farmacia")
    private final List<Cliente> clientes = new ArrayList<>();

    @OneToMany(mappedBy = "farmacia")
    private final List<Usuario> usuarios = new ArrayList<>();

    @OneToMany(mappedBy = "farmacia")
    private final List<Caja> cajas = new ArrayList<>();
}