package app.farmy.farmy.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsuarioResponseDTO {
    private Long id;
    private String nombreCompleto;
    private String email;
    private String rol;
    private String nombreFarmacia; // En lugar del objeto Farmacia entero, solo mandamos el nombre
    private LocalDateTime createdAt;
    private Boolean estado;


    public void setEstado(String estado) {
        this.estado = convertirEstado(estado);
    }

    public static boolean convertirEstado(String estaString) {
        return switch (estaString.toUpperCase()) {
            case "ACTIVO" -> true;
            case "INACTIVO" -> false;
            default -> false;
        };
    }

    public static String convertirEstado(boolean estadoBool) {
        return estadoBool ? "Activo" : "Inactivo";
    }
}