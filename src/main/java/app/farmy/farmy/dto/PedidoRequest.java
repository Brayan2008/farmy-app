package app.farmy.farmy.dto;

import java.util.List;
import lombok.Data;

@Data
public class PedidoRequest {
    private ClienteDTO cliente;
    private List<CartItemDTO> items;
    private Long idFarmacia;
}
