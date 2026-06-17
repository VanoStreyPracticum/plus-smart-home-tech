package ru.yandex.practicum.interaction;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private ProductCategory category;
    private QuantityState quantityState;
    private ProductState state;
}
