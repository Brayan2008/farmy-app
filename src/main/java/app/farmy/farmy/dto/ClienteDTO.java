package app.farmy.farmy.dto;

import lombok.Data;

@Data
public class ClienteDTO {
    private String nombres;
    private String apellidos;
    private String tipoDocumento;
    private String numeroDocumento;
    private String email;
    private String telefono;
    private String direccion;
}
