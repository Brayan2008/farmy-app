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
        @UniqueConstraint(name = "AK_Laboratorio", columnNames = {"ruc"})
    }
)
@Getter
@Setter
public class Laboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int idLaboratorio;

    @Column(length = 20)
    private String codigo;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(length = 11, nullable = false)
    private String ruc;

    @Column(length = 20)
    private String telefono;

    @Column(length = 50)
    private String email;

    @Column(length = 100)
    private String direccion;

    private String estado;

    private LocalDate fechaRegistro;

    @OneToMany(mappedBy = "laboratorio")
    private final List<Productos> productos = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "id_farmacia", foreignKey = @ForeignKey(name = "FK_Farmacia_Laboratorio"))
    private Farmacia farmacia;

    @PrePersist
    public void preGuardado() {
        estado = "Activo";
        fechaRegistro = LocalDate.now();
    }

}