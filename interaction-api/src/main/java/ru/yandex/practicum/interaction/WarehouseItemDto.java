package ru.yandex.practicum.interaction;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseItemDto {
    private Long productId;
    private Integer quantity;
    private Double width;
    private Double height;
    private Double depth;
    private Double weight;
    private boolean fragile;
}
