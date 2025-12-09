package app.farmy.farmy.dto;

import lombok.Data;

@Data
public class CartItemDTO {
    private int id; // idLote
    private String name;
    private double price;
    private int quantity;
}
