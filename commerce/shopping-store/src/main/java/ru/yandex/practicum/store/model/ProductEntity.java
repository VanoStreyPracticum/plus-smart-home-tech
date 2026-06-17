package ru.yandex.practicum.store.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.interaction.ProductCategory;
import ru.yandex.practicum.interaction.ProductState;
import ru.yandex.practicum.interaction.QuantityState;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID productId;

    private String productName;
    private String description;
    private String imageSrc;
    private Double price;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    private QuantityState quantityState;

    @Enumerated(EnumType.STRING)
    private ProductState state;
}
