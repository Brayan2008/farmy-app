package app.farmy.farmy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@AllArgsConstructor
@Getter
@Setter
public class Usuario {
    
    @Id
    private int Id;

    private String Nombre;

    private String CorreoElectronico;

    private String Estado;
}