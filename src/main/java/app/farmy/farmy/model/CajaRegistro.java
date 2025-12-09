package app.farmy.farmy.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "caja_registro")
@Getter
@Setter
public class CajaRegistro {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idCajaRegistro;

    @Column(nullable = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private String estado; // Activo, Inactivo

    private LocalDateTime fechaCreacion;

    @ManyToOne
    @JoinColumn(name = "idUsuario", foreignKey = @ForeignKey(name = "FK_Usuario_CajaRegistro"))
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "idFarmacia", foreignKey = @ForeignKey(name = "FK_Farmacia_CajaRegistro"))
    private Farmacia farmacia;

    @PrePersist
    public void preGuardado() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "Activo";
        }
        // Generar código temporal si no existe (para cumplir con NOT NULL en BD)
        // El código definitivo se generará en el controlador después de obtener el ID
        if (codigo == null || codigo.trim().isEmpty()) {
            codigo = "TMP" + System.currentTimeMillis();
        }
    }
}
