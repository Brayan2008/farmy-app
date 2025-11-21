package app.farmy.farmy.dto;

import java.util.List;

import app.farmy.farmy.model.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;

public record RolDTO(@NotBlank String nombreRol,
                @Null String descripcion,
                @NotBlank String estado,
                List<String> permisos) {

        public Rol toRol(RolDTO rol) {
                return Rol.builder()
                                .descripcion(descripcion == null ? "" : descripcion)
                                .estado(this.estado)
                                .nombreRol(nombreRol)
                                .build();
        }

}