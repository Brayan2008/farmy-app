package app.farmy.farmy.dto;

import lombok.Data;

@Data
public class UsuarioRegistroDTO {
    private String nombreCompleto;
    private String alias;
    private String email;
    private String password;
    private Long farmaciaId; // Vital: Aquí vinculamos el usuario a la farmacia
}