package app.farmy.farmy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Reporte {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String codigo;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private String tipo; 
    
    @Column(nullable = false)
    private String formato; 
    
    @Column
    private String descripcion;
    
    @Column(nullable = false)
    private String estado; 
    
    @Column(nullable = false)
    private LocalDateTime fechaGeneracion;
    
    @Column
    private LocalDateTime fechaCompletado;
    
    @Column(length = 1000)
    private String parametros; 
    
    @Column
    private String archivo; 
    
    @Column
    private String emailDestino;
    
    @Column
    private String frecuencia; // unica, diaria, semanal, mensual
    
    @Column
    private Integer registrosProcesados;
    
    @PrePersist
    public void prePersist() {
        if (fechaGeneracion == null) {
            fechaGeneracion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "proceso";
        }
    }
}