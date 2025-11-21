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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int idRol;

    @Column(length = 20)
    private String nombreRol;

    private String descripcion;

    private LocalDate fechaCreación;

    private String estado;

    @ManyToMany
    @JoinTable(name = "PermisosXRol", 
               joinColumns = @JoinColumn(name = "IdRol", foreignKey = @ForeignKey(name = "FK_Rol_PermisosXRol")), 
               inverseJoinColumns = @JoinColumn(name = "IdPermiso", foreignKey = @ForeignKey(name=" FK_Permiso_PermisosXRol")))
    private final List<Permisos> permisos = new ArrayList<>();

    @OneToMany(mappedBy = "rol")
    private final List<Usuario> usuarios = new ArrayList<>();

    @PrePersist
    public void preGuardado() {
        fechaCreación = LocalDate.now();
    }

}